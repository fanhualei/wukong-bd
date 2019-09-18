# 实用案例



## 1. 制作实验镜像

做一个tomcat镜像，并有3个版本，分别测试版本的更新。



### 1.1 注册docker账户

在`https://hub.docker.com/`上注册账户，例如`fanhualei`      密码 `goodmorning`



### 1.2 制作镜像

找一个小一点的镜像，可以使用alpline来制作`https://wiki.alpinelinux.org/wiki/Tomcat`







```shell
kubectl version

kubectl get nodes

kubectl run kubernetes-bootcamp --image=gcr.io/google-samples/kubernetes-bootcamp:v1 --port=8080

kubectl get deployments
```











常用shell脚本：

```shell
#每个一秒钟，看看某个http页面的输出
while true;do curl http://ip:port/***.html; sleep 1;done 

```

