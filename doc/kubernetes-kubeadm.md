# **kubeadm** 集群安装

参考网址

* https://github.com/eip-work/kuboard-press/blob/master/install/install-k8s.md



> 目录

[TOC]


# 第一步、检查配置



> 检查 centos / hostname

```shell
# 在 master 节点和 worker 节点都要执行
cat /etc/redhat-release

# 此处 hostname 的输出将会是该机器在 Kubernetes 集群中的节点名字
# 不能使用 localhost 作为节点的名字
hostname

# 请使用 lscpu 命令，核对 CPU 信息
# Architecture: x86_64    本安装文档不支持 arm 架构
# CPU(s):       2         CPU 内核数量不能低于 2
lscpu
```



> 操作系统兼容性

| CentOS 版本 | 本文档是否兼容 | 备注                                |
| ----------- | -------------- | ----------------------------------- |
| 7.6         | 😄              | 已验证                              |
| 7.5         | 😄              | 已验证                              |
| 7.4         | 🤔              | 待验证                              |
| 7.3         | 🤔              | 待验证                              |
| 7.2         | 😞              | 已证实会出现 kubelet 无法启动的问题 |



> 修改 hostname

 如果您需要修改 hostname，可执行如下指令：

```shell
# 修改 hostname
hostnamectl set-hostname your-new-host-name

# 查看修改结果
hostnamectl status

# 设置 hostname 解析
echo "127.0.0.1   $(hostname)" >> /etc/hosts
```



> 安装后的软件版本为

- Kubernetes v1.16.0
  - calico 3.8.2
  - nginx-ingress 1.5.5
- Docker 18.09.7



> 拟安装机器

| vbox             | hostname      | ip            |
| ---------------- | ------------- | ------------- |
| k8s-master-105   | master.demo   | 192.168.1.185 |
| k8s-worker01-107 | worker01.demo | 192.168.1.186 |
| k8s-worker02-108 | worker02.demo | 192.168.1.187 |



# 第二步、制作标准镜像

这一步单独做，主要的目的是用来备份镜像，下次不用重新安装了。



## 2.1、 步骤说明

**使用 root 身份在所有节点执行如下代码，以安装软件：**

- docker
- nfs-utils
- kubectl / kubeadm / kubelet



**步骤：**

* 第一步、安装 docker
* 第二步、安装 nfs-utils
* 第三步、K8S基本配置
  * 关闭 防火墙
  * 关闭 SeLinux
  * 关闭 swap
  * 修改 /etc/sysctl.conf
  * 配置K8S的yum源
*  第四步、安装kubelet、kubeadm、kubectl
* 第五步、docker设置优化
  * 修改docker Cgroup Driver为systemd
  * 设置 docker 镜像国内下载地址
* 第六步、启动设备



## 2.2、执行安装操作

可以执行快速安装命令，也可以手工一条一条执行



> 网友提供的快速安装

```
# 在 master 节点和 worker 节点都要执行

curl -sSL https://kuboard.cn/install-script/v1.16.0/install-kubelet.sh | sh
```

> 代码说明

[代码详细](shell/k8s-install-kubeadm/install-kubelet.sh) 或参考[原始代码]()

```sh
#!/bin/bash

# 在 master 节点和 worker 节点都要执行

# 安装 docker
# 参考文档如下
# https://docs.docker.com/install/linux/docker-ce/centos/ 
# https://docs.docker.com/install/linux/linux-postinstall/

# 卸载旧版本
yum remove -y docker \
docker-client \
docker-client-latest \
docker-common \
docker-latest \
docker-latest-logrotate \
docker-logrotate \
docker-selinux \
docker-engine-selinux \
docker-engine

# 设置 yum repository
yum install -y yum-utils \
device-mapper-persistent-data \
lvm2
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 安装并启动 docker
yum install -y docker-ce-18.09.7 docker-ce-cli-18.09.7 containerd.io
systemctl enable docker
systemctl start docker

# 安装 nfs-utils
# 必须先安装 nfs-utils 才能挂载 nfs 网络存储
yum install -y nfs-utils

# 关闭 防火墙
systemctl stop firewalld
systemctl disable firewalld

# 关闭 SeLinux
setenforce 0
sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" /etc/selinux/config

# 关闭 swap
swapoff -a
yes | cp /etc/fstab /etc/fstab_bak
cat /etc/fstab_bak |grep -v swap > /etc/fstab

# 修改 /etc/sysctl.conf
# 如果有配置，则修改
sed -i "s#^net.ipv4.ip_forward.*#net.ipv4.ip_forward=1#g"  /etc/sysctl.conf
sed -i "s#^net.bridge.bridge-nf-call-ip6tables.*#net.bridge.bridge-nf-call-ip6tables=1#g"  /etc/sysctl.conf
sed -i "s#^net.bridge.bridge-nf-call-iptables.*#net.bridge.bridge-nf-call-iptables=1#g"  /etc/sysctl.conf
# 可能没有，追加
echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-ip6tables = 1" >> /etc/sysctl.conf
echo "net.bridge.bridge-nf-call-iptables = 1" >> /etc/sysctl.conf
# 执行命令以应用
sysctl -p

# 配置K8S的yum源
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
       http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

# 卸载旧版本
yum remove -y kubelet kubeadm kubectl

# 安装kubelet、kubeadm、kubectl
yum install -y kubelet-1.15.3 kubeadm-1.15.3 kubectl-1.15.3

# 修改docker Cgroup Driver为systemd
# # 将/usr/lib/systemd/system/docker.service文件中的这一行 ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock
# # 修改为 ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock --exec-opt native.cgroupdriver=systemd
# 如果不修改，在添加 worker 节点时可能会碰到如下错误
# [WARNING IsDockerSystemdCheck]: detected "cgroupfs" as the Docker cgroup driver. The recommended driver is "systemd". 
# Please follow the guide at https://kubernetes.io/docs/setup/cri/
sed -i "s#^ExecStart=/usr/bin/dockerd.*#ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock --exec-opt native.cgroupdriver=systemd#g" /usr/lib/systemd/system/docker.service

# 设置 docker 镜像，提高 docker 镜像下载速度和稳定性
# 如果您访问 https://hub.docker.io 速度非常稳定，亦可以跳过这个步骤
curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://91c0cc1e.m.daocloud.io

# 重启 docker，并启动 kubelet
systemctl daemon-reload
systemctl restart docker
systemctl enable kubelet && systemctl start kubelet

docker version
```





# 第三步、备份标准镜像

使用vbox做一个镜像，这样做worker就简单了





# 第四步、初始化 master 节点



## 4.1、步骤说明

- 以 root 身份在master 机器上执行
- 初始化 master 节点时，如果因为中间某些步骤的配置出错，想要重新初始化 master 节点，请先执行 `kubeadm reset`操作



> 具体步骤

* kubeadm init
* 配置kubectl
* 安装calico 网络插件





## 4.2、执行安装操作



> 前期准备

```shell
# 只在 master 节点执行
# 替换 x.x.x.x 为 master 节点实际 IP（请使用内网 IP）
# export 命令只在当前 shell 会话中有效，开启新的 shell 窗口后，请重新执行此处的 export 命令
export MASTER_IP=x.x.x.x

# 替换 apiserver.demo 为 您想要的 dnsName (不建议使用 master 的 hostname 作为 APISERVER_NAME)
export APISERVER_NAME=apiserver.demo

# Kubernetes 容器组所在的网段，该网段安装完成后，由 kubernetes 创建，事先并不存在于您的物理网络中
export POD_SUBNET=10.100.0.1/20

echo "${MASTER_IP}    ${APISERVER_NAME}" >> /etc/hosts

```



> 执行脚本

下面脚本是网友写的，也可以按照下面的内容手工执行

```shell
curl -sSL https://kuboard.cn/install-script/v1.16.0/init-master.sh | sh
```

> > 也可以手工执行脚本

```shell
#!/bin/bash

# 只在 master 节点执行

# 查看完整配置选项 https://godoc.org/k8s.io/kubernetes/cmd/kubeadm/app/apis/kubeadm/v1beta2
rm -f ./kubeadm-config.yaml

cat <<EOF > ./kubeadm-config.yaml
apiVersion: kubeadm.k8s.io/v1beta2
kind: ClusterConfiguration
kubernetesVersion: v1.15.3
imageRepository: registry.cn-hangzhou.aliyuncs.com/google_containers
controlPlaneEndpoint: "${APISERVER_NAME}:6443"
networking:
  serviceSubnet: "10.96.0.0/12"
  podSubnet: "${POD_SUBNET}"
  dnsDomain: "cluster.local"
EOF

# kubeadm init
# 根据您服务器网速的情况，您需要等候 3 - 10 分钟
kubeadm init --config=kubeadm-config.yaml --upload-certs

# 配置 kubectl
rm -rf /root/.kube/
mkdir /root/.kube/
cp -i /etc/kubernetes/admin.conf /root/.kube/config

# 安装 calico 网络插件
# 参考文档 https://docs.projectcalico.org/v3.8/getting-started/kubernetes/
rm -f calico.yaml
wget https://docs.projectcalico.org/v3.8/manifests/calico.yaml
sed -i "s#192\.168\.0\.0/16#${POD_SUBNET}#" calico.yaml
kubectl apply -f calico.yaml
```



## 4.3、检查 master 初始化结果



```shell
# 只在 master 节点执行

# 执行如下命令，等待 3-10 分钟，直到所有的容器组处于 Running 状态
watch kubectl get pod -n kube-system -o wide

# 查看 master 节点初始化结果
kubectl get nodes -o wide
```



![](imgs/k8s-install-master-runing.png)





```shell
export MASTER_IP=192.168.1.185
export APISERVER_NAME=apiserver.demo
export POD_SUBNET=10.100.0.1/20
echo "${MASTER_IP}    ${APISERVER_NAME}" >> /etc/hosts
curl -sSL https://kuboard.cn/install-script/v1.15.3/init-master.sh | sh
```

kubectl run net-test --image=alpine --replicas=2 sleep 3600

kubectl  delete deploy net-test 

kubectl run  -dit  net-test --image=alpine  ash

kubectl run    net-test --image=alpine  /bin/sh

kubectl get pod

kubectl get deploy 











# 第五步、初始化 worker节点



## 5.1 通过镜像生成虚拟机

启动虚拟机，并配置网络ifcfg-enp0s8，有两个地方要修改

- `vi /etc/sysconfig/network-scripts`
- IPADDR =192.168.1.***
- UUID 改成与其他不同就可以了





## 5.2 设置hosts与hostname



> 设置hostname

每个机器的名字**应该都不一样**，这样好管理

下面应该应该根据具体的情况，来修改**worker01.demo** 的名称

```shell
hostnamectl set-hostname worker01.demo 
hostname
```



> 设置hosts，可以找到主机

这里应该注意`192.168.1.185  apiserver.demo` 是 为了能找到master

```
echo "192.168.56.185  apiserver.demo" >> /etc/hosts
ping -c 2 apiserver.demo
```







## 5.3 获得 join命令参数

在 master 节点执行

```shell
# 只在 master 节点执行
kubeadm token create --print-join-command
```

会得到一个字符串



## 5.4 初始化worker

将从master上得到的字符串，复制过来，并执行

```shell
# 只在 worker 节点执行
kubeadm join apiserver.demo:6443 --token m09bzr.pstzjimrusxmzlw5     --discovery-token-ca-cert-hash sha256:faa9b062bad44f287fcfba8b039255fb60ee112b0d946d064a07b185a1279f5b
```



## 5.5 检查初始化结果

在 master 节点上执行

```shell
# 查看worker01 是否加入
kubectl get nodes
```

![alt](imgs/k8s-install-1-getnode.png)

```shell
# 查看worker01 已经加入
watch kubectl get pod -n kube-system -o wide
```



![alt](imgs/k8s-install-1-marster-Running2.png)



## 5.6 移除节点(可选)

在准备移除的 worker 节点上执行

```shell
# 只在 worker 节点执行
kubeadm reset
```

在 master 节点 demo-master-a-1 上执行

```shell
# 只在 master 节点执行
kubectl delete node demo-worker-x-x
```

- 将 demo-worker-x-x 替换为要移除的 worker 节点的名字
- worker 节点的名字可以通过在节点 demo-master-a-1 上执行 kubectl get nodes 命令获得



# 第六步. 安装dashboard

* [官方说明地址](https://kubernetes.io/docs/tasks/access-application-cluster/web-ui-dashboard/)
* [Kubernetes Dashboard的安装与坑](https://www.jianshu.com/p/c6d560d12d50)

```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta4/aio/deploy/recommended.yaml
```







## 参考文档

* 安装相关
  * [Kubernetes的几种主流部署方式01-minikube部署](https://segmentfault.com/a/1190000018607114)
  * [最简单的kubernetes入门实验教程](https://www.jianshu.com/p/f4c2104ba90a)
    - 使用了kubeadm一步安装
  * [Kubernetes中文社区](http://docs.kubernetes.org.cn/)
  * [Kubernetes 安装文档推荐](https://www.kubernetes.org.cn/5650.html)
  * [10分钟搭建Kubernetes容器集群平台（kubeadm）](https://blog.51cto.com/lizhenliang/2296100?tdsourcetag=s_pcqq_aiomsg)
* 基础知识
  * [Kubernetes中文社区 | 中文文档](http://docs.kubernetes.org.cn/)
  * [使用 Docker Alpine 镜像安装 nginx](https://www.cnblogs.com/klvchen/p/11015267.html)

