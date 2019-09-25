# Kubernetes 安装



* [单机版安装](#单机版安装)
* [Kubeadm集群安装](kubernetes-kubeadm.md)
* [二进制集群安装](kubernetes-kubeadm.md)









## 单机版安装

**从一个简单的例子开始**

部署：
1）关闭防火墙服务
systemctl disable firewalld
systemctl stop firewalld
2）安装etcd和Kubernetes软件(会自动安装Docker软件)
\#etcd是高可用、强一致性的服务发现存储仓库
yum install etcd kubernetes
3）按顺序启动所有服务
systemctl start etcd
systemctl start docker
systemctl start kube-apiserver
systemctl start kube-controller-manager
systemctl start kube-scheduler
systemctl start kubelet
systemctl start kube-proxy
一个单机版的Kubernetes集群就安装启动完成