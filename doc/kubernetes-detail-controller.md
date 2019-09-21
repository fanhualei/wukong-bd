> 目录

[TOC]





# 0. 控制器-详解

Pod（容器组）是 Kubernetes 中最小的调度单元，您可以通过 kubectl 直接创建一个 Pod。Pod  本身并不能自愈（self-healing）。如果一个 Pod 所在的 Node （节点）出现故障，或者调度程序自身出现故障，Pod  将被删除；同理，当因为节点资源不够或节点维护而驱逐 Pod 时，Pod 也将被删除。

Kubernetes 通过引入 Controller（控制器）的概念来管理 Pod 实例。

在 Kubernetes 中，**您应该始终通过创建 Controller 来创建 Pod，而不是直接创建 Pod。**

> 控制器可以提供如下特性：

- 水平扩展（运行 Pod 的多个副本）
- rollout（版本更新）
- self-healing（故障恢复） 例如：当一个节点出现故障，控制器可以自动地在另一个节点调度一个配置完全一样的 Pod，以替换故障节点上的 Pod。



> 在 Kubernetes 支持的控制器有如下几种：

- [Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/) 
- [StatefulSet](https://kuboard.cn/learning/k8s-intermediate/workload/wl-statefulset/) 
- [DaemonSet](https://kuboard.cn/learning/k8s-intermediate/workload/wl-daemonset/) 
- [CronJob](https://kuboard.cn/learning/k8s-intermediate/workload/wl-cronjob/) 
- [Jobs - Run to Completion](https://kuboard.cn/learning/k8s-intermediate/workload/wl-job/) 
- [ReplicaSet](https://kubernetes.io/docs/concepts/workloads/controllers/replicaset/)(不建议使用)
  - Kubernetes 官方推荐使用 Deployment 替代 ReplicaSet

* [ReplicationController](https://kubernetes.io/docs/concepts/workloads/controllers/replicationcontroller/)(不建议使用)
  * Kubernetes 官方推荐使用 Deployment 替代 ReplicationController

* [Garbage Collection](https://kubernetes.io/docs/concepts/workloads/controllers/garbage-collection/)

* [TTL Controller for Finished Resources](https://kubernetes.io/docs/concepts/workloads/controllers/ttlafterfinished/)





# 1. Deployment-控制器



## 1.1 Deployment介绍 

术语表

| 英文       | 英文简称   | 中文   |
| ---------- | ---------- | ------ |
| Pod        | Pod        | 容器组 |
| Controller | Controller | 控制器 |
| ReplicaSet | ReplicaSet | 副本集 |
| Deployment | Deployment | 部署   |



Deployment 是最常用的用于部署无状态服务的方式。Deployment 控制器使得您能够以声明的方式更新 Pod（容器组）和 ReplicaSet（副本集）。

几种运维场景：

- [创建Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/create.html) 创建 Deployment 后，Deployment 控制器将立刻创建一个 ReplicaSet 副本集，并由 ReplicaSet 创建所需要的 Pod。
- [更新Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/update.html)  更新 Deployment 中 Pod 的定义（例如，发布新版本的容器镜像）。此时 Deployment 控制器将为该 Deployment  创建一个新的 ReplicaSet 副本集，并且逐步在新的副本集中创建 Pod，在旧的副本集中删除 Pod，以达到滚动更新的效果。
- [回滚Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/rollback.html) 回滚到一个早期 Deployment 版本。
- [伸缩Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/scale.html) 水平扩展 Deployment，以便支持更大的负载，或者水平收缩 Deployment，以便节省服务器资源。
- [暂停和继续Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/pause.html)
- [查看Deployment状态](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/status.html)
- [清理策略](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/cleanup.html)
- [金丝雀发布](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/canary.html)



## 1.2  创建 Deployment

本文描述了如何创建一个 Deployment，如何理解 Deployment 各个字段，以及如何查看 Deployment 的创建结果。

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.7.9
        ports:
        - containerPort: 80

```



按照下面的步骤创建该 Deployment



```shell
kubectl apply -f https://k8s.io/examples/controllers/nginx-deployment.yaml
```

> 您可以为该命令增加 --record 选项，此时 kubectl 会将 `kubectl apply -f https://k8s.io/examples/controllers/nginx-deployment.yaml --record` 写入 Deployment 的 annotation（注解） `kubernetes.io/change-cause` 中。这样，您在将来就可以回顾某一个 Deployment 版本变化的原因



```shell
#检查 Deployment 的创建情况
kubectl get deployments

#查看 Deployment 的发布状态（rollout status）
kubectl rollout status deployment.v1.apps/nginx-deployment

#查看该 Deployment 创建的 ReplicaSet（rs）
kubectl get rs

#查看 Pod 的标签
kubectl get pods --show-labels
```

> kubectl get pods --show-labels 这个结果中有

**Pod-template-hash 标签**:这个时系统自动创建的，不要修改。

> kubectl get deployments 显示结果说明

| 字段名称       | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| **NAME**       | Deployment name                                              |
| **DESIRED**    | Deployment 期望的 Pod 副本数，即 Deployment 中 `.spec.replicas` 字段指定的数值。该数值是“期望”值 |
| **CURRENT**    | 当前有多少个 Pod 副本数在运行                                |
| **UP-TO-DATE** | Deployment 中，符合当前 Pod Template 定义的 Pod 数量         |
| **AVAILABLE**  | 当前对用户可用的 Pod 副本数                                  |
| **AGE**        | Deployment 部署以来到现在的时长                              |



## 1.3  更新 Deployment

可以修改配置文件，然后执行`kubectl apply -f`来更新，或者使用kubectl命令，例如下面的例子：

```shell
# 执行以下命令，将容器镜像从 nginx:1.7.9 更新到 nginx:1.9.1
kubectl --record deployment.apps/nginx-deployment set image deployment.v1.apps/nginx-deployment nginx=nginx:1.9.1

#或者，您可以 edit 该 Deployment，并将 .spec.template.spec.containers[0].image 从 nginx:1.7.9 修改为 nginx:1.9.1
kubectl edit deployment.v1.apps/nginx-deployment

#查看发布更新（rollout）的状态，执行命令：
kubectl rollout status deployment.v1.apps/nginx-deployment


```

> 覆盖更新 Rollover （更新过程中再更新）

例如：

- 假设您创建了一个 Deployment 有 5 个 nginx:1.7.9 的副本；
- 您立刻更新该 Deployment 使得其 `.spec.replicas` 为 5，容器镜像为 `nginx:1.9.1`，而此时只有 3 个 nginx:1.7.9 的副本已创建；
- 此时，Deployment  Controller 将立刻开始 kill 已经创建的 3 个 nginx:1.7.9 的 Pod，并开始创建 nginx:1.9.1 的  Pod。Deployment Controller 不会等到 5 个 nginx:1.7.9 的 Pod 都创建完之后在开始新的更新



## 1.4  回滚 Deployment

默认情况下，kubernetes 将保存 Deployment 的所有更新（rollout）历史。

### 1.4.1   模拟更新错误

```shell
# 假设您在更新 Deployment 的时候，犯了一个拼写错误，将 nginx:1.9.1 写成了 nginx:1.91
kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.91 --record=true

#检查其状态
kubectl rollout status deployment.v1.apps/nginx-deployment

#  您将看到两个旧的 和一个新的
kubectl get rs

#执行命令 kubectl get pods，您将看到 1 个由新 ReplicaSet 创建的 Pod 卡在抓取 image 的死循环里：
kubectl get pods

#执行命令 kubectl describe deployment 查看 Deployment 的详情
kubectl describe deployment

```



### 1.4.2  检查 更新历史



```shell
#检查 Deployment 的历史版本
kubectl rollout history deployment.v1.apps/nginx-deployment
```

> 输出结果如下所示

```
deployments "nginx-deployment"
REVISION    CHANGE-CAUSE
1           kubectl apply --filename=https://k8s.io/examples/controllers/nginx-deployment.yaml --record=true
2           kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.9.1 --record=true
3           kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.91 --record=true
```

**CHANGE-CAUSE** 是该 revision（版本）创建时从 Deployment 的 annotation `kubernetes.io/change-cause` 拷贝而来。

您可以通过如下方式制定 **CHANGE-CAUSE** 信息：

- 为 Deployment 增加注解，`kubectl annotate deployment.v1.apps/nginx-deployment kubernetes.io/change-cause="image updated to 1.9.1"`
- 执行 kubectl apply 命令时，增加 `--record` 选项
- 手动编辑 Deployment 的 `.metadata.annotation` 信息



```shell
#查看 revision（版本）的详细信息
kubectl rollout history deployment.v1.apps/nginx-deployment --revision=2
```

> 输出结果如下所示：

```
deployments "nginx-deployment" revision 2
  Labels:       app=nginx
          pod-template-hash=1159050644
  Annotations:  kubernetes.io/change-cause=kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.9.1 --record=true
  Containers:
  nginx:
    Image:      nginx:1.9.1
    Port:       80/TCP
    QoS Tier:
        cpu:      BestEffort
        memory:   BestEffort
    Environment Variables:      <none>
  No volumes.
```



### 1.4.3  回滚到前一个版本

```shell
#将 Deployment 从当前版本回滚到前一个版本
kubectl rollout undo deployment.v1.apps/nginx-deploymen

#使用 --to-revision 选项回滚到前面的某一个指定版本
kubectl rollout undo deployment.v1.apps/nginx-deployment --to-revision=2

#检查该回滚是否成功
kubectl get deployment nginx-deployment

#查看 Deployment 的详情
kubectl describe deployment nginx-deployment
```



## 1.5  伸缩 Deployment

伸缩（Scaling） Deployment，是指改变 Deployment 中 Pod 的副本数量，以应对实际业务流量的变化。

### 1.5.1  指定伸缩

```shell
# 手工指定
kubectl scale deployment.v1.apps/nginx-deployment --replicas=10

# 自动 -如果您的集群启用了自动伸缩（horizontal Pod autoscaling ）
#执行以下命令，您就可以基于 CPU 的利用率在一个最大和最小的区间自动伸缩您的 Deployment：
kubectl autoscale deployment.v1.apps/nginx-deployment --min=10 --max=15 --cpu-percent=80
```



### 1.5.2  按比例伸缩

指定最大增加节点数量



## 1.6  暂停和继续 Deployment

您可以先暂停 Deployment，然后再触发一个或多个更新，最后再继续（resume）该 Deployment。这种做法使得您可以在暂停和继续中间对 Deployment 做多次更新，**而无需触发不必要的滚动更新**。

```shell
#暂停 Deployment
kubectl rollout pause deployment.v1.apps/nginx-deployment

#更新 Deployment 的容器镜像
kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.9.1

#针对 Deployment 执行更多的修改
kubectl set resources deployment.v1.apps/nginx-deployment -c=nginx --limits=cpu=200m,memory=512Mi

#查看 Deployment 的信息是否被正确修改
kubectl describe deployment nginx-deployment

#继续（resume）该 Deployment，可使前面所有的变更一次性生效
kubectl rollout resume deployment.v1.apps/nginx-deployment

```



## 1.7  查看 Deployment 的状态

Deployment 的生命周期中，将会进入不同的状态，这些状态可能是：

- Progressing 正在执行滚动更新
- complete
- fail to progress



```shell
# 监控 Deployment 滚动更新的过程
kubectl rollout status deployment.v1.apps/nginx-deployment
```



### 1.7.1  Progressing 状态

当如下任何一个任务正在执行时，Kubernete 将 Deployment 的状态标记为 ***progressing***：

- Deployment 创建了一个新的 ReplicaSet
- Deployment 正在 scale up 其最新的 ReplicaSet
- Deployment 正在 scale down 其旧的 ReplicaSet
- 新的 Pod 变为 ***就绪（ready）*** 或 ***可用（available）***

您可以使用命令 `kubectl rollout status` 监控 Deployment 滚动更新的过程



### 1.7.2  Complete 状态

如果 Deployment 符合以下条件，Kubernetes 将其状态标记为 ***complete***：

- 该 Deployment 中的所有 Pod 副本都已经被更新到指定的最新版本
- 该 Deployment 中的所有 Pod 副本都处于 ***可用（available）*** 状态
- 该 Deployment 中没有旧的 ReplicaSet 正在运行

您可以执行命令 `kubectl rollout status` 检查 Deployment 是否已经处于 ***complete*** 状态。如果是，则该命令的退出码为 0。 例如，执行命令 `kubectl rollout status deployment.v1.apps/nginx-deployment`



### 1.7.3  Failed 状态

Deployment 在更新其最新的 ReplicaSet 时，可能卡住而不能达到 ***complete*** 状态。如下原因都可能导致此现象发生：

- 集群资源不够
- 就绪检查（readiness probe）失败
- 镜像抓取失败
- 权限不够
- 资源限制
- 应用程序的配置错误导致启动失败



为了解决资源不足的问题，您可以尝试：

- scale down 您的 Deployment
- scale down 其他的 Deployment
- 向集群中添加计算节点



### 1.7.4  处理Failed 状态的 Deployment

您可以针对 ***Failed*** 状态下的 Deployment 执行任何适用于 Deployment 的指令，例如：

- scale up / scale down
- 回滚到前一个版本
- 暂停（pause）Deployment，以对 Deployment 的 Pod template 执行多处更新



## 1.8  版本历史记录

通过 Deployment 中 `.spec.revisionHistoryLimit` 字段，可指定为该 Deployment 保留多少个旧的 ReplicaSet。超出该数字的将被在后台进行垃圾回收。该字段的默认值是 10。



## 1.9   金丝雀发布

滚动发布





# 2. StatefulSet-控制器

StatefulSet 顾名思义，用于管理 Stateful（有状态）的应用程序。

StatefulSet 管理 Pod 时，确保其 Pod 有一个按顺序增长的 ID。

与 [Deployment](https://kuboard.cn/learning/k8s-intermediate/workload/wl-deployment/)  相似，StatefulSet 基于一个 Pod 模板管理其 Pod。与 Deployment 最大的不同在于 StatefulSet  始终将一系列不变的名字分配给其 Pod。这些 Pod 从同一个模板创建，但是并不能相互替换：每个 Pod 都对应一个特有的持久化存储标识。

同其他所有控制器一样，StatefulSet 也使用相同的模式运作：用户在 StatefulSet 中定义自己期望的结果，StatefulSet 控制器执行需要的操作，以使得该结果被达成。



## 2.1 StatefulSet 使用场景

对于有如下要求的应用程序，StatefulSet 非常适用：

- 稳定、唯一的网络标识（dnsname）
- 稳定、不变的持久化路径（或存储卷）
- 按顺序地增加副本、减少副本，并在减少副本时执行清理
- 按顺序自动地执行滚动更新

如果一个应用程序不需要稳定的网络标识，或者不需要按顺序部署、删除、增加副本，您应该考虑使用 Deployment 这类无状态（stateless）的控制器。



## 2.2 StatefulSet 限制

- Pod 的存储要么由 storage class 对应的 [PersistentVolume Provisioner](https://github.com/kubernetes/examples/blob/master/staging/persistent-volume-provisioning/README.md)提供，要么由集群管理员事先创建
- 删除或 scale down 一个 StatefulSet 将不会删除其对应的数据卷。这样做的考虑是数据安全
- 删除 StatefulSet 时，将无法保证 Pod 的终止是正常的。如果要按顺序 gracefully 终止 StatefulSet 中的 Pod，可以在删除 StatefulSet 前将其 scale down 到 0
- 当使用默认的 [Pod Management Policy](https://kuboard.cn/learning/k8s-intermediate/workload/wl-statefulset/update.html) (OrderedReady) 进行滚动更新时，可能进入一个错误状态，并需要[人工介入](https://kuboard.cn/learning/k8s-intermediate/workload/wl-statefulset/update.html)才能修复



## 2.3 StatefulSet 基本概念

### 2.3.1 创建 StatefulSet

下面是一个 StatefulSet 的例子，由如下内容组成：

- 一个名为 nginx 的 [Headless Service](https://kubernetes.io/docs/concepts/services-networking/service/#headless-service)，用于控制网络域

* 一个名为 web 的StatefulSet，副本数为 3

* volumeClaimTemplates 提供稳定的存储（每一个 Pod ID 对应自己的存储卷，且 Pod 重建后，仍然能找到对应的存储卷）

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
  labels:
    app: nginx
spec:
  ports:
  - port: 80
    name: web
  clusterIP: None
  selector:
    app: nginx
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: web
spec:
  selector:
    matchLabels:
      app: nginx # has to match .spec.template.metadata.labels
  serviceName: "nginx"
  replicas: 3 # by default is 1
  template:
    metadata:
      labels:
        app: nginx # has to match .spec.selector.matchLabels
    spec:
      terminationGracePeriodSeconds: 10
      containers:
      - name: nginx
        image: nginx:1.7.9
        ports:
        - containerPort: 80
          name: web
        volumeMounts:
        - name: www
          mountPath: /usr/share/nginx/html
  volumeClaimTemplates:
  - metadata:
      name: www
    spec:
      accessModes: [ "ReadWriteOnce" ]
      storageClassName: "my-storage-class"
      resources:
        requests:
          storage: 1Gi
```



### 2.3.2 稳定的网络 ID

- StatefulSet 中 Pod 的 hostname 格式为 $(StatefulSet name)-$(Pod 序号)。上面的例子将要创建三个 Pod，其名称分别为： web-0，web-1，web-2。
- StatefulSet  可以使用 Headless Service 来控制其 Pod 所在的域。该域（domain）的格式为 $(service  name).$(namespace).svc.cluster.local，其中 “cluster.local” 是集群的域。
- StatefulSet 中每一个 Pod 将被分配一个 dnsName，格式为： $(podName).$(所在域名)



下表列出了不同的 集群域、Service name、StatefulSet name 的情况下，对应的 StatefulSet 中 Pod 的 DNS 名字：

| 字段名                    | 组合一                                       | 组合二                                   | 组合三                                |
| ------------------------- | -------------------------------------------- | ---------------------------------------- | ------------------------------------- |
| **集群域 Cluster Domain** | cluster.local                                | cluster.local                            | cluster.local                         |
| **Service name**          | default/nginx                                | foo/nginx                                | foo/nginx                             |
| **StatefulSet name**      | default/web                                  | foo/web                                  | foo/web                               |
| **StatefulSet Domain**    | nginx.default.svc.cluster.local              | nginx.foo.svc.cluster.local              | nginx.foo.svc.kube.local              |
| **Pod DNS**               | web-{0..N-1}.nginx.default.svc.cluster.local | web-{0..N-1}.nginx.foo.svc.cluster.local | web-{0..N-1}.nginx.foo.svc.kube.local |
| **Pod name**              | web-{0..N-1}                                 | web-{0..N-1}                             | web-{0..N-1}                          |



### 2.3.3 稳定的存储

Kubernetes 为每一个 VolumeClaimTemplate 创建一份 PersistentVolume（存储卷）。在上面的例子中，每一个 Pod 都将由 StorageClass（存储类）`my-storage-class` 为其创建一个 1Gib 大小的 PersistentVolume（存储卷）。当 Pod 被调度（或重新调度）到一个节点上，其挂载点将挂载该存储卷声明（关联到该 PersistentVolume）。

- 当 Pod 或 StatefulSet 被删除时，其关联的 PersistentVolumeClaim（存储卷声明）以及其背后的 PersistentVolume（存储卷）仍然存在。
- 如果相同的 Pod 或 StatefulSet 被再次创建，则，新建的名为 web-0 的 Pod 仍将挂载到原来名为 web-0 的 Pod 所挂载的存储卷声明及存储卷。
- 这确保了 web-0、web-1、web-2 等，不管被删除重建多少次，都将 “稳定” 的使用各自所对应的存储内容



### 2.3.4 Pod name 标签

当 StatefulSet 控制器创建一个 Pod 时，会为 Pod 添加一个标签（label） `statefulset.kubernetes.io/pod-name` 且该标签的值为 Pod 的名字。您可以利用此名字，StatefulSet 中的某一个特定的 Pod 关联一个 Service。

> 实际操作中，您无需为 StatefulSet 中的一个特定 Pod 关联 Service，因为您可以直接通过该 Pod 的 DNS Name 访问到 Pod。







## 2.4 StatefulSet 的部署和伸缩

### 2.4.1 部署和伸缩 StatefulSet 时的执行顺序

- 在创建一个副本数为 N 的 StatefulSet 时，其 Pod 将被按 {0 ... N-1} 的顺序逐个创建
- 在删除一个副本数为 N 的 StatefulSet （或其中所有的 Pod）时，其 Pod 将按照相反的顺序（即 {N-1 ... 0}）终止和删除
- 在对 StatefulSet 执行扩容（scale up）操作时，新增 Pod 所有的前序 Pod 必须处于 Running（运行）和 Ready（就绪）的状态
- 终止和删除 StatefulSet 中的某一个 Pod 时，该 Pod 所有的后序 Pod 必须全部已终止

> 下面说一下具体的例子

[创建 StatefulSet](https://kuboard.cn/learning/k8s-intermediate/workload/wl-statefulset/basics.html) 例子中的 nginx StatefulSet 被创建时：

- Pod web-0、web-1、web-2 将被按顺序部署
- web-0 处于 Running 和 Ready 状态之前，web-1 不会创建；web-1 处于 Running 和 Ready 状态之前，web-2 不会创建
- 如果 web-1 已处于 Running 和 Ready 的状态，web-2 尚未创建，此时 web-0 发生了故障，则在 web-0 成功重启并达到 Running 和 Ready 的状态之前，web-2 不会创建
- 如果用户对这个 StatefulSet 执行缩容（scale down）操作，将其副本数调整为 1，则： 
  - web-2 将被首先终止；在 web-2 已终止并删除之后，才开始终止 web-1
  - 假设在 web-2 终止并删除之后，web-1 终止之前，此时 web-0 出现故障，则，在 web-0 重新回到 Running 和 Ready 的状态之前，kubernetes 将不会终止 web-1

### 2.5.2 Pod 管理策略

在 Kubernetes 1.7 及其后续版本中，可以为 StatefulSet 设定 `.spec.podManagementPolicy` 字段，以便您可以继续使用 StatefulSet 唯一 ID 的特性，但禁用其有序创建和销毁 Pod 的特性。该字段的取值如下：

* OrderedReady
  * OrderedReady 是 `.spec.podManagementPlicy` 的默认值。其对 Pod 的管理方式已经在 [部署和伸缩 StatefulSet 时的执行顺序](https://kuboard.cn/learning/k8s-intermediate/workload/wl-statefulset/scaling.html#部署和伸缩-statefulset-时的执行顺序) 详细描述

* Parallel
  * `.spec.podManagementPlicy`  的取值为 Parallel，则 StatefulSet Controller 将同时并行地创建或终止其所有的 Pod。此时  StatefulSet Controller 将不会逐个创建 Pod，等待 Pod 进入 Running 和 Ready 状态之后再创建下一个  Pod，也不会逐个终止 Pod。
  * 此选项只影响到伸缩（scale up/scale down）操作。更新操作不受影响。

## 2.6 StatefulSet 的更新策略

在 Kubernetes 1.7 及之后的版本中，可以为 StatefulSet 设定 `.spec.updateStrategy` 字段，以便您可以在改变 StatefulSet 中 Pod 的某些字段时（container/labels/resource request/resource limit/annotation等）禁用滚动更新。

### 2.6.1 On Delete

OnDelete 策略实现了 StatefulSet 的遗留版本（kuberentes 1.6及以前的版本）的行为。如果 StatefulSet 的 `.spec.updateStrategy.type` 字段被设置为 OnDelete，当您修改 `.spec.template` 的内容时，StatefulSet Controller 将不会自动更新其 Pod。您必须手工删除 Pod，此时 StatefulSet Controller 在重新创建 Pod 时，使用修改过的 `.spec.template` 的内容创建新 Pod。

### 2.6.1 Rolling Updates

`.spec.updateStrategy.type` 字段的默认值是 RollingUpdate，该策略为 StatefulSet 实现了 Pod 的自动滚动更新。在用户更新 StatefulSet 的 `.spec.tempalte` 字段时，StatefulSet Controller 将自动地删除并重建 StatefulSet 中的每一个 Pod。处理顺序如下：

- 从序号最大的 Pod 开始，逐个删除和更新每一个 Pod，直到序号最小的 Pod 被更新

- 当正在更新的 Pod 达到了 Running 和 Ready 的状态之后，才继续更新其前序 Pod

- **Partitions**

  通过指定 `.spec.updateStrategy.rollingUpdate.partition` 字段，可以分片（partitioned）执行RollingUpdate 更新策略。当更新 StatefulSet 的 `.spec.template` 时：

  - 序号大于或等于 `.spec.updateStrategy.rollingUpdate.partition` 的 Pod 将被删除重建
  - 序号小于 `.spec.updateStrategy.rollingUpdate.partition` 的 Pod 将不会更新，即使手工删除该 Pod，kubernetes 也会使用前一个版本的 `.spec.template` 重建该 Pod
  - 如果 `.spec.updateStrategy.rollingUpdate.partition` 大于 `.spec.replicas`，更新 `.spec.tempalte` 将不会影响到任何 Pod

  * 大部分情况下，您不需要使用 `.spec.updateStrategy.rollingUpdate.partition`，除非您碰到如下场景：
    * 执行预发布
    * 执行金丝雀更新
    * 执行按阶段的更新

  

 * Forced Rollback 强制回滚

  当使用默认的 Pod 管理策略时（OrderedReady），很有可能会进入到一种卡住的状态，需要人工干预才能修复。  如果您更新 Pod template 后，该 Pod 始终不能进入 Running 和 Ready 的状态（例如，镜像错误或应用程序配置错误），StatefulSet 将停止滚动更新并一直等待。

  此时，如果您仅仅将 Pod template 回退到一个正确的配置仍然是不够的。由于一个已知的问题，StatefulSet 将继续等待出错的 Pod 进入就绪状态（该状态将永远无法出现），才尝试将该 Pod 回退到正确的配置。

  在修复 Pod template 以后，您还必须删除掉所有已经尝试使用有问题的 Pod template 的 Pod。StatefulSet此时才会开始使用修复了的 Pod template 重建 Pod。

  

  









# 3. DaemonSet-控制器

**DaemonSet 控制器确保所有（或一部分）的节点都运行了一个指定的 Pod 副本。**

- 每当向集群中添加一个节点时，指定的 Pod 副本也将添加到该节点上
- 当节点从集群中移除时，Pod 也就被垃圾回收了
- 删除一个 DaemonSet 可以清理所有由其创建的 Pod

**DaemonSet 的典型使用场景有：**

- 在每个节点上运行集群的存储守护进程，例如 glusterd、ceph
- 在每个节点上运行日志收集守护进程，例如 fluentd、logstash
- 在每个节点上运行监控守护进程，例如 [Prometheus Node Exporter](https://github.com/prometheus/node_exporter)、[Sysdig Agent](https://sysdigdocs.atlassian.net/wiki/spaces/Platform)

通常情况下，一个 DaemonSet 将覆盖所有的节点。复杂一点儿的用法，可能会为某一类守护进程设置多个 DaemonSets，每一个 DaemonSet 针对不同类硬件类型设定不同的内存、cpu请求。



## 3.1 创建 DaemonSet

### 3.1.1 YAML 示例

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluentd-elasticsearch
  namespace: kube-system
  labels:
    k8s-app: fluentd-logging
spec:
  selector:
    matchLabels:
      name: fluentd-elasticsearch
  template:
    metadata:
      labels:
        name: fluentd-elasticsearch
    spec:
      tolerations:
      - key: node-role.kubernetes.io/master
        effect: NoSchedule
      containers:
      - name: fluentd-elasticsearch
        image: fluent/fluentd-kubernetes-daemonset:v1.7.1-debian-syslog-1.0
        resources:
          limits:
            memory: 200Mi
          requests:
            cpu: 100m
            memory: 200Mi
        volumeMounts:
        - name: varlog
          mountPath: /var/log
        - name: varlibdockercontainers
          mountPath: /var/lib/docker/containers
          readOnly: true
      terminationGracePeriodSeconds: 30
      volumes:
      - name: varlog
        hostPath:
          path: /var/log
      - name: varlibdockercontainers
        hostPath:
          path: /var/lib/docker/containers
```

执行如下命令可创建该 DaemonSet：

```
kubectl apply -f ./daemonset.yaml
```

### 3.1.2 必填字段

与其他所有 Kubernetes API 对象相同，DaemonSet 需要如下字段：

- apiVersion
- kind
- metadata

除此之外，DaemonSet 还需要 `.spec` 字段



### 3.1.3 只在部分节点上运行

指定 `.spec.template.spec.nodeSelector` ，DaemonSet Controller 将只在指定的节点上创建 Pod （参考 [节点选择器 nodeSelector](https://kuboard.cn/learning/k8s-intermediate/config/assign-pod-node.html#节点选择器-nodeselector)）。同样的，如果指定 `.spec.template.spec.affinity` ，DaemonSet Controller 将只在与 [node affinity](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/) 匹配的节点上创建 Pod。



## 3.2 DaemonSet 是如何调度的

Kubernetes v1.12 版本以后，默认通过 kubernetes 调度器来调度 DaemonSet 的 Pod。

> 污点和容忍

在调度 DaemonSet 的 Pod 时，污点和容忍（[taints and tolerations](https://kuboard.cn/learning/k8s-intermediate/config/taints-and-toleration.html)）会被考量到，同时，以下容忍（toleration）将被自动添加到 DaemonSet 的 Pod 中：

| Toleration Key                         | Effect     | Version | 描述                                                         |
| -------------------------------------- | ---------- | ------- | ------------------------------------------------------------ |
| node.kubernetes.io/not-ready           | NoExecute  | 1.13+   | 节点出现问题时（例如网络故障），DaemonSet 容器组将不会从节点上驱逐 |
| node.kubernetes.io/unreachable         | NoExecute  | 1.13+   | 节点出现问题时（例如网络故障），DaemonSet 容器组将不会从节点上驱逐 |
| node.kubernetes.io/disk-pressure       | NoSchedule | 1.8+    |                                                              |
| node.kubernetes.io/memory-pressure     | NoSchedule | 1.8+    |                                                              |
| node.kubernetes.io/unschedulable       | NoSchedule | 1.12+   | 默认调度器针对 DaemonSet 容器组，容忍节点的 `unschedulable`属性 |
| node.kubernetes.io/network-unavailable | NoSchedule | 1.12+   | 默认调度器针对 DaemonSet 容器组，在其使用 host network 时，容忍节点的 `network-unavailable` 属性 |



## 3.3 与 DaemonSet 通信

与 DaemonSet 容器组通信的模式有：

- **Push：** DaemonSet 容器组用来向另一个服务推送信息，例如数据库的统计信息。这种情况下 DaemonSet 容器组没有客户端
- **NodeIP + Port：** DaemonSet 容器组可以使用 `hostPort`，此时可通过节点的 IP 地址直接访问该容器组。客户端需要知道节点的 IP 地址，以及 DaemonSet 容器组的 端口号
- **DNS：** 创建一个 [headless service](https://kubernetes.io/docs/concepts/services-networking/service/#headless-services)，且该 Service 与 DaemonSet 有相同的 Pod Selector。此时，客户端可通过该 Service 的 DNS 解析到 DaemonSet 的 IP 地址
- **Service：** 创建一个 Service，且该 Service 与 DaemonSet 有相同的 Pod Selector，客户端通过该 Service，可随机访问到某一个节点上的 DaemonSet 容器组



## 3.4 更新 DaemonSet

> 更新信息

- 在改变节点的标签时：
  - 如果该节点匹配了 DaemonSet 的 `.spec.template.spec.nodeSelector`，DaemonSet 将会在该节点上创建一个 Pod
  - 如果该节点原来匹配 DaemonSet 的 `.spec.template.spec.nodeSelector`，现在不匹配了，则，DaemonSet 将会删除该节点上对应的 Pod
- 您可以修改 DaemonSet 的 Pod 的部分字段，但是，DaemonSet 控制器在创建新的 Pod 时，仍然会使用原有的 Template 进行 Pod 创建。
- 您可以删除 DaemonSet。如果在 `kubectl` 命令中指定 `--cascade=false` 选项，DaemonSet 容器组将不会被删除。紧接着，如果您创建一个新的 DaemonSet，与之前删除的 DaemonSet 有相同的 `.spec.selector`，新建 DaemonSet 将直接把这些未删除的 Pod 纳入管理。DaemonSet 根据其 `updateStrategy` 决定是否更新这些 Pod



执行滚动更新:https://kubernetes.io/docs/tasks/manage-daemon/update-daemon-set/



## 3.5 DaemonSet 的替代选项

DaemonSet 有如下替代选项可以选择

### 3.5.1 Init Scripts

您可以通过脚本（例如，`init`、`upstartd`、`systemd`）直接在节点上启动一个守护进程。相对而言，DaemonSet 在处理守护进程时，有如下优势：

- 使用与应用程序相同的方式处理守护进程的日志和监控
- 使用与应用程序相同的配置语言和工具（例如：Pod template、kubectl）处理守护进程
- 在容器中运行守护进程，可为守护进程增加 resource limits 等限定

### 3.5.2 Pods

您可以直接创建 Pod，并指定其在某一个节点上运行。相对而言，使用 DaemonSet 可获得如下优势：

- Pod 终止后，DaemonSet 可以立刻新建 Pod 以顶替已终止的 Pod。Pod 终止的原因可能是：
  - 节点故障
  - 节点停机维护

### 3.5.3 静态 Pod

这种不被推荐了，今后可能会被删除

### 3.5.4 Deployment

DaemonSet 和 Deployment 一样，他们都创建长时间运行的 Pod（例如 web server、storage server 等）

- Deployment 适用于无状态服务（例如前端程序），对于这些程序而言，扩容（scale up）/ 缩容（scale down）、滚动更新等特性比精确控制 Pod 所运行的节点更重要。
- DaemonSet 更适合如下情况：
  - Pod 的副本总是在所有（或者部分指定的）节点上运行
  - 需要在其他 Pod 启动之前运行





# 参考文档

* [Kubernetes 免费中文教程--推荐](https://kuboard.cn/learning/)

* [Kubernetes中文社区 | 中文文档](http://docs.kubernetes.org.cn/)