# kubernetes 代码示例

确保每个例子的代码都可以运行。参考了[马哥kubernetes入门](https://pdf.us/tag/kubernetes/page/3)



[TOC]



* github上有官方给的例子
  * https://github.com/kubernetes/examples



# 基本概念

![alt](imgs/218142549.jpg)





![alt](imgs/0190218170105.jpg)



![alt](imgs/20190218170135.jpg)



# 1. Pod

使用容器标准方式：一个容器仅运行一个进程

[参考网址](https://pdf.us/2019/02/19/2803.html)



## 1.1 Hello Pod



```shell
cd ~ ; mkdir 1-1 ; cd 1-1
vi mypod.yaml
```



> mypod.yaml 详细

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: myapp
    image: alpine
    command: ["/bin/sh"]
    args: ["-c","while true; do sleep 30; done"]
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> exit

# 删除
kubectl delete -f mypod.yaml
```



## 1.2 设置环境变量

应用程序若不支持从环境变量获取配置，可通过 entrypoint 脚本完成环境变量到程序配置文件的同步

向Pod对象中容器传递环境变量有两种方法：env 和 envFrom。envFrom见[8.2 ConfigMap的使用](#8.2 ConfigMap的使用)



```shell
cd ~ ; mkdir 1-2 ; cd 1-2
vi mypod.yaml
```



> mypod.yaml 详细

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: filebeat
    image: ikubernetes/filebeat:5.6.5-alpine
    env:
    - name: REDIS_HOST
      value: db.ilinux.io:6379
    - name: LOG_LEVEL
      value: info
```



```shell

# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> echo REDIS_HOST
> exit

# 删除
kubectl delete -f mypod.yaml
```



## 1.3 共享节点网络

仅需设置 spec.hostNetwork 为 true.

IP地址为节点的IP地址，并且端口也暴漏出来了。



```shell
cd ~ ; mkdir 1-3 ; cd 1-3
vi mypod.yaml
```



> mypod.yaml 详细

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: my-test
    image: fanhualei/tomcat-alpine:v1
  hostNetwork: true
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 看那个节点的ip网页
curl 192.168.1.186:8080

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> ip add
> exit



# 删除
kubectl delete -f mypod.yaml
```



## 1.4 权限和访问控制





```shell
cd ~ ; mkdir 1-4 ; cd 1-4
vi mypod.yaml
```



> mypod.yaml 详细

设定Pod或容器的权限和访问控制.

以uid1000的非特权用户运行容器并禁止权限升级

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: my-test
    image: alpine
    command: ["/bin/sh","-c","sleep 86400"]
    securityContext:
      runAsNonRoot: true
      runAsUser: 1000
      allowPrivilegeEscalation: false
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml


# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> ps aux
> exit



# 删除
kubectl delete -f mypod.yaml
```





## 1.5 设置标签



```shell
cd ~ ; mkdir 1-5 ; cd 1-5
vi mypod.yaml
```



> mypod.yaml 详细

资源标签 labels 位于 **metadata** 下

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
  labels:
    type: pod
    use: web
    speed: fast
spec:
  containers:
  - name: my-test
    image: alpine
    command: ["/bin/sh","-c","sleep 86400"]
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 查看标签
kubectl get pods --show-labels

# 看看启动了没有
kubectl get -f mypod.yaml -o wide  --show-labels

# 查看详细信息
kubectl describe -f mypod.yaml


# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> exit



# 删除
kubectl delete -f mypod.yaml
```



> 标签选择器高级用法

**原则：**
同时指定的多个选择器是 AND 与 操作
使用空值标签，表示选择全部
空的标签选择器，表示没有资源被选择



**基于集合关系的选择器：**
key in (val1,val2,...)    在集合中即可
key notin (val1,val2,...)   不在集合中
key  所有存在此键名标签的资源
!key 所有不存在此键名标签的资源



```shell
#标签选择器 Label Selector
kubectl get pods -l use=web
kubectl get pods -l use!=web
kubectl get pods -l use==web
kubectl get pods -l use=web,name=ma   # 多个选择器

kubectl get pods -l "type in (pod,svc)"
kubectl get pods -l "type notin  (pod,svc)"
kubectl get pods -l "type"  #所有存在此键名标签的资源
kubectl get pods -l 'type in (pod,svc),!name' 
```



## 1.6 标签选择器

让其他对象找到Pod

Service/Deployment/ReplicaSet等关联到Pod对象，通过在spec字段嵌套selector字段，通过matchLabels指定标签选择器。

* matchLabels 直接用键值对
* matchExpressions 基于表达式指定标签选择器列表

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mysvc
spec:
  selector:
    matchLabels:
      app: test
    matchExpressions:
    - { key: tier, operator: In, values: [cache,foo]} #In和NotIn
    - { key: environment, opetator: Exists,values:}  #Exists和DostNotExist时values必须为空  
```



## 1.7 发布到特定节点中

可以指定Pod发布到特定的宿主机器上，例如这些机器上有ssd硬盘

### 第一步、给节点设置标记

```shell
kubectl get nodes --show-labels
kubectl label nodes kube-node1 disktype=ssd  # 为节点添加标签
kubectl get nodes -L disktype
```



### 第二步、选择节点

让Pod在选定节点上运行,实现节点亲和性调度，**spec.nodeSelector**
绑定到特定节点还有一种方法 使用 **spec.nodeName（不常用，这种写的太死了）**



```shell
cd ~ ; mkdir 1-7 ; cd 1-7
vi mypod.yaml
```



> mypod.yaml 详细

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  nodeSelector:
    matchLabels:
      disktype: sdd
  #nodeName: 节点名称  
  containers:
  - name: myapp
    image: alpine
    command: ["/bin/sh"]
    args: ["-c","while true; do sleep 30; done"]
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> exit

# 删除
kubectl delete -f mypod.yaml
```



## 1.8 生命周期

![alt](imgs/0190221152827.jpg)



### 1.8.1 初始化容器

主容器启动之前要运行的容器，常用于执行一些预置操作
初始化容器必须运行完成至结束，每个初始化容器都必须按定义顺序串行运行

例如gitRepo这个取消了，可以通过初始化容器来实现类似的功能



```shell
cd ~ ; mkdir 1-8-1 ; cd 1-8-1
vi mypod.yaml
```



> mypod.yaml 详细

spec.initContainers 定义

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: myapp
    image: alpine
    command: ["/bin/sh"]
    args: ["-c","while true; do sleep 300; done"]
  initContainers:  
  - name: init-app
    image: alpine
    command: ['sh','-c','sleep 20']  
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> exit

# 删除
kubectl delete -f mypod.yaml
```



### 1.8.2 钩子函数

**如何设置**

* kubectl explain pods.spec.containers.lifecycle.postStart.exec

* postStart，
  * 容器创建完成后立即运行，不保证一定会于容器中ENTRYPOINT之前运行
* preStop，
  * 容器终止操作之前立即运行，在其完成前会阻塞删除容器的操作调用

**钩子处理器实现方式**：

* Exec，在钩子事件触发时，直接在当前容器中运行由用户定义的命令
* HTTP，在当前容器中向某Url发起HTTP请求





```shell
cd ~ ; mkdir 1-8-2 ; cd 1-8-2
vi mypod.yaml
```



> mypod.yaml 详细

spec.initContainers 定义

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: myapp
    image: alpine
    command: ["/bin/sh"]
    args: ["-c","while true; do sleep 300; done"]
  initContainers:  
  - name: init-app
    image: alpine
    command: ['sh','-c','sleep 20']  
    
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
spec:
  containers:
  - name: myapp
    image: alpine
    lifecycle:
      postStart:
        exec:
          command: ["/bin/sh","-c","echo 'lifecycle hooks handler' > /home/index.html"]    
```



```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

kubectl exec lifecycle-demo -- cat /home/index.html

# 查看生成的目录
kubectl exec -it test-pd /bin/sh
> exit

# 删除
kubectl delete -f mypod.yaml
```



### 1.8.3 容器探测

**设置点**

- pods.spec.containers.livenessProbe
- pods.spec.containers.readinessProbe

**探测方法**

- ExecAction，在容器内执行命令，检测状态码是否为0
- TCPSocketAction，与容器的某tcp端口尝试连接，端口打开即为正常
- HTTPGetAction，向容器某端口发起get请求，响应码为2xx 3xx即为正常

**处理逻辑**

* 存活性检测 livenessProbe
  * 判定容器是否为Running状态，不通过则杀死容器，未定义存活性检测默认为 Success
* 就绪性检测 readinessProbe
  * 判断容器是否就绪并可对外提供服务，不通过则不会添加到Service对象端点列表中



### 1.8.4 容器重启策略

> pods.spec.restartPolicy

- Always，默认，总是重启
- OnFailure，出错时重启
- Never，不重启

`反复重启的延迟时间：10、20、40、80、160、300秒逐次延长，最大300秒`



### 1.8.5 设置容器终止宽限期

系统强制删除操作宽限期倒计时(30s)启动，发送TERM信号到Pod中每个容器的主进程，倒计时结束，发送KILL信号
--grace-period=<seconds> 自定义宽限期时长，默认30秒





# 2. Controller















# 3. Service







# 4. Volume



## 4.1 局部存储



### 4.1.1 emptyDir

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



### 4.1.2 hostPath

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



### 4.1.3 gitRepo



gitRepo卷类型已弃用。要为容器提供git存储库，[请将EmptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)安装到使用git克隆存储库的[InitContainer中](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)，然后将[EmptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir)安装到Pod的容器中。



## 4.2 持久化存储



### 4.2.1 nfs 网络

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
        server: 192.169.1.185
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



## 4.3 配置型存储

### 4.3.1 secret

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



### 4.3.2 configMap

​	建立一个目录

```shell
cd ~ ; mkdir pod-v-config ; cd pod-v-config ; vi mypod.yaml

```





```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-config
data:
  special.how: very
  special.type: charm123546/test/12234
  
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
    - name: config-volume
      mountPath: /root/config  
  restartPolicy: Never  
  volumes:
    - name: config-volume
      configMap:
        name: my-config
      
   # 也可以单独提取一个key,具体看2.5 secret的例子，有类似的
   # 在一般情况下 configmap 挂载文件时，会先覆盖掉挂载目录
   # 如果想不对原来的文件夹下的文件造成覆盖，只是将 configmap 中的每个 key，按照文件的方式挂载到目录下，可以使用 subpath 参数。（但是这种情况不常用）
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
> cd /root/config 
> cat special.how ; echo \n ; cat special.type ; echo \n
> exit

# 删除
kubectl delete -f mypod.yaml
```



# 5. PVC和PV

为什么又PV和PVC这个概念呢？ 上面学习中，可以看到用户要手工关联Volume，这样就强关联了。 实际上管理员可以定义一些不同大小或读取速度的空间，然后告诉程序员那些空间可以选择，这样程序员就不用知道这些空间到底背后实现的机制，这样就完全解耦合了。

* PersistentVolume缩写=PV

* PersistentVolumeClaim缩写=PVC

![alt](imgs/k8s-pvc-pv.png)

>操作步骤

* 存储管理员：创建了很多存储空间
* K8s管理员：创建PV与存储进行关联
* 程序员：创建PVC去申请PV，如果申请成功，那么PVC与PV绑定。



## 5.1 创建存储空间

> 创建目录

```shell
mkdir -p /home/data/v1
mkdir -p /home/data/v2
mkdir -p /home/data/v3
mkdir -p /home/data/v4
mkdir -p /home/data/v5
# 分配权限
chown nfsnobody.nfsnobody /home/data/v1
chown nfsnobody.nfsnobody /home/data/v2
chown nfsnobody.nfsnobody /home/data/v3
chown nfsnobody.nfsnobody /home/data/v4
chown nfsnobody.nfsnobody /home/data/v5
vi /etc/exports
```



> exports文件

```
/home/data/v1/ 192.169.1.0/24(rw,sync)
/home/data/v2/ 192.169.1.0/24(rw,sync)
/home/data/v3/ 192.169.1.0/24(rw,sync)
/home/data/v4/ 192.169.1.0/24(rw,sync)
/home/data/v5/ 192.169.1.0/24(rw,sync)
```



> 让共享目录生效

```shell
# 使定义生效
exportfs
# 查看生效结果
showmount -e
```



## 5.2 定义PV

k8s管理员来做这件事。`kubectl explain pv` 来查看帮助

关键顶一点：

* 访问模型
* 空间大小
* 回收机制
  * Retain 保留



下面定义一个nfs格式的pv:  `pv-demo.yaml`

```
cd ~ ; mkdir pod-v-pvc ;cd pod-v-pvc ; vi pv-demo.yaml
```



```yaml
# PV 不用名称空间，因为集群中通用
apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv001
  labels:
    name: pv001
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 1Gi
  #当前支持的回收策略有：“Retain”（保留-默认）, “Recycle”（重复利用）, “Delete”（删除）. 
  #建议使用“Retain”（保留） 这样数据不会丢失，就是要重复使用这个，需要手工删除pv
  #“Recycle”（重复利用）：一旦删除pod ,那么数据会被清空
  #“Delete”（删除） pv状态设置成不可用，但是数据还被保留，用这个也相对安全，数据还是最重要的。
  #persistentVolumeReclaimPolicy: Recycle
  nfs:
    path: /home/data/v1/
    server: 192.169.1.185
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv002
  labels:
    name: pv002
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v2/
    server: 192.169.1.185
    
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv003
  labels:
    name: pv003
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v3/
    server: 192.169.1.185 
```



```shell
kubectl apply -f pv-demo.yaml

# 查看得到pv
kubectl get pv
```



## 5.3 定义PVC与Pod



> 建立yaml文件

```shell
vi mypod.yaml
```



> 定义一个文件: mypod.yaml

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mypvc
spec:
  accessModes: ["ReadWriteMany"] # 必须使PV要求的子集
  resources:          #资源要求
    requests:
      storage: 800Mi
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
    - name: pv-storage
      mountPath: /test-pd
  volumes:
    - name: pv-storage
      persistentVolumeClaim:
        claimName: mypvc

```



> 进行测试

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 查看pv是否绑定了
kubectl get pv

# 查看pvc是否绑定了
kubectl get pvc

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看详细信息
kubectl describe -f mypod.yaml

# 查看生成的目录，有时候会很奇怪，跟本地的不一样，因为这个机器可能不是本地机器
kubectl exec -it test-pd /bin/sh
> ls /test-pd

# 删除
kubectl delete -f mypod.yaml

# 将pv也给删除了
kubectl delete -f pv-demo.yaml
```

![alt](imgs/k8s-pvc-test-result.png)





## 5.4 回收策略

* pod 被删除了，PVC还在。
* PVC 被删除了呢 ?
  * 取决PV的回收策略
* 我手工，可以把PV删除呢？
  * 在新的版本不能删除





# 6. StorageClass

上一节提到PV与PVC的解决模式，这种模式会让管理员很烦，因为他们要提前建立存储空间，有没有自动建立的机制呢？

前提条件：

* 存储设备，有API接口，来进行输出存储划分。这样才可以自动创建PV 。 NFS不支持这种功能。
* glass存储集群







# 7. Secret

参考文档[Kubernetes对象之Secret](https://www.jianshu.com/p/958f406ec071)

ecret对象与ConfigMap对象类似，但它主要用于存储以下敏感信息，例如密码，OAuth token和SSH key等等。将这些信息存储在secret中，和直接存储在Pod的定义中，或Docker镜像定义中相比，更加安全和灵活。

Secret有三种类型：

- Opaque：使用base64编码存储信息，可以通过`base64 --decode`解码获得原始数据，因此安全性弱。
- kubernetes.io/dockerconfigjson：用于存储docker registry的认证信息。
- kubernetes.io/service-account-token：用于被 serviceaccount 引用。serviceaccout 创建时 Kubernetes 会默认创建对应的 secret。Pod 如果使用了 serviceaccount，对应的 secret 会自动挂载到 Pod 的 /run/secrets/kubernetes.io/serviceaccount 目录中。



## 7.1 Secret的创建 

当前只使用：type: Opaque 的创建模式，具体可以看下面的内容





## 7.2 Secret的使用

创建好Secret之后，可以通过两种方式使用：

- 以Volume方式，见[2.3.1 secret](#2.3.1 secret)
- 以环境变量方式



### 7.2.1 以环境变量方式

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



### 7.2.2 以Volume方式

见[2.3.1 secret](#2.3.1 secret)



# 8. ConfigMap

ConfigMap顾名思义，是用于保存配置数据的键值对，可以用来保存单个属性，也可以保存配置文件。

参考文档[Kubernetes对象之ConfigMap](https://www.jianshu.com/p/cf8705a93c6b)



## 8.1 ConfigMap的创建

有以下方法：

* 从key-value字符串创建
* 从env文件创建
* 从目录创建
* 根据yaml描述文件创建



### 8.1.1 从key-value字符串创建(不推荐)

```shell
# 创建
kubectl create configmap my-config --from-literal=key1=/ddd/dd/values1 --from-literal=key2=values2

# 显示
kubectl get configmap my-config -o go-template='{{.data}}'

kubectl describe configmap my-config

# 删除
kubectl delete configmap my-config
```



### 8.1.2 从env文件创建vi

适合遗留的老系统，支持的文件格式很多，可以是`json,yaml,env` 注意文件尾部不要出现空格，不然可能报错。

在vi中，使用`:set invlist` 来显示空格

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



### 8.1.3 从目录创建

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



### 8.1.4 根据yaml描述文件创建



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





## 8.2 ConfigMap的使用

Pod可以通过三种方式来使用ConfigMap，分别为：

- 将ConfigMap中的数据设置为环境变量
- 将ConfigMap中的数据设置为命令行参数
- 使用Volume将ConfigMap作为文件或目录挂载
- configMap内容发生变更时，引用了它的pod都会变更
  - 环境变量传入的，不会改变已经建成的Pod
  - 但是用volume建立的，会立即改变。



### 8.2.1 环境变量方式

> 建立一个目录

```shell
mkdir config-env ; cd config-env
vi mypod.yaml
```





```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-config
data:
  special.how: very
  special.type: charm123546/test/12234
  
---

apiVersion: v1
kind: ConfigMap
metadata:
  name: env-config
data:
  log_level: INFO
  
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
    
    # 下面方式可以key的名字
    env:
    - name: HOW
      valueFrom:
        configMapKeyRef:
          name: my-config
          key: special.how
    - name: TYPE
      valueFrom:
        configMapKeyRef:
          name: my-config
          key: special.type
          
    # 下面方式可以直接从一个大配置文件来引用      
    envFrom:
        - configMapRef:
            name: env-config  
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
> echo $HOW
> echo $TYPE  
> echo $log_level  
> printENV
> exit

# 删除
kubectl delete -f mypod.yaml
```



### 8.2.2 命令行参数

> 建立一个目录

```shell
cd ~ ; mkdir config-cmd ; cd config-cmd ; vi mypod.yaml

```





```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-config
data:
  special.how: very
  special.type: charm123546/test/12234
  
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
    - "echo $(HOW) $(HOW);while true; do echo heloo world; sleep 3; done"
    
    env:
    - name: HOW
      valueFrom:
        configMapKeyRef:
          name: my-config
          key: special.how
    - name: TYPE
      valueFrom:
        configMapKeyRef:
          name: my-config
          key: special.type
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
> echo $HOW :  $TYPE
> exit

# 删除
kubectl delete -f mypod.yaml
```



### 8.2.3 Volume挂载

详细内容见[2.3.2 configMap](#2.3.2 configMap)





# 9. StatefulSet



## 9.1 模拟存储空间

如果[4.1 创建存储空间](#4.1 创建存储空间)做过了，那么这一步可以省略。

自nfs服务器上操作

> 创建目录

```shell
mkdir -p /home/data/v1
mkdir -p /home/data/v2
mkdir -p /home/data/v3
mkdir -p /home/data/v4
mkdir -p /home/data/v5
# 分配权限
chown nfsnobody.nfsnobody /home/data/v1
chown nfsnobody.nfsnobody /home/data/v2
chown nfsnobody.nfsnobody /home/data/v3
chown nfsnobody.nfsnobody /home/data/v4
chown nfsnobody.nfsnobody /home/data/v5
vi /etc/exports
```



> exports文件

```
/home/data/v1/ 192.169.1.0/24(rw,sync)
/home/data/v2/ 192.169.1.0/24(rw,sync)
/home/data/v3/ 192.169.1.0/24(rw,sync)
/home/data/v4/ 192.169.1.0/24(rw,sync)
/home/data/v5/ 192.169.1.0/24(rw,sync)
```



> 让共享目录生效

```shell
# 使定义生效
exportfs
# 查看生效结果
showmount -e
```





## 9.2 定义PV

k8s管理员来做这件事。`kubectl explain pv` 来查看帮助



下面定义一个nfs格式的pv:  `pv-demo.yaml`

```
cd ~ ; mkdir statefulset ;cd statefulset ; vi pv-demo.yaml
```



```yaml
# PV 不用名称空间，因为集群中通用
apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv001
  labels:
    name: pv001
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 1Gi
  #当前支持的回收策略有：“Retain”（保留-默认）, “Recycle”（重复利用）, “Delete”（删除）. 
  #建议使用“Retain”（保留） 这样数据不会丢失，就是要重复使用这个，需要手工删除pv
  #“Recycle”（重复利用）：一旦删除pod ,那么数据会被清空
  #“Delete”（删除） pv状态设置成不可用，但是数据还被保留，用这个也相对安全，数据还是最重要的。
  #persistentVolumeReclaimPolicy: Recycle
  nfs:
    path: /home/data/v1/
    server: 192.169.1.185
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv002
  labels:
    name: pv002
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v2/
    server: 192.169.1.185
    
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv003
  labels:
    name: pv003
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v3/
    server: 192.169.1.185  
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv004
  labels:
    name: pv004
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v4/
    server: 192.169.1.185  
    
---

apiVersion: v1
kind: PersistentVolume
metadata: 
  name: pv005
  labels:
    name: pv005
    speed: fast

spec:
  #单路只读  单路读写ReadWriteOnce 多路读写ReadWriteMany   
  accessModes: ["ReadWriteMany","ReadWriteOnce"]   
  # 存储大小 Ei Pi Ti Gi Mi Ki 
  # 下面定vi义支持1G
  capacity:
    storage: 200Mi
  nfs:
    path: /home/data/v5/
    server: 192.169.1.185      
    
    
```



```shell
kubectl apply -f pv-demo.yaml

# 查看得到pv
kubectl get pv
```



## 9.3 创建StatefulSet

具体步骤如下：

* 定义一个无头service
* 定义一个StatefulSet
* 定义一个存储卷生成模板



```shell
vi mypod.yaml
```



> 定义一个文件: mypod.yaml

```yaml
# 定义一个无头的service ，这才能为每一个pod指定唯一的标识符
apiVersion: v1
kind: Service
metadata:
  name: mysvc
  namespace: default
spec:
  clusterIP: None  # 关键点，将clusterIP设为None
  selector:
    app: test    
---

apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: myset
spec:
  #updateStrategy:
  #  rollingUpdate:
  #    partition: 2   # 只有大于等于2的才进行更新。
  serviceName: mysvc  #选择服务
  replicas: 3
  selector:
    matchLabels:
      app: test
  #pod模板
  template:
    metadata:
      labels:
        app: test
    spec:
      containers:
      - name: mypd
        image: fanhualei/tomcat-alpine:v1 
        command: ['tomcat']
        args: ['run']
        ports:
        - containerPort: 8080
          name: web
        volumeMounts:
        - name: myappdata
          mountPath: /opt/webapp     
  #定义PVC模板        
  volumeClaimTemplates:
  - metadata:
      name: myappdata
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 100Mi


```



> 进行测试

* 查看会不会自动生成PVC
* 看看节点会不会连续
* 每个Pod有自己的名字，并且是可以被解析的。
* 可以扩容缩容，缩减逆序
* 更新
  * partition 定义更新分区，模拟金丝雀发布

```shell
# 生成pod
kubectl apply -f mypod.yaml

# 看看启动了没有
kubectl get -f mypod.yaml -o wide

# 查看pod的情况 pod的名称是myset-0 myset-1
kubectl get pods -o wide

# 查看无头服务的IP地址
dig -t A mysvc.default.svc.cluster.local. @10.97.0.10


#StatefulSet在Headless Service的基础上又为StatefulSet控制的每个Pod副本创建了一个DNS域名，这个域名的格式为：
#$(podname).(headless server name)
#FQDN： $(podname).(headless server name).namespace.svc.cluster.local
# 登录到容器内部
kubectl exec -it myset-0 /bin/sh
> ping -c 2 myset-0.mysvc.default.svc.cluster.local
> ping -c 2 myset-1.mysvc.default.svc.cluster.local
> nslookup myset-0.mysvc.default.svc.cluster.local
> exit

# 查看pvc是否绑定了
kubectl get pvc

# 查看pv是否绑定了
kubectl get pv


# 查看详细信息
kubectl describe -f mypod.yaml

# 删除
kubectl delete -f mypod.yaml

# 删除自动生成PVC
kubectl get pvc
kubectl delete pvc  myappdata-myset-0 myappdata-myset-1

# 将pv也给删除了
kubectl delete -f pv-demo.yaml


```



## 9.4 针对某个节点升级

如果你删除了节点，需要重新制作一下环境。

```shell
# 减少容器，
kubectl patch sts myset -p '{"spec":{"replicas":2}}'

# kubectl get pods -o wide

# 节点虽然少了，但是pv的绑定还在
kubectl get pv -o wide

# 增加容器
kubectl patch sts myset -p '{"spec":{"replicas":4}}'

# 节点有绑定回去了
kubectl get pv -o wide


# 大于3的才更新
kubectl patch sts myset -p  '{"spec":{"updateStrategy":{"rollingUpdate":{"partition":3}}}}'

# 进行升级
kubectl set image sts myset *=fanhualei/tomcat-alpine:v3 

# 改变节点信息，分别查看每个节点的信息
kubectl get pod myset-2 -o yaml | grep image
```





