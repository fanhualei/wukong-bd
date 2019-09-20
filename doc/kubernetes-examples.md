> 目录

[TOC]

小技巧：[GitHub访问速度慢的一种优化方法](https://baijiahao.baidu.com/s?id=1608100091125662190&wfr=spider&for=pc)





# 1. 环境准备

需要

- k8s环境
- 用来测试的tomcat镜像
  - 这个镜像有3个版本，分别测试版本的更新。
  - 可以从[hub.docker](https://hub.docker.com/r/fanhualei/tomcat-alpine)下载



如果想了解这两步的详细过程，可以参考：

- [安装kubeadm 集群](kubernetes-kubeadm.md)
- [制作测试镜像](docker-tomcat.md)



# 2. Kubernetes 入门



> 查看一下环境

```shell
kubectl version
kubectl get nodes
kubectl cluster-info
#先使用这个命令，看看服务器正常不
kubectl get pod -n kube-system
```



## 2.1 部署第一个应用程序

部署tomcat deployment



### 创建 YAML 文件

创建文件 mytomcat-deployment.yaml，内容如下：

```yaml
#与k8s集群版本有关，使用 kubectl api-versions 即可查看当前集群支持的版本
apiVersion: apps/v1	
kind: Deployment	#该配置的类型，Deployment
metadata:	        #译名为元数据，即 Deployment 的一些基本属性和信息
  name: mytomcat-deployment	#Deployment 的名称
  labels:	    #标签，可以灵活定位一个或多个资源，其中key和value均可自定义，可以定义多组
    app: mytomcat #为该Deployment设置key为app，value为mytomcat的标签
spec:	        #这是关于该Deployment描述，可以理解为你期待该Deployment在k8s中如何使用
  replicas: 1	#使用该Deployment创建一个应用程序实例
  selector:	    #标签选择器，与上面的标签共同作用，目前不需要理解
    matchLabels: #选择包含标签app:nginx的资源
      app: mytomcat
  template:	    #这是选择或创建的Pod的模板
    metadata:	#Pod的元数据
      labels:	#Pod的标签，上面的selector即选择包含标签app:mytomcat的Pod
        app: mytomcat
    spec:	    #期望Pod实现的功能（即在pod中部署）
      containers:	#生成container，与docker中的container是同一种
      - name: mytomcat	#container的名称
        image: fanhualei/tomcat-alpine:v1	#使用镜像创建container，
        command: ['tomcat']
        args: ['run']
```





### 应用 YAML 文件

```shell
kubectl apply -f mytomcat-deployment.yaml
```

如果删除也可以使用

```shell
kubectl delete -f mytomcat-deployment.yaml
```



### 查看部署结果

```shell
# 查看 Deployment
kubectl get deployments -o wide

# 查看 Pod
kubectl get pods  -o wide
```



## 2.2 查看 Pods / Nodes

> 目标

- 了解Pods（容器组）
- 了解Nodes（节点）
  - 集群中的计算机，可以是虚拟机或物理机
  - 每个 Node（节点）都由 master 管理
  - 一个 Node（节点）可以有多个Pod（容器组）
  - master 会根据每个 Node（节点）上可用资源，自动调度 Pod（容器组）到最佳的 Node（节点）上。
- 排查故障



### 基本用法

**kubectl get** - 显示资源列表

```shell
# kubectl get 资源类型

#获取类型为Deployment的资源列表
kubectl get deployments -o wide

#获取类型为Pod的资源列表
kubectl get pods -o wide

#获取类型为Node的资源列表
kubectl get nodes -o wide
```



**kubectl describe** - 显示有关资源的详细信息

```shell
# kubectl describe 资源类型 资源名称

#查看Pod信息 名字要替换掉
kubectl describe pod mytomcat-deployment-85b994d8f5-7b66f	

#查看Deployment的信息
kubectl describe deployment mytomcat-deployment	
```



**kubectl logs** - 查看pod中的容器的打印日志（和命令docker logs 类似）

```shell
# kubectl logs Pod名称

kubectl logs mytomcat-deployment-85b994d8f5-7b66f
```



**kubectl exec** - 在pod中的容器环境内执行命令(和命令docker exec 类似)

kubectl exec -it mytomcat-deployment-85b994d8f5-7b66f /bin/ash



### 高级用法

`-o go-template --template` 控制显示的内容

`-l app=mytomcat` 过滤内容

```shell
kubectl get pods -o go-template --template '{{range.items}}{{.metadata.name}}{{"\n"}}{{end}}' -l app=mytomcat
```





## 2.3 公布应用程序

> 目的

- 了解 Service（服务）
- 了解 Labels（标签）和 LabelSelector（标签选择器）与 Service（服务）的关系
- 在集群中，通过 Service（服务）向外公布应用程序



### Service（服务）概述

Service是一个抽象层，它通过 LabelSelector 选择了一组 Pod（容器组），把这些 Pod 的指定端口公布到到集群外部，并支持负载均衡和服务发现。

在创建Service的时候，通过设置配置文件中的 spec.type 字段的值，可以以不同方式向外部暴露应用程序：

- **ClusterIP**（默认）

  在群集中的内部IP上公布服务，这种方式的 Service（服务）只在集群内部可以访问到

- **NodePort**

  使用 NAT 在集群中每个的同一端口上公布服务。这种方式下，可以通过访问集群中任意节点+端口号的方式访问服务 `<NodeIP>:<NodePort>`。此时 ClusterIP 的访问方式仍然可用。

- **LoadBalancer**

  在云环境中（需要云供应商可以支持）创建一个集群外部的负载均衡器，并为使用该负载均衡器的 IP 地址作为服务的访问地址。此时 ClusterIP 和 NodePort 的访问方式仍然可用。



### 创建文件 mytomcat-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mytomcat-service	#Service 的名称
  labels:     	#Service 自己的标签
    app: mytomcat	#为该 Service 设置 key 为 app，value 为 mytomcat 的标签
spec:	    #这是关于该 Service 的定义，描述了 Service 如何选择 Pod，如何被访问
  selector:	    #标签选择器
    app: mytomcat	#选择包含标签 app:mytomcat 的 Pod
  ports:
  - name: mytomcat-port	#端口的名字
    protocol: TCP	    #协议类型 TCP/UDP
    port: 80	        #集群内的其他容器组可通过 80 端口访问 Service
    nodePort: 32600     #通过任意节点的 32600 端口访问 Service
    targetPort: 8080	#将请求转发到匹配 Pod 的 8080 端口
  type: NodePort	    #Serive的类型，ClusterIP/NodePort/LoaderBalancer
```



### 执行命令

```shell
kubectl apply -f mytomcat-service.yaml
```



### 检查执行结果

```shell
kubectl get services -o wide

# 也可以查看系统的组件
kubectl get services -n kube-system  -o wide
```



### 访问服务



> 在node层面上访问

```shell
# 任意节点的 IP 包含master
# curl <任意节点的 IP>:32600
curl 192.168.1.185:32600

```

如果您的集群在云上，您可能通过云服务商的安全组开放 32600 端口的访问

![alt](imgs/kube-ex-server-show.png)

> 集群内部的其他pod，可以通过`servername:端口`来访问这个服务

登录到任意一个pod

```shell
# 登录到任意主机，可以过名字来访问
wget  -q -O -  mytomcat-service
```

![alt](imgs/k8s-ex-get-service.png)





## 2.4 伸缩应用程序

以前部署了一个mytomcat的pod，但是当流量来的时候，可以快速生成多个服务，并做成负载均衡。



### 修改 mytomcat-deployment.yaml 文件

```shell
# 将副本数修改成
replicas: 3
```



### 执行命令

```shell
kubectl apply -f mytomcat-deployment.yaml
```



### 查看结果

```
watch  kubectl get pods -o wide
```

> 查看负载均衡的结果

```
while true;do curl http://192.168.1.185:32600; sleep 1;done
```

![alt](imgs/k8s-service-cluser-show.png)



## 2.5 执行滚动更新

`fanhualei/tomcat-alpine` 从`v1`更新到了`v2`，如何将生成的pod进行更新。 如果出现错误，如何回滚。如何只更新一半，如何分好几天分配的更新。



### 修改 mytomcat-deployment.yaml 文件

```shell
#使用镜像fanhualei/tomcat-alpine:v2替换原来的fanhualei/tomcat-alpine:v1
image: fanhualei/tomcat-alpine:v2   
```



### 执行命令

```shell
kubectl apply -f mytomcat-deployment.yaml
```



### 查看结果

```shell
# 循环输出
while true;do curl http://192.168.1.185:32600; sleep 1;done

# 查看当前状态：这行命令没有啥作用
watch kubectl rollout status deployment/mytomcat-deployment

# 查看历史
kubectl rollout history deployment/mytomcat-deployment

#回滚到上个版本，或指定版本
kubectl rollout undo deployment/mytomcat-deployment

# kubectl rollout undo deployment/demo --to-revision=2
```









# 参考文档

* [Kubernetes 免费中文教程--推荐](https://kuboard.cn/learning/)

* [Kubernetes中文社区 | 中文文档](http://docs.kubernetes.org.cn/)