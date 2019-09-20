# 案例



## 1. 环境准备

需要

* k8s环境
* 用来测试的tomcat镜像
  * 这个镜像有3个版本，分别测试版本的更新。
  * 可以从[hub.docker](https://hub.docker.com/r/fanhualei/tomcat-alpine)下载



如果想了解这两步的详细过程，可以参考：

* [安装kubeadm 集群](kubernetes-kubeadm.md)
* [制作测试镜像](docker-tomcat.md)



### 1.1 安装辅助脚本

> [官网提示可以安装shell快捷方式](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

```
# 安装bash-completion
yum install -y bash_completion
# 添加补全脚本
kubectl completion bash >/etc/bash_completion.d/kubectl
```

* [Kubectl常用命令详解](https://blog.csdn.net/weixin_44631350/article/details/89450781)



## 2. 命令行的使用



```shell
#先使用这个命令，看看服务器正常不
kubectl get pod -n kube-system
```





### 2.1 创建Deployment

> 查看一下环境

```shell
kubectl version
kubectl get nodes
kubectl cluster-info
```



> 生成镜像

```shell
kubectl run kube-test --image=fanhualei/tomcat-alpine:v1 --port=8080
kubectl get deployments


kubectl run test06 --image=alpine ash

```



### 2.2 查看Pods和Nodes

- **kubectl get -** 列出资源
- **kubectl describe** - 显示资源的详细信息
- **kubectl logs** - 打印pod中的容器日志
- **kubectl exec** - pod中容器内部执行命令



```shell


kubectl get pods -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}'


kubectl label node apiserver.demo node-role.kubernetes.io/master='master'
kubectl label node worker01.demo  node-role.kubernetes.io/node='node'
kubectl label node worker02.demo  node-role.kubernetes.io/node='node'

```





```shell
kubectl get nodes
kubectl get pods

kubectl describe nodes
kubectl describe pods

kubectl get pods -o wide

kubectl logs
kubectl exec $POD_NAME env
kubectl exec -ti $POD_NAME bash
```



```
Events:
  Type     Reason                  Age                   From                    Message
  ----     ------                  ----                  ----                    -------
  Normal   Scheduled               7m41s                 default-scheduler       Successfully assigned default/test06-c8b98566c-hvpcb to worker01.demo
  Warning  FailedCreatePodSandBox  7m1s                  kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "1b8a095262fd4fe5ab08d86a6f069a74a7bc8f03ffd3aa4a267d1afa3db092e7" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m58s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "2686c8c5fc260ec8201364a60bc0b9caa4055914dc01eb6b62316b8b7f20dfe4" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m57s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "ad4f570cc438fcc039e6f0c8fb279a89905547d6fee019b70ac2f7934be71f93" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m56s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "82a153f17458f87835da6ef60025b7d16d69684c08815404f52de58ec10fc943" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m54s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "241195df960e27b479a2fa5545a23d261252859e0b00a2b77302f05c09fcee0f" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m52s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "3e3f6a349d21fc7146456baa62e42ed39b69f453310c1474d9bd8bb52292d36c" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m51s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "c905b00b2d9d4ade9f0f294379585d2188d1c7ae7b76575bc2081da602fb040a" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m50s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "05854fb8c4795d949815e3b08a4ddd7eccb84a54da69a9b32de6bc0aa222fabb" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Warning  FailedCreatePodSandBox  6m49s                 kubelet, worker01.demo  Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "86885f65995324ea40cbae690fb75e12617d5991e2b472a6ffca422754bce850" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/
  Normal   SandboxChanged          6m43s (x12 over 7m)   kubelet, worker01.demo  Pod sandbox changed, it will be killed and re-created.
  Warning  FailedCreatePodSandBox  2m (x139 over 6m47s)  kubelet, worker01.demo  (combined from similar events): Failed create pod sandbox: rpc error: code = Unknown desc = failed to set up sandbox container "4f382998712a946ca4fed13a207b994b2122177907f4f99af927814dc4a964dc" network for pod "test06-c8b98566c-hvpcb": NetworkPlugin cni failed to set up pod "test06-c8b98566c-hvpcb_default" network: stat /var/lib/calico/nodename: no such file or directory: check that the calico/node container is running and has mounted /var/lib/calico/

```







### 2.3 使用Service暴露应用



### 2.4 实现应用伸缩



### 2.5 实现应用滚动更新





常用shell脚本：

```shell
#每个一秒钟，看看某个http页面的输出
while true;do curl http://ip:port/***.html; sleep 1;done 

```



[Kubernetes中文社区 | 中文文档](http://docs.kubernetes.org.cn/)