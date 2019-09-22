> 目录

[TOC]







# Pod详解

术语中英文对照：

| 英文全称   | 英文缩写   | 中文翻译 |
| ---------- | ---------- | -------- |
| Pod        | Pod        | 容器组   |
| Container  | Container  | 容器     |
| Controller | Controller | 控制器   |



# 1. Pod-概述

Pod（容器组）是 Kubernetes 中最小的可部署单元。

一个 Pod（容器组）包含了一个应用程序容器（某些情况下是多个容器）、存储资源、一个唯一的网络 IP 地址、以及一些确定容器该如何运行的选项。

Pod 容器组代表了 Kubernetes 中一个独立的应用程序运行实例，该实例可能由单个容器或者几个紧耦合在一起的容器组成。



## 1.1 Pod 如何管理多个容器

Pod 的设计目的是用来支持多个互相协同的容器，是的他们形成一个有意义的服务单元。



> 提示：只有在容器之间紧密耦合时，才应该使用这种方式

```
将多个容器运行于同一个容器组中是一种相对高级复杂的使用方法。只有在您的容器相互之间紧密耦合时，您才应该使用这种方式。例如：您可能有一个容器是 web server，用来将共享数据卷中的文件作为网站发布出去，同时您有另一个 "sidecar" 容器从远程抓取并更新这些文件。
```





> 初始化容器

某些 Pod 除了使用 app container （工作容器）以外，还会使用 init container （初始化容器），初始化容器运行并结束后，工作容器才开始启动。



> Pod 为其成员容器提供了两种类型的共享资源：网络和存储

* 网络 Networking：每一个 Pod 被分配一个独立的 IP 地址。Pod 中的所有容器共享一个网络名称空间：
  * 同一个 Pod 中的所有容器 IP 地址都相同
  * 同一个 Pod 中的不同容器不能使用相同的端口，否则会导致端口冲突
  * 同一个 Pod 中的不同容器可以通过 localhost:port 进行通信
  * 同一个 Pod 中的不同容器可以通过使用常规的进程间通信手段，例如 SystemV semaphores 或者 POSIX 共享内存
  * **不同 Pod 上的两个容器如果要通信，必须使用对方 Pod 的 IP 地址 + 对方容器的端口号 进行网络通信**

* 存储 Storage

Pod 中可以定义一组共享的数据卷。Pod 中所有的容器都可以访问这些共享数据卷，以便共享数据。Pod 中数据卷的数据也可以存储持久化的数据，使得容器在重启后仍然可以访问到之前存入到数据卷中的数据。请参考 [数据卷 Volume](https://kuboard.cn/learning/k8s-intermediate/persistent/volume.html)



## 1.2 Pod容器的特点

Kubernetes 的设计中 Pod 是一个相对来说存活周期短暂，且随时会丢弃的实体。

在 Pod 被创建后（您直接创建，或者间接通过 Controller 创建），将被调度到集群中的一个节点上运行。

Pod 将一直保留在该节点上，直到 Pod 以下情况发生：

- Pod 中的容器全部结束运行
- Pod 被删除
- 由于节点资源不够，Pod 被驱逐
- 节点出现故障（例如死机）



>请不要混淆以下两个概念：

```
- 重启 Pod 中的容器
- 重启 Pod
Pod 本身并不会运行，Pod 仅仅是容器运行的一个环境
```



Pod 本身并不能自愈（self-healing）。如果一个 Pod 所在的 Node （节点）出现故障，或者调度程序自身出现故障，Pod 将被删除；

同理，当因为节点资源不够或节点维护而驱逐 Pod 时，Pod 也将被删除。

Kubernetes 通过引入 Controller（控制器）的概念来管理 Pod 实例。**在 Kubernetes 中，更为推荐的做法是使用 Controller 来管理 Pod，而不是直接创建 Pod。**

通过命令`kubectl run ` 会提示这个方法将被取代。



## 1.3 应该使用控制器创建 Pod

> 用户应该始终使用控制器来创建 Pod，而不是直接创建 Pod

控制器可以提供如下特性：

- 水平扩展（运行 Pod 的多个副本）

- rollout（版本更新）

- self-healing（故障恢复）

  例如：当一个节点出现故障，控制器可以自动地在另一个节点调度一个配置完全一样的 Pod，以替换故障节点上的 Pod。



> 在 Kubernetes 中，广泛使用的控制器有：

- Deployment
- StatefulSet
- DaemonSet

控制器通过其中配置的 Pod Template 信息来创建 Pod。



## 1.4 Pod 模板

Pod Template 是关于 Pod 的定义，但是被包含在其他的 Kubernetes 对象中（例如 Deployment、StatefulSet、DaemonSet 等控制器）。

控制器通过 Pod Template 信息来创建 Pod。

正是由于 Pod Template 的存在，Kuboard 可以使用一个工作负载编辑器来处理不同类型的控制器。



## 1.5 停止 Pod 

Pod 代表了运行在集群节点上的进程，而进程的终止有两种方式：

- 优雅地终止
- 直接 kill，此时进程没有机会执行清理动作

```
默认情况下，删除 Pod 的 grace period（等待时长）是 30 秒。

可以通过 kubectl delete 命令的选项 --grace-period=<seconds> 自己指定 grace period（等待时长）。

如果您要强制删除 Pod，您必须为 kubectl delete 命令同时指定两个选项 --grace-period=0 和 --force
```





# 2. Pod-生命周期



## 2.1 Pod生命周期阶段的划分

Pod phase 代表其所处生命周期的阶段。Pod phase 并不是用来代表其容器的状态，也不是一个严格的状态机。

phase 的可能取值有：

| Phase     | 描述                                                         |
| --------- | ------------------------------------------------------------ |
| Pending   | Kubernetes 已经创建并确认该 Pod。此时可能有两种情况：Pod 还未完成调度（例如没有合适的节点）正在从 docker registry 下载镜像 |
| Running   | 该 Pod 已经被绑定到一个节点，并且该 Pod 所有的容器都已经成功创建。其中至少有一个容器正在运行，或者正在启动/重启 |
| Succeeded | Pod 中的所有容器都已经成功终止，并且不会再被重启             |
| Failed    | Pod 中的所有容器都已经终止，至少一个容器终止于失败状态：容器的进程退出码不是 0，或者被系统 kill |
| Unknown   | 因为某些未知原因，不能确定 Pod 的状态，通常的原因是 master 与 Pod 所在节点之间的通信故障 |



## 2.2 Pod 状况

Pod conditions，每一个 Pod 都有一个数组描述其是否达到某些指定的条件。

该数组的每一行可能有六个字段：

| 字段名             | 描述                                                         |
| ------------------ | ------------------------------------------------------------ |
| type               | type 是最重要的字段，可能的取值有：**PodScheduled：** Pod 已被调度到一个节点**Ready：** Pod 已经可以接受服务请求，应该被添加到所匹配 Service 的负载均衡的资源池。  **Initialized：**Pod 中所有初始化容器已成功执行。  **Unschedulable：**不能调度该 Pod（缺少资源或者其他限制）。  **ContainersReady：**Pod 中所有容器都已就绪 |
| status             | 能的取值有：TrueFalseUnknown                                 |
| reason             | Condition 发生变化的原因，使用一个符合驼峰规则的英文单词描述 |
| message            | Condition 发生变化的原因的详细描述，human-readable           |
| lastTransitionTime | Condition 发生变化的时间戳                                   |
| lastProbeTime      | 上一次针对 Pod 做健康检查/就绪检查的时间戳                   |



## 2.3 容器健康检查

Probe 是指 kubelet 周期性地检查容器的状况。



> Probe有三种类型 ：

- **ExecAction：** 在容器内执行一个指定的命令。如果该命令的退出状态码为 0，则成功
- **TCPSocketAction：** 探测容器的指定 TCP 端口，如果该端口处于 open 状态，则成功
- **HTTPGetAction：** 探测容器指定端口/路径上的 HTTP Get 请求，如果 HTTP 响应状态码在 200 到 400（不包含400）之间，则成功



> Probe 有三种可能的结果：

- **Success：** 容器通过检测
- **Failure：** 容器未通过检测
- **Unknown：** 检测执行失败，此时 kubelet 不做任何处理



> 对运行中的容器执行 Probe的两种情况：

- **就绪检查 readinessProbe：** 确定容器是否已经就绪并接收服务请求。如果就绪检查失败，kubernetes 将该 Pod 的 IP 地址从所有匹配的 Service 的资源池中移除掉。

- **健康检查 livenessProbe：** 确定容器是否正在运行。如果健康检查失败，kubelete 将结束该容器，并根据 restart policy（重启策略）确定是否重启该容器。

  

## 2.4 何时使用 健康检查/就绪检查？

* 如果容器中的进程在碰到问题时可以自己 重启，您并不需要执行健康检查；kubelet 可以自动的根据 Pod 的 restart policy（重启策略）执行对应的动作。
* 如果您希望在容器的进程无响应后，将容器 kill 掉并重启，则指定一个健康检查 liveness probe，并同时指定 restart policy（重启策略）为 Always 或者 OnFailure
* 如果您想在探测 Pod 确实就绪之后才向其分发服务请求，请指定一个就绪检查 readiness probe。此时，就绪检查的内容可能和健康检查相同。就绪检查适合如下几类容器：
  - **初始化时需要加载大量的数据、配置文件**
  - **启动时需要执行迁移任务**
  - **其他**



## 2.5 容器的状态（docker）

一旦 Pod 被调度到节点上，kubelet 便开始使用容器引擎（通常是 docker）创建容器。容器有三种可能的状态：Waiting / Running / Terminated：

- **Waiting：** 容器的初始状态。处于 Waiting 状态的容器，仍然有对应的操作在执行，例如：拉取镜像、应用 Secrets等。
- **Running：** 容器处于正常运行的状态。容器进入 Running 状态之后，如果指定了 postStart hook，该钩子将被执行。
- **Terminated：** 容器处于结束运行的状态。容器进入 Terminated 状态之前，如果指定了 preStop hook，该钩子将被执行。



## 2.6 重启策略

定义 Pod 或工作负载时，可以指定 restartPolicy，可选的值有：

- Always （默认值）
- OnFailure
- Never

restartPolicy 将作用于 Pod 中的所有容器。kubelete 将在五分钟内，按照递延的时间间隔（10s, 20s, 40s ......）尝试重启已退出的容器，并在十分钟后再次启动这个循环，直到容器成功启动，或者 Pod 被删除。

> 控制器 Deployment/StatefulSet/DaemonSet 中，只支持 Always 这一个选项，不支持 OnFailure 和 Never 选项。



## 2.7 Pod的存活期

通常，如果没有人或者控制器删除 Pod，Pod 不会自己消失。只有一种例外，那就是 Pod 处于 Scucceeded 或 Failed 的 phase，并超过了垃圾回收的时长（在 kubernetes master 中通过 terminated-pod-gc-threshold 参数指定），kubelet 自动将其删除。





# 3. Pod-初始化容器



## 3.1 初始化容器介绍

Pod 可以包含多个工作容器，也可以包含一个或多个初始化容器，初始化容器在工作容器启动之前执行。

初始化容器与工作容器完全相同，除了如下几点：

- 初始化容器总是运行并自动结束
- kubelet 按顺序执行 Pod 中的初始化容器，前一个初始化容器成功结束后，下一个初始化容器才开始运行。
  - 所有的初始化容器成功执行后，才开始启动工作容器
- 如果 Pod 的任意一个初始化容器执行失败，kubernetes 将反复重启该 Pod，直到初始化容器全部成功（除非 Pod 的 restartPolicy 被设定为 Never）
- 初始化容器的 Resource request / limits 处理不同，请参考 [Resources](https://kuboard.cn/learning/k8s-intermediate/workload/init-container.html#Resources)
- 初始化容器不支持 [就绪检查 readiness probe](https://kuboard.cn/learning/k8s-intermediate/workload/pod-lifecycle.html#container-probes)，因为初始化容器必须在 Pod ready 之前运行并结束

##  3.2  使用初始化容器

初始化容器可以指定不同于工作容器的镜像，这使得初始化容器相较于直接在工作容器中编写启动相关的代码更有优势：

- 初始化容器可以包含工作容器中没有的工具代码或者自定义代码。例如：您无需仅仅为了少量的 setup 工作（使用 sed, awk, python 或 dig 进行环境设定）而重新从一个基础镜像制作另外一个镜像
- 初始化容器可以更安全地执行某些使工作容器变得不安全的代码
- 应用程序的镜像构建者和部署者可以各自独立地工作，而无需一起构建一个镜像
- 初始化容器相较于工作容器，可以以另外一个视角处理文件系统。例如，他们可以拥有访问 Secrets 的权限，而工作容器却不一定被授予该权限
- 初始化容器在任何工作容器启动之前结束运行，这个特性使得我们可以阻止或者延迟工作容器的启动，直到某些前提条件得到满足。一旦前提条件满足，所有的工作容器将同时并行启动

## 3.3 Examples

下面是一些使用初始化容器的例子：

- 使用一行 shell 命令，等待某一个 Service 启动后再启动工作容器

```shell
for i in {1..100}; do sleep 1; if dig myservice; then exit 0; fi; done; exit 1
```

* 使用 Pod 的信息将其注册到某一个远程服务：

```shell
curl -X POST http://$MANAGEMENT_SERVICE_HOST:$MANAGEMENT_SERVICE_PORT/register -d 'instance=$(<POD_NAME>)&ip=$(<POD_IP>)'
```

* 等候一定时间再启动工作容器

```shell
sleep 60
```

* 将 Git repository 克隆到一个数据卷
* 根据某些参数，运行一个模板工具动态生成工作容器所需要的配置文件



## 3.4 初始化容器的行为

- Pod  的启动时，首先初始化网络和数据卷，然后按顺序执行每一个初始化容器。
  - 任何一个初始化容器都必须成功退出，才能开始下一个初始化容器。
  - 如果某一个容器启动失败或者执行失败，kubelet  将根据 Pod 的 restartPolicy 决定是否重新启动 Pod。
- 只有所有的初始化容器全都执行成功，Pod 才能进入 ready 状态。
  - 初始化容器的端口是不能够通过 kubernetes Service 访问的。
  - Pod 在初始化过程中处于 Pending 状态，并且同时有一个 type 为 `initializing` status 为 `True` 的 [Condition](https://kuboard.cn/learning/k8s-intermediate/workload/pod-lifecycle.html#pod-conditions)
- 如果 Pod 重启，所有的初始化容器也将被重新执行。
- 您可以重启、重试、重新执行初始化容器，因此初始化容器中的代码必须是 **幂等** 的。
  - 具体来说，向 emptyDir 写入文件内容的代码应该考虑到该文件已经存在的情况。请参考 [幂等](https://kuboard.cn/glossary/idempotent.html) 获得更多信息
- 您可以组合使用就绪检查和 activeDeadlineSeconds Kuboard 暂不支持，以防止初始化容器始终失败。
- Pod 中不能包含两个同名的容器（初始化容器和工作容器也不能同名）。

### 3.4.1 Resources

在确定初始化容器的执行顺序以后，以下 resource 使用规则将适用：

- 所有初始化容器中最高的 resource request/limit 是最终生效的 request/limit
- 对于 Pod 来说，最终生效的 resource request/limit 是如下几个当中较高的一个： 
  - 所有工作容器某一个 resource request/limit 的和
  - 最终生效的初始化容器的 request/limit 的和
- Kubelet 依据最终生效的 request/limit 执行调度，这意味着，在执行初始化容器时，就已经为 Pod 申请了其资源需求

## 3.5 Pod 重启的原因

Pod 重启时，所有的初始化容器都会重新执行，Pod 重启的原因可能有：

- 用户更新了 Pod 的定义，并改变了初始化容器的镜像 
  - 改变任何一个初始化容器的镜像，将导致整个 Pod 重启
  - 改变工作容器的镜像，将只重启该工作容器，而不重启 Pod
- Pod 容器基础设施被重启（例如 docker engine），这种情况不常见，通常只有 node 节点的 root 用户才可以执行此操作
- Pod 中所有容器都已经结束，restartPolicy 是 Always，且初始化容器执行的记录已经被垃圾回收，此时将重启整个 Pod



## 3.6 将容器调度到指定节点

什么情况下会用，例如：

- 确保某些 Pod 被分配到具有固态硬盘的节点
- 将相互通信频繁的两个 Pod 分配到同一个高可用区的节点

[官网说明](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)

Kubernetes 一共提供了四种方法，可以将 Pod 调度到指定的节点上，这些方法从简便到复杂的顺序如下：

- 指定节点 nodeName
- 节点选择器 nodeSelector
- Node isolation/restriction
- Affinity and anti-affinity



### 2.6.1 指定节点

nodeName 是 PodSpec 当中的一个字段。如果该字段非空，调度程序直接将其指派到 nodeName 对应的节点上运行。

使用最少的，因为有局限

- 如果 nodeName 对应的节点不存在，Pod 将不能运行
- 如果 nodeName 对应的节点没有足够的资源，Pod 将运行失败，可能的原因有：OutOfmemory /OutOfcpu
- 集群中的 nodeName 通常是变化的（新的集群中可能没有该 nodeName 的节点，指定的 nodeName 的节点可能从集群中移除）



### 2.6.2 节点选择器

nodeSelector 是 PodSpec 中的一个字段。指定了一组名值对。



#### 2.6.2.1 为节点增加labels

```shell
kubectl get nodes --show-labels
#kubectl label nodes <node-name> <label-key>=<label-value>
#例如
kubectl label nodes kubernetes-foo-node-1.c.a-robinson.internal disktype=ssd
```



#### 2.6.2.2 为pod选择节点

添加文件`pod-nginx.yaml`

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx
  labels:
    env: test
spec:
  containers:
  - name: nginx
    image: nginx
    imagePullPolicy: IfNotPresent
  nodeSelector:
    disktype: ssd   # 这个是重点
```

```shell
kubectl apply -f pod-nginx.yaml
kubectl get pods -o wide
```



### 2.6.3 Node isolation/restriction

请参考 Kubernetes 官网文档 [Node isolation/restriction](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#node-isolation-restriction)



### 2.6.4 Affinity and anti-affinity

请参考 Kubernetes 官网文档 [Affinity and anti-affinity](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#affinity-and-anti-affinity)

