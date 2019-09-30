# 系统管理-Service Account（推荐）

本文章模拟添加用户与权限。

拟添加的用户，K8s管理员只添加NS管理员

| 用户名        | 权限说明                |
| ------------- | ----------------------- |
| testing-admin | testing命名空间管理员   |
| testing-edit  | testing命名空间编辑人员 |
| testing-view  | testing命名空间浏览人员 |





**不建议使用证书形式的原因：**

- 首先要创建证书，比较麻烦。
- 其次这种用户不能使用dashboard，只能用kubectrl来登录。
- [证书形式的用户管理](kubernetes-admin-usercount.md)





# 1. k8s管理员

假设在服务器上

```shell
mkdir ~/admin-user ; cd ~/admin-user
```



## 1.1 创建命名空间

模拟一个要分配的命名空间`testing`。这多创建了一个pod，用来测试。

```yaml
#预防已经建立过了
rm -f test.yaml;

#建立一个文件
cat>test.yaml<<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: testing
  labels:
    env: testing
---
apiVersion: v1
kind: Pod
metadata:
  name: test-pd
  namespace: testing
spec:
  containers:
  - name: myapp
    image: alpine
    command: ["/bin/sh"]
    args: ["-c","while true; do sleep 3; done"]
    
EOF

#生效
kubectl apply -f test.yaml

#查看是否有这个pod
kubectl get pods -n testing
```



## 1.2 创建NS管理员账户

创建一个Name Space管理员账户，这个账户放在default命名空间中，当然也可以放到其他命名空间中

```shell
kubectl create serviceaccount testing-admin -n testing  

# kubectl delete serviceaccount testing-admin -n testing  
```

注意，创建serviceaccount后，会自动创建一个绑定的 secret ，后面在kubeconfig文件中，会用到该secret中的token





## 1.3 分配基本权限

给命名空间管理员账户授权，在大部分场景下，引用内建`ClusterRole的admin/view/edit` 这几个Role就行

![alt](imgs/k8s-default-role.jpg)




> 进行配置

建立`rbd-admin.yaml`文件

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  creationTimestamp: null
  name: rbd-admin
  namespace: testing
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: admin
subjects:
- kind: ServiceAccount
  name: testing-admin
  namespace: testing
```

执行，并查看结果

```shell
# 进行绑定
kubectl apply -f rbd-admin.yaml

# 查看是否绑定成功的两种方式
kubectl get rolebinding -n testing -o wide
```



> 小技巧

*可以通过命令授权-得到要建立的文件：*

```shell
kubectl create rolebinding rbd-admin --clusterrole=admin --serviceaccount=testing:testing-admin -n testing -o yaml  --dry-run > rbd-admin.yaml
```



## 1.4 分配追加用户权限

NS管理员实际已经有了追加用户的权限，但是为了更方便他分配，需要将`cluster role 的 admin edit view`分配给他。

```shell
vi  ns-admin-grantor.yaml 
```

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: ns-admin-grantor
rules:
- apiGroups:
  - '*'
  resources:
  - clusterroles
  resourceNames:
  - admin
  - edit
  - view
  verbs:
  - list
  - get
  - bind
  
---
#让NS admin 可以访问这几个默认的clusterrole
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: rbd-testing-ns-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: ns-admin-grantor
subjects:
- kind: ServiceAccount
  name: testing-admin
  namespace: testing
```



```shell
 kubectl apply -f ns-admin-grantor.yaml 
 
 kubectl get ClusterRole
 
 kubectl get ClusterRoleBinding
```





## 1.5 将证书给NS管理员

发送的文件有：

| 文件名 | 作用                                                         |
| ------ | ------------------------------------------------------------ |
| ca.crt | 用来登录api的公用CA证书.  在这个目录下：  `/etc/kubernetes/pki` |





## 1.6 其他常用命令

运维常用指令

```shell
# 列出所有的resources 名称
kubectl api-resources

# 读取、列出、监视Pod和Service资源
kubectl explain roles.rules


# 查看系统的集群role
kubectl get  clusterrole 

kubectl get  clusterrole admin -o yaml

kubectl get  clusterrole cluster-admin -o yaml


```





# 2. NS管理员

下面模拟命名空间管理员登录到k8s的过程。



## 2.1 获得自己的Token

通过下面的命令可以获得自己的token

```shell
user_name=testing-admin

sec_name=$(kubectl get -n testing serviceaccounts $user_name --output=jsonpath='{.secrets[*].name}')

#echo $sec_name

#从返回内容中可查到，该secret的token为：
token=$(kubectl get -n testing secrets $sec_name --output=jsonpath='{.data.token}')

#该token是经过base64处理的，需要进行解码处理
echo $token | base64 -d
```



```
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ0ZXN0aW5nIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InRlc3RpbmctYWRtaW4tdG9rZW4tdHMyOHoiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoidGVzdGluZy1hZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjdhZTQ4NWI3LTM0NWQtNGJlYy1iMzE2LWE5Y2Y3NGUyYjJjMCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDp0ZXN0aW5nOnRlc3RpbmctYWRtaW4ifQ.eDIKd1tF0Jht5p9jAET-wYZeV5geUoNIBBsJSdr_xUiU2p9kojr4Bt0OYVFViP87xcfmvu7z9ElJjStlxFiSEGMue7pcKtitYRoTgNc0ZwVFyGec5j0rzI-zu81HRgONuK1t1lRcmwW7mkODi0RRjdU-Waj2XOpRo2AIri4D2IrXoEfMUTcqeFkxuLzAj9GbhBGVPoeJ9zGemdqAtb9QzHRQETpjwRvRDIgKzJO6_w1csLjFj6XNeTVqRzPuqu-XNiI5tvYiNlK5QYG0mPFRN98xHrewsQMSwCHY-G0Rwq7cFI4hvH89-tyXJmTl18owRMjQuHecw5caIFiI_AcK1Q
```





## 2.1 登录Dashboard

> 前提

已经安装了dashboard，[dashboard具体安装方法](kubernetes-kubeadm.md)



> 具体步骤

* 在firefox中输入：https://192.168.1.185:30001
* 选择token登录，并输入管理员给的token
* 在name space框中输入`testing`，然后点击Pod，就能看到新建立的Pod





## 2.1 配置kubectl

NS管理员，需要在自己机器上安装`kubectl`，windows上的客户端不 稳定，所以可以在一个node上做测试。

参考文档：[安装和设置kubectl](https://www.kubernetes.org.cn/installkubectl) ，如何在windows中使用，可以参考[证书形式的用户管理](kubernetes-admin-usercount.md)

具体如下：



### ① 前期准备

```shell
# 看看以前是否已经连接过了
kubectl get pod

# 清空已经有的配置，这里是做测试，在正式环境中不要清空
rm -r ~\.kube
```



### ② 生成conf

将管理员给的ca文件复制到当前目录

以下4步必须执行

```shell
# 1.配置集群信息，存在可省略此步骤，这里一定要使用域名
kubectl config set-cluster kubernetes --embed-certs=true --certificate-authority=ca.crt --server="https://apiserver.demo:6443"

# 2.配置客户端证书及密钥，用户名组名会自动提取
kubectl config set-credentials testing-admin  --token=eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ0ZXN0aW5nIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InRlc3RpbmctYWRtaW4tdG9rZW4tdHMyOHoiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoidGVzdGluZy1hZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjdhZTQ4NWI3LTM0NWQtNGJlYy1iMzE2LWE5Y2Y3NGUyYjJjMCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDp0ZXN0aW5nOnRlc3RpbmctYWRtaW4ifQ.eDIKd1tF0Jht5p9jAET-wYZeV5geUoNIBBsJSdr_xUiU2p9kojr4Bt0OYVFViP87xcfmvu7z9ElJjStlxFiSEGMue7pcKtitYRoTgNc0ZwVFyGec5j0rzI-zu81HRgONuK1t1lRcmwW7mkODi0RRjdU-Waj2XOpRo2AIri4D2IrXoEfMUTcqeFkxuLzAj9GbhBGVPoeJ9zGemdqAtb9QzHRQETpjwRvRDIgKzJO6_w1csLjFj6XNeTVqRzPuqu-XNiI5tvYiNlK5QYG0mPFRN98xHrewsQMSwCHY-G0Rwq7cFI4hvH89-tyXJmTl18owRMjQuHecw5caIFiI_AcK1Q

# 3.配置context，组合cluster和credentials
.\kubectl.exe config set-context testing-admin@kubernetes --cluster=kubernetes --user=testing-admin

# 4.指定当前上下文，这时候会在~目录下建立一个.kube 目录和conf文件
.\kubectl.exe config use-context testing-admin@kubernetes

```

> 生成的config文件如下

```yaml
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: {证书-k8sadmin的conf有}
    server: https://apiserver.demo:6443
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: testing-admin
  name: testing-admin@kubernetes
current-context: testing-admin@kubernetes
kind: Config
preferences: {}
users:
- name: testing-admin
  user:
    token: {添加token}
```



### ③ 连接服务器

```shell
# 测试 肯看是否能得到那个pod
kubectl  get pods
#提示下面错误 
#Error from server (Forbidden): pods is forbidden: User "testing-admin" cannot list resource "pods" in API group "" in the namespace "default"


# 下面例子是对的
kubectl  get pods -n testing

# 登录进去 如果是win10系统，要在powerShell中使用，在xShell中不好用
kubectl exec -it   test-pd /bin/sh   -n testing

# 看看自己是否可以得到 集群的权限
kubectl get clusterroles # 没有查看所有的权限

kubectl get clusterroles admin # 可以查看已经分配给自己的 admin view edit
```





## 2.3 给组内用户分配权限



### ① 创建用户

创建一个`可以编辑的用户`

```shell
kubectl create serviceaccount testing-edit -n testing  

kubectl get serviceaccount testing-edit -n testing  

# 清除数据
#kubectl delete serviceaccount testing-edit -n testing 
```



### ② 分配权限

将*clusterrole=edit*的权限分配给用户

```shell

# 分配不了这个权限,如何能让用户有clusterrole的权限呢？
kubectl create rolebinding rbd-edit --clusterrole=edit --serviceaccount=testing:testing-edit -n testing 

kubectl get rolebinding -n testing 

# 清除数据
#kubectl delete rolebinding rbd-edit -n testing
```

基本命令 `kubectl get clusterrole`



### ③  得到用户Token

通过下面的命令可以获得自己的token

```shell
user_name=testing-edit

sec_name=$(kubectl get -n testing serviceaccounts $user_name --output=jsonpath='{.secrets[*].name}')

#echo $sec_name

#从返回内容中可查到，该secret的token为：
token=$(kubectl get -n testing secrets $sec_name --output=jsonpath='{.data.token}')

#该token是经过base64处理的，需要进行解码处理
echo $token | base64 -d
```



```
eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ0ZXN0aW5nIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InRlc3RpbmctZWRpdC10b2tlbi16MmxjMiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJ0ZXN0aW5nLWVkaXQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiIyOTkwNzZmNi1jMzcyLTRmMGItYTljOC03YWUwZmYyYjBiYjYiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6dGVzdGluZzp0ZXN0aW5nLWVkaXQifQ.N-1Lur_3TEl1FgmfKhQpKqyusDAecIjMqAIHGJdGfvA32-ZyWS0GbLQQpAmwB-6PVB4AIYmInw7ADyDCCj8zE_q-Omk9dnv1HP2agh_3nWfOHfUxiA7-LOXoDwlrssNZ3r70PCvwwx8bEfelHtpzT5mZjx2gs3PgrzzBiHZH5XIeUVXdNd-uM75qpkxY9wrJlTSHexW-3aiIIn2kLRhIaGBXylVLiWV5GG-cR1RDbRn1LagxvvxEJWRWQ2WAjUPmqe-qO_bWF2i1WeFnpwb6TiG0PXPklKg5uwWVUzxWmyMjakHO97x32AGUH5hxOgHJlfSF-D1WQjV_GqgHRPypiQ
```





# 3. 普通用户

普通用户得到`token`后，可以到`Dashboard`中试试：https://192.168.1.185:30001

普通用户也可以通过kubectl来进行登录。



关于权限建议多实验几次。







# 参考文档

* 做的比较简单的
  * [k8s认证及serviceAccount、userAccount](https://www.cnblogs.com/fawaikuangtu123/p/11295376.html)
  * [k8s之RBAC-基于角色的访问控制](https://www.cnblogs.com/fawaikuangtu123/p/11295430.html)
  * [k8s之dashboard认证、资源需求、资源限制及HeapSter](https://www.cnblogs.com/fawaikuangtu123/p/11296191.html)
* 与实际应用相关的
  * [这些用来审计 Kubernetes RBAC 策略的方法你都见过吗？](https://blog.csdn.net/alex_yangchuansheng/article/details/99853350)

* 使用 Docker 和 Jenkins 持续交付
* 基础原理文档
  * [Kubernetes API 安全机制详解](https://baijiahao.baidu.com/s?id=1634394182029344798&wfr=spider&for=pc)
  * [马哥kubernetes入门](https://pdf.us/tag/kubernetes/page/3)



[kubernetes实战(八)：k8s集群安全机制RBAC](https://www.cnblogs.com/dukuan/p/9948063.html)