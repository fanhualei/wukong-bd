# kubernetes 代码示例

* github上有官方给的例子
  * https://github.com/kubernetes/examples







# 1. Pod





# 2. Volume



## 2.1 emptyDir

同一容器组中的不同容器都可以对该目录执行读写操作，并且共享其中的数据。

[更多内容，请参考](kubernetes-detail-volume.md/#1.2.1 emptyDir )

> 建立一个目录

```shell
mkdir pod-v-emptydir
cd pod-v-emptydir
vi mypod.yaml
```



> 定义一个文件: mypod.yaml

```yaml
apiVersion: v1 
kind: Pod
metadata:
  name: pod-emptydir
  namespace: default
  labels:
    app: myapp
    use: volumen-emptDir
spec:
  #定义一个http容器
  containers:
  - name: hpptd  
    image: nginx:alpine
    ports:
    - name: http
      containerPort: 80  
    volumeMounts:
    - name: html
      mountPath: /usr/share/nginx/html  #绑定到nginx
  #定义一个应用类
  - name: busybox
    image: busybox
    volumeMounts:
    - name: html
      mountPath: /data/  #绑定的目录可以不同名字
    command:
    - "/bin/sh"
    - "-c"
    - "while true; do echo $(date)>> /data/index.html; sleep 2; done"
  #定义一个存储卷    
  volumes:
  - name: html   #这个名字会被其他引用
    emptyDir: {}
```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get pods/pod-emptydir -o wide

# 查看内容
curl  10.100.1.231

# 删除
kubectl delete -f mypod.yaml
```



> 常用的调试脚本

```shell
# 查看帮助
kubectl explain pods.spec.volumes.emptydir
```



## 2.2 hostPath

将**所在节点**的文件系统上某一个文件或文件夹挂载进容器组（容器）。

绝大多数容器组并不需要使用 hostPath 数据卷，但是要监控宿主机的状态时，特别有用：

- 某容器需要访问 Docker，可使用 hostPath 挂载宿主节点的 /var/lib/docker
- 在容器中运行 cAdvisor，使用 hostPath 挂载宿主节点的 /sys

[更多内容，请参考](kubernetes-detail-volume.md/#1.2.2 hostPath)



cAdvisor在docker上的安装方法

```shell
sudo docker run \
  --volume=/:/rootfs:ro \
  --volume=/var/run:/var/run:ro \
  --volume=/sys:/sys:ro \
  --volume=/var/lib/docker/:/var/lib/docker:ro \
  --volume=/dev/disk/:/dev/disk:ro \
  --publish=8080:8080 \
  --detach=true \
  --name=cadvisor \
  google/cadvisor:latest
```





> 建立一个目录

```shell
mkdir pod-v-hostpath
cd pod-v-hostpath
vi mypod.yaml
```



> 定义一个文件: mypod.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - image: nginx:alpine
    name: test-container
    volumeMounts:
    - mountPath: /test-pd
      name: test-volume
  volumes:
  - name: test-volume
    hostPath:
      # directory location on host
      path: /root
      # this field is optional
      type: Directory
```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录，有时候会很奇怪，跟本地的不一样，因为这个机器可能不是本地机器
kubectl exec -it test-pd /bin/sh
> ls /test-pd

# 删除
kubectl delete -f mypod.yaml
```



## 2.3 gitRepo



gitRepo卷类型已弃用。要为容器提供git存储库，[请将EmptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)安装到使用git克隆存储库的[InitContainer中](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)，然后将[EmptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)安装到Pod的容器中。



## 2.4 nfs 网络

- 可以在加载 NFS 数据卷前就在其中准备好数据；
- 可以在不同容器组之间共享数据；
- 可以被多个容器组加载并同时读写；
- 容器组被移除时，将仅仅 umount（卸载）NFS 数据卷，NFS 中的数据仍将被保留。



前提假设nfs网络已经安装，可以看一下[nfs网络安装](nfs.md)

假设已经安装完毕 	



> 建立一个目录

```shell
mkdir pod-v-nfs
cd pod-v-nfs
vi mypod.yaml
```



> 定义一个文件: mypod.yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - image: alpine
    name: test-container
    command:
    - "/bin/sh"
    - "-c"
    - "while true; do echo heloo world; sleep 3; done"
    
    volumeMounts:
    - mountPath: /test-pd
      name: nfs-storage
  volumes:
    - name: nfs-storage
      nfs:
        server: 192.168.1.185
        path: "/"   # 这个目录必须有，不然就建立不了容器
```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录，有时候会很奇怪，跟本地的不一样，因为这个机器可能不是本地机器
kubectl exec -it test-pd /bin/sh
> ls /test-pd

# 删除
kubectl delete -f mypod.yaml
```



## 2.5 secret

Kubemetes提供了Secret来处理敏感数据，比如密码、Token和密钥，相比于直接将敏感数据配置在Pod的定义或者镜像中，Secret提供了更加安全的机制（Base64加密），防止数据泄露。Secret的创建是独立于Pod的，以数据卷的形式挂载到Pod中，Secret的数据将以文件的形式保存，容器通过读取文件可以获取需要的数据。





> 建立一个目录

```shell
mkdir pod-v-secret
cd pod-v-secret
vi mypod.yaml
```



> 定义一个文件: mypod.yaml



参考文档[Kubernetes对象之Secret](https://www.jianshu.com/p/958f406ec071)

```shell
# 分别得到这两个数据
echo -n 'admin' | base64
# YWRtaW4=

echo -n '123456' | base64
# MTIzNDU2
```





```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysecret
type: Opaque
data:
  username: YWRtaW4=  #上面生成的base64
  password: MTIzNDU2 #上面生成的base64
---


apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - image: alpine
    name: test-container
    command:
    - "/bin/sh"
    - "-c"
    - "while true; do echo heloo world; sleep 3; done"
    
    volumeMounts:
    - mountPath: /test-pd
      name: secret-storage
      readOnly: true
    - mountPath: /test-pd2
      name: secret-one-key
      readOnly: true      
  volumes:
    - name: secret-storage
      secret:
        secretName: mysecret
    - name: secret-one-key  # 也可以单独挂载一个
      secret:
        secretName: mysecret
        items:
        - key: username
          path: myusername/    #这个必须指定,可以写成与key一样的名称，这里为了做测试，写了不同的名字
```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> ls /test-pd
> cat /test-pd/password
> cat /test-pd2/myusername    

# 删除
kubectl delete -f mypod.yaml
```



## 2.6. ConfigMap

​	





# 3. Secret

参考文档[Kubernetes对象之Secret](https://www.jianshu.com/p/958f406ec071)

ecret对象与ConfigMap对象类似，但它主要用于存储以下敏感信息，例如密码，OAuth token和SSH key等等。将这些信息存储在secret中，和直接存储在Pod的定义中，或Docker镜像定义中相比，更加安全和灵活。

Secret有三种类型：

- Opaque：使用base64编码存储信息，可以通过`base64 --decode`解码获得原始数据，因此安全性弱。
- kubernetes.io/dockerconfigjson：用于存储docker registry的认证信息。
- kubernetes.io/service-account-token：用于被 serviceaccount 引用。serviceaccout 创建时 Kubernetes 会默认创建对应的 secret。Pod 如果使用了 serviceaccount，对应的 secret 会自动挂载到 Pod 的 /run/secrets/kubernetes.io/serviceaccount 目录中。



## 3.1 Secret的使用

当前只使用：type: Opaque 的创建模式，具体可以看下面的内容





## 3.2 Secret的使用

创建好Secret之后，可以通过两种方式使用：

- 以Volume方式，见[2.5 secret](#2.5 secret)
- 以环境变量方式



### 3.2.1 以环境变量方式

> 建立一个目录

```shell
mkdir secret-env
cd secret-env
vi mypod.yaml
```



> 定义一个文件: mypod.yaml



参考文档[Kubernetes对象之Secret](https://www.jianshu.com/p/958f406ec071)

```shell
# 分别得到这两个数据
echo -n 'admin' | base64
# YWRtaW4=

echo -n '123456' | base64
# MTIzNDU2
```





```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mysecret
type: Opaque
data:
  username: YWRtaW4=  #上面生成的base64
  password: MTIzNDU2 #上面生成的base64
---

apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - image: alpine
    name: test-container
    command:
    - "/bin/sh"
    - "-c"
    - "while true; do echo heloo world; sleep 3; done"
    
    env:
    - name: SECRET_USERNAME
      valueFrom:
        secretKeyRef:
          name: mysecret
          key: username
    - name: SECRET_PASSWORD
      valueFrom:
        secretKeyRef:
          name: mysecret
          key: password
```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> echo $SECRET_USERNAME
> echo $SECRET_PASSWORD  

# 删除
kubectl delete -f mypod.yaml
```



### 3.2.2 以Volume方式

见[2.5 secret](#2.5 secret)



# 4. ConfigMap

ConfigMap顾名思义，是用于保存配置数据的键值对，可以用来保存单个属性，也可以保存配置文件。

参考文档[Kubernetes对象之ConfigMap](https://www.jianshu.com/p/cf8705a93c6b)



## 4.1 ConfigMap的创建

有以下方法：

* 从key-value字符串创建
* 从env文件创建
* 从目录创建
* 根据yaml描述文件创建



### 4.1.1 从key-value字符串创建(不推荐)

```shell
# 创建
kubectl create configmap my-config --from-literal=key1=/ddd/dd/values1 --from-literal=key2=values2

# 显示
kubectl get configmap my-config -o go-template='{{.data}}'

kubectl describe configmap my-config

# 删除
kubectl delete configmap my-config
```



### 4.1.2 从env文件创建

适合遗留的老系统

```shell
#模拟一个文件
echo -e "a=b\nc=d" | tee config.env

#创建
kubectl create configmap my-config --from-env-file=config.env

# 显示
kubectl get configmap my-config -o go-template='{{.data}}'

# 删除
kubectl delete configmap my-config
rm -f config.env
```



### 4.1.3 从目录创建

适合内容非常多的情况

```shell
#模拟目录
mkdir config
echo 123456789 >config/a
echo qqqqqqqqq >config/b

#创建
kubectl create configmap my-config --from-file=config

# 显示
kubectl get configmap my-config -o go-template='{{.data}}'

# 删除
kubectl delete configmap my-config
rm -rf config
```



### 4.1.4 根据yaml描述文件创建



> 创建文件

```
vi config.yaml
```



```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-config
data:
  special.how: very
  special.type: charm123546/test/12234
```



> 使用

```shell
#创建
kubectl create  -f  config.yaml

# 显示
kubectl get -f  config.yaml -o go-template='{{.data}}'

# 删除
kubectl delete -f  config.yaml
rm -rf config.yaml
```





## 4.2 ConfigMap的使用

