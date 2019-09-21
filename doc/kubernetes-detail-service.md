> 目录

[TOC]







# 1. Service概述



## 1.1. 为何需要 Service

Kubernetes 中 Pod 是随时可以消亡的（节点故障、容器内应用程序错误等原因）。如果使用 [Deployment](https://kuboard.cn/learning/k8s-intermediate/service/learning/k8s-intermediate/workload/wl-deployment/) 运行您的应用程序，Deployment 将会在 Pod 消亡后再创建一个新的 Pod 以维持所需要的副本数。每一个 Pod 有自己的 IP 地址，然而，对于 Deployment 而言，对应 Pod 集合是动态变化的。

这个现象导致了如下问题：

- 如果某些 Pod（假设是 'backends'）为另外一些 Pod（假设是 'frontends'）提供接口，在 'backends' 中的 Pod 集合不断变化（IP 地址也跟着变化）的情况下，'frontends' 中的 Pod 如何才能知道应该将请求发送到哪个 IP 地址？

Service 存在的意义，就是为了解决这个问题。



## 1.2 Kubernetes Service

Kubernetes 中 Service 是一个 API 对象，通过 kubectl + YAML ，定义一个 Service，可以将符合 Service 指定条件的 Pod 作为可通过网络访问的服务提供给服务调用者。

Service 是 Kubernetes 中的一种服务发现机制：

- Pod 有自己的 IP 地址
- Service 被赋予一个唯一的 dns name
- Service 通过 label selector 选定一组 Pod
- Service 实现负载均衡，可将请求均衡分发到选定这一组 Pod 中

例如，假设有一个无状态的图像处理后端程序运行了 3 个 Pod 副本。这些副本是相互可替代的（前端程序调用其中任何一个都可以）。在后端程序的副本集中的 Pod 经常变化（销毁、重建、扩容、缩容等）的情况下，前端程序不应该关注这些变化。

Kubernetes 通过引入 Service 的概念，将前端与后端解耦。



## 1.3 架构图

使用 Kubernetes 的最佳实践：

- Service 与 Controller 同名
- Service 与 Controller 使用相同的 label selector



![alt](imgs/image-20190917210501081.926247e9.png)



# 2. Service详细描述



## 2.1 创建 Service

Kubernetes Servies 是一个 RESTFul 接口对象，可通过 yaml 文件创建。

> 例如，假设您有一组 Pod：

- 每个 Pod 都监听 9376 TCP 端口
- 每个 Pod 都有标签 app=MyApp

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app: MyApp
  ports:
    - protocol: TCP
      # 这里可以定义name进行解耦
      port: 80  # service的端口
      targetPort: 9376 #pod的端口
```



> 上述 YAML 文件可用来创建一个 Service：

- 名字为 `my-service`
- 目标端口未 TCP 9376
- 选取所有包含标签 app=MyApp 的 Pod



>  关于 Service，您还需要了解：

- Kubernetes 将为该 Service 分配一个 IP 地址（ClusterIP 或 集群内 IP），供 Service Proxy 使用（参考[虚拟 IP 和 Service proxy](https://kuboard.cn/learning/k8s-intermediate/service/service-details.html#虚拟-ip-和-service-proxy)）
- Kubernetes 将不断扫描符合该 selector 的 Pod，并将最新的结果更新到与 Service 同名 `my-service` 的 Endpoint 对象中。
  - Service 从自己的 IP 地址和 `port` 端口接收请求，并将请求映射到符合条件的 Pod 的 `targetPort`。为了方便，默认 `targetPort`的取值 与 `port` 字段相同

- Pod 的定义中，Port 可能被赋予了一个名字，您可以在 Service 的 `targetPort` 字段引用这些名字，而不是直接写端口号。这种做法可以使得您在将来修改后端程序监听的端口号，而无需影响到前端程序。
- Service 的默认传输协议是 TCP，您也可以使用其他 [支持的传输协议](https://kuboard.cn/learning/k8s-intermediate/service/service-details.html#支持的传输协议)。
- Kubernetes Service 中，可以定义多个端口，不同的端口可以使用相同或不同的传输协议。



## 2.2 创建 Service（无 label selector）

主要为了指向外部的一个IP.

例如：

- 您想要在生产环境中使用一个 Kubernetes 外部的数据库集群，在测试环境中使用 Kubernetes 内部的 数据库
- 您想要将 Service 指向另一个名称空间中的 Service，或者另一个 Kubernetes 集群中的 Service
- 您正在将您的程序迁移到 Kubernetes，但是根据您的迁移路径，您只将一部分后端程序运行在 Kubernetes 中。其他的还运行在kubernetes以外

在上述这些情况下，您可以定义一个没有 Pod Selector 的 Service。例如：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  ports:
    - protocol: TCP
      port: 80
      targetPort: 9376
```

因为该 Service 没有 selector，相应的 Endpoint 对象就无法自动创建。您可以手动创建一个 Endpoint 对象，以便将该 Service 映射到后端服务真实的 IP 地址和端口：

```yaml
apiVersion: v1
kind: Endpoints
metadata:
  name: my-service
subsets:
  - addresses:
      - ip: 192.0.2.42
    ports:
      - port: 9376
```



> - Endpoint 中的 IP 地址不可以是 loopback（127.0.0.0/8 IPv4 或 ::1/128 IPv6），或 link-local（169.254.0.0/16 IPv4、224.0.0.0/24 IPv4 或 fe80::/64 IPv6）
> - Endpoint 中的 IP 地址不可以是集群中其他 Service 的 ClusterIP



对于 Service 的访问者来说，Service 是否有 label selector 都是一样的。在上述例子中，Service 将请求路由到 Endpoint 192.0.2.42:9376 (TCP)。

ExternalName Service 是一类特殊的没有 label selector 的 Service，该类 Service 使用 DNS 名字。参考 [ExternalName](https://kuboard.cn/learning/k8s-intermediate/service/service-details.html#externalname)



## 2.3 虚拟 IP 和服务代理

Kubernetes 集群中的每个节点都运行了一个 `kube-proxy`，负责为 Service（ExternalName 类型的除外）提供虚拟 IP 访问。



### 2.3.1 为何不使用 round-robin DNS

- 一直以来，DNS 软件都不确保严格检查 TTL（Time to live），并且在缓存的 dns 解析结果应该过期以后，仍然继续使用缓存中的记录
- 某些应用程序只做一次 DNS 解析，并一直使用缓存下来的解析结果
- 即使应用程序对 DNS 解析做了合适的处理，为 DNS 记录设置过短（或者 0）的 TTL 值，将给 DNS 服务器带来过大的负载



### 2.3.2 三种代理模式

Kubernetes 支持三种 proxy mode（代理模式），他们的版本兼容性如下：

| 代理模式              | Kubernetes 版本 | 是否默认 | 备注     |
| --------------------- | --------------- | -------- | -------- |
| User space proxy mode | v1.0 +          |          | 快废弃了 |
| Iptables proxy mode   | v1.1 +          | 默认     |          |
| Ipvs proxy mode       | v1.8 +          |          | 推荐使用 |



#### 2.3.2.1 IPVS 代理模式

**IPVS 模式的优点**

IPVS proxy mode 基于 netfilter 的 hook 功能，与 iptables 代理模式相似，但是 IPVS 代理模式使用 hash table 作为底层的数据结构，并在 kernel space 运作。这就意味着

- IPVS 代理模式可以比 iptables 代理模式有更低的网络延迟，在同步代理规则时，也有更高的效率
- 与 user space 代理模式 / iptables 代理模式相比，IPVS 模式可以支持更大的网络流量



> - 如果要使用 IPVS 模式，您必须在启动 kube-proxy 前为节点的 linux 启用 IPVS
> - kube-proxy 以 IPVS 模式启动时，如果发现节点的 linux 未启用 IPVS，则退回到 iptables 模式



### 2.3.3 代理模式总结

在所有的代理模式中，发送到 Service 的 IP:Port 的请求将被转发到一个合适的后端 Pod，而无需调用者知道任何关于 Kubernetes/Service/Pods 的细节。

Service 中额外字段的作用：

- ```
  service.spec.sessionAffinity
  ```

  - 默认值为 "None"
  - 如果设定为 "ClientIP"，则**同一个客户端的连接将始终被转发到同一个 Pod**

- ```
  service.spec.sessionAffinityConfig.clientIP.timeoutSeconds
  ```

  - 默认值为 10800 （3 小时）
  - 设定会话保持的持续时间



## 2.4 多端口的Service

Kubernetes 中，您可以在一个 Service 对象中定义多个端口，此时，您必须为每个端口定义一个名字。如下所示：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app: MyApp
  ports:
    - name: http
      protocol: TCP
      port: 80
      targetPort: 9376
    - name: https
      protocol: TCP
      port: 443
      targetPort: 9377
```

>端口的名字必须符合 Kubernetes 的命名规则，且，端口的名字只能包含小写字母、数字、`-`，并且必须以数字或字母作为开头及结尾。
>
>例如：
>
>合法的 Port 名称：`123-abc`、`web`
>
>非法的 Port 名称：`123_abc`、`-web`



## 2.5 使用自定义的 IP 地址

创建 Service 时，如果指定 `.spec.clusterIP` 字段，可以使用自定义的 Cluster IP 地址。该 IP 地址必须是 APIServer 中配置字段 `service-cluster-ip-range` CIDR 范围内的合法 IPv4 或 IPv6 地址，否则不能创建成功。

可能用到自定义 IP 地址的场景：

- 想要重用某个已经存在的 DNS 条目
- 遗留系统是通过 IP 地址寻址，且很难改造



## 2.6 服务发现

Kubernetes 支持两种主要的服务发现模式：

- 环境变量
- DNS



### 2.6.1 环境变量

kubelet 查找有效的 Service，并针对每一个 Service，向其所在节点上的 Pod 注入一组环境变量。支持的环境变量有：

- [Docker links 兼容](https://docs.docker.com/network/links/) 的环境变量
- {SVCNAME}_SERVICE_HOST 和 {SVCNAME}_SERVICE_PORT
  - Service name 被转换为大写
  - 小数点 `.` 被转换为下划线 `_`

例如，Service `redis-master` 暴露 TCP 端口 6379，其 Cluster IP 为 10.0.0.11，对应的环境变量如下所示：

```
REDIS_MASTER_SERVICE_HOST=10.0.0.11
REDIS_MASTER_SERVICE_PORT=6379
REDIS_MASTER_PORT=tcp://10.0.0.11:6379
REDIS_MASTER_PORT_6379_TCP=tcp://10.0.0.11:6379
REDIS_MASTER_PORT_6379_TCP_PROTO=tcp
REDIS_MASTER_PORT_6379_TCP_PORT=6379
REDIS_MASTER_PORT_6379_TCP_ADDR=10.0.0.11
```



**环境变量的局限**

>如果要在 Pod 中使用基于环境变量的服务发现方式，必须先创建 Service，再创建调用 Service 的 Pod。否则，Pod 中不会有该 Service 对应的环境变量。
>
>如果使用基于 DNS 的服务发现，您无需担心这个创建顺序的问题



### 2.6.2 DNS

如果按照 www.kuboard.cn 上的文档安装 Kubernetes，默认已经安装了 DNS 服务，[Core DNS](https://coredns.io/)。

CoreDNS 监听 Kubernetes API 上创建和删除 Service 的事件，并为每一个 Service 创建一条 DNS 记录。集群中所有的 Pod 都可以使用 DNS Name 解析到 Service 的 IP 地址。

例如，名称空间 `my-ns` 中的 Service `my-service`，将对应一条 DNS 记录 `my-service.my-ns`。 

名称空间 `my-ns` 中的Pod可以直接 `nslookup my-service` （`my-service.my-ns` 也可以）。

其他名称空间的 Pod 必须使用 `my-service.my-ns`。`my-service` 和 `my-service.my-ns` 都将被解析到 Service 的 Cluster IP。

Kubernetes 同样支持 DNS SRV（Service）记录，用于查找一个命名的端口。假设 `my-service.my-ns` Service 有一个 TCP 端口名为 `http`，则，您可以 `nslookup _http._tcp.my-service.my-ns` 以发现该Service 的 IP 地址及端口 `http`

对于 `ExternalName` 类型的 Service，只能通过 DNS 的方式进行服务发现。参考 [Service/Pod 的 DNS](https://kuboard.cn/learning/k8s-intermediate/service/dns.html)

```shell
# 本namespace
nslookup my-service

# 发现该Service 的 IP 地址及端口
nslookup _http._tcp.my-service.my-ns
```



## 2.7 无头Services

用于自己开发负载均衡策略



“Headless” Service 不提供负载均衡的特性，也没有自己的 IP 地址。创建 “headless” Service 时，只需要指定 `.spec.clusterIP` 为 "None"。



**“Headless” Service 可以用于对接其他形式的服务发现机制，而无需与 Kubernetes 的实现绑定。**

因为没有ClusterIP，kube-proxy 并不处理此类服务，因为没有load balancing或 proxy 代理设置，在访问服务的时候回返回后端的全部的Pods IP地址，**主要用于开发者自己根据pods进行负载均衡器的开发**(设置了selector)。



对于 “Headless” Service 而言：

- 没有 Cluster IP
- kube-proxy 不处理这类 Service
- Kubernetes不提供负载均衡或代理支持



DNS 的配置方式取决于该 Service 是否配置了 selector：

- 配置了 Selector

  Endpoints Controller 创建 `Endpoints` 记录，并修改 DNS 配置，使其直接返回指向 selector 选取的 Pod 的 IP 地址

- 没有配置 Selector

  Endpoints Controller 不创建 `Endpoints` 记录。DNS服务返回如下结果中的一种：

  - 对 ExternalName 类型的 Service，返回 CNAME 记录
  - 对于其他类型的 Service，返回与 Service 同名的 `Endpoints` 的 A 记录



## 2.8 虚拟 IP 的实现

如果只是想要正确使用 Service，不急于理解 Service 的实现细节，您无需阅读本章节。

### 2.8.1 避免冲突

Kubernetes 的一个设计哲学是：尽量避免非人为错误产生的可能性。就设计 Service 而言，Kubernetes 应该将您选择的端口号与其他人选择的端口号隔离开。为此，Kubernetes 为每一个 Service 分配一个该 Service 专属的 IP 地址。

为了确保每个 Service 都有一个唯一的 IP 地址，kubernetes 在创建 Service 之前，先更新 etcd 中的一个全局分配表，如果更新失败（例如 IP 地址已被其他 Service 占用），则 Service 不能成功创建。

Kubernetes 使用一个后台控制器检查该全局分配表中的 IP 地址的分配是否仍然有效，并且自动清理不再被 Service 使用的 IP 地址。

### 2.8.2 Service 的 IP 地址

Pod 的 IP 地址路由到一个确定的目标，然而 Service 的 IP 地址则不同，通常背后并不对应一个唯一的目标。 kube-proxy 使用 iptables （Linux 中的报文处理逻辑）来定义虚拟 IP 地址。当客户端连接到该虚拟 IP 地址时，它们的网络请求将自动发送到一个合适的 Endpoint。Service 对应的环境变量和 DNS 实际上反应的是 Service 的虚拟 IP 地址（和端口）。

> 有三种模式

* Userspace  不推荐
* iptables  默认，不是最好的
* IPVS   推荐

在一个大型集群中（例如，存在 10000 个 Service）iptables 的操作将显著变慢。IPVS 的设计是基于 in-kernel hash table 执行负载均衡。因此，使用 IPVS 的 kube-proxy 在 Service 数量较多的情况下仍然能够保持好的性能。同时，基于 IPVS 的 kube-proxy 可以使用更复杂的负载均衡算法（最少连接数、基于地址的、基于权重的等）



## 2.9 支持的传输协议

* TCP
  * 默认值。任何类型的 Service 都支持 TCP 协议。
* UDP
  * 大多数 Service 都支持 UDP 协议。对于 LoadBalancer 类型的 Service，是否支持 UDP 取决于云供应商是否支持该特性。
* HTTP
  * 如果您的云服务商支持，您可以使用 LoadBalancer 类型的 Service 设定一个 Kubernetes 外部的 HTTP/HTTPS 反向代理，将请求转发到 Service 的 Endpoints。
  * 需要使用`Ingress`
* Proxy Protocol 
  * 如果您的云服务上支持（例如 AWS），您可以使用 LoadBalancer 类型的 Service 设定一个 Kubernetes 外部的负载均衡器，并将连接已 PROXY 协议转发到 Service 的 Endpoints。
* SCTP
  * 尚处于 `alpha` 阶段，暂不推荐使用。



# 3. 可用的Service类型

Kubernetes Service 支持的不同访问方式。

Kubernetes 中可以通过不同方式发布 Service，通过 `ServiceType` 字段指定，该字段的默认值是 `ClusterIP`，可选值有：

- **ClusterIP**:  默认值。通过集群内部的一个 IP 地址暴露 Service，只在集群内部可以访问

- **NodePort**:  通过每一个节点上的的静态端口（NodePort）暴露 Service，同时自动创建 ClusterIP 类型的访问方式

  - 在集群内部通过 $(ClusterIP): $(Port) 访问
  - 在集群外部通过 $(NodeIP): $(NodePort) 访问

- **LoadBalancer**:  通过云服务供应商（AWS、Azure、GCE 等）的负载均衡器在集群外部暴露 Service，同时自动创建 NodePort 和 ClusterIP 类型的访问方式

  - 在集群内部通过 $(ClusterIP): $(Port) 访问
  - 在集群外部通过 $(NodeIP): $(NodePort) 访问
  - 在集群外部通过 $(LoadBalancerIP): $(Port) 访问

- **ExternalName**: Kuboard 暂不支持 将 Service 映射到 `externalName` 指定的地址（例如：foo.bar.example.com），返回值是一个 CNAME 记录。不使用任何代理机制。

  

## 3.1 ClusterIP

ClusterIP 是 ServiceType 的默认值。在 [Iptables 代理模式](https://kuboard.cn/learning/k8s-intermediate/service/service-details.html#iptables-代理模式) 中，详细讲述了 ClusterIP 类型 Service 的工作原理。



## 3.2 NodePort

> 基本功能

* 对于同一 Service，在每个节点上的节点端口都相同
  * 您可以通过 `nodePort` 字段指定节点端口号，但不能重复，也必须在范围内
* 该端口的范围 ,默认是：30000-32767
* 节点将该端口上的网络请求转发到对应的 Service 上。
* 在启动 kube-proxy 时使用参数 `--nodeport-address` 可指定阶段端口可以绑定的 IP 地址段。该参数接收以逗号分隔的 CIDR 作为参数值（例如：10.0.0.0/8,192.0.2.0/25），kube-proxy 将查找本机符合该 CIDR 的 IP 地址，并将节点端口绑定到符合的 IP 地址上。



> 使用 NodePort，您可以：

- 根据自己的需要配置负载均衡器
- 配置 Kubernetes / 非 Kubernetes 的混合环境
- 直接暴露一到多个节点的 IP 地址，以便客户端可访问 Kubernetes 中的 Service



> NodePort 类型的 Service 可通过如下方式访问：

- 在集群内部通过 $(ClusterIP): $(Port) 访问
- 在集群外部通过 $(NodeIP): $(NodePort) 访问



## 3.3 LoadBalancer

在支持外部负载均衡器的云环境中（例如 GCE、AWS、Azure 等），将 `.spec.type` 字段设置为 `LoadBalancer`，Kubernetes 将为该Service 自动创建一个负载均衡器。负载均衡器的创建操作异步完成，您可能要稍等片刻才能真正完成创建，负载均衡器的信息将被回写到 Service 的 `.status.loadBalancer` 字段。如下所示：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app: MyApp
  ports:
    - protocol: TCP
      port: 80
      targetPort: 9376
  clusterIP: 10.0.171.239
  loadBalancerIP: 78.11.24.19
  type: LoadBalancer
status:
  loadBalancer:
    ingress:
      - ip: 146.148.47.155
```

发送到外部负载均衡器的网络请求就像被转发到 Kubernetes 中的后端 Pod 上。负载均衡的实现细节由各云服务上确定。

关于更多 LoadBalancer Service 相关的描述，请参考 [Type LoadBalancer](https://kubernetes.io/docs/concepts/services-networking/service/#loadbalancer) 和您所使用的云供应商的文档



## 3.4 ExternalName

ExternalName 类型的 Service 映射到一个外部的 DNS name，而不是一个 pod label selector。可通过 `spec.externalName` 字段指定外部 DNS name。

下面的例子中，名称空间 `prod` 中的 Service `my-service` 将映射到 `my.database.example.com`：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
  namespace: prod
spec:
  type: ExternalName
  externalName: my.database.example.com
```



测试一下，看看返回值

```shell
#集群的 DNS 服务将返回一个 CNAME 记录，其对应的值为 my.database.example.com
$ nslookup my-service.prod.svc.cluster.local
```



访问 `my-service` 与访问其他类型的 Service 相比，网络请求的转发发生在 DNS level，而不是使用 proxy。如果您在后续想要将 `my.database.example.com` 对应的数据库迁移到集群内部来，您可以按如下步骤进行：

1. 在 Kubernetes 中部署数据库（并启动数据库的 Pod）
2. 为 Service 添加合适的 selector 和 endpoint
3. 修改 Service 的类型

> 注意事项
>
> - ExternalName 可以接受一个 IPv4 地址型的字符串作为 `.spec.externalName` 的值，但是这个字符串将被认为是一个由数字组成的 DNS name，而不是一个 IP 地址。
> - 如果要 必须写 一个 IP 地址，请考虑使用 [headless Service](https://kuboard.cn/learning/k8s-intermediate/service/service-details.html#headless-services)



## 3.5 External IP

如果有外部 IP 路由到 Kubernetes 集群的一个或多个节点，Kubernetes Service 可以通过这些 `externalIPs` 进行访问。`externalIP` 需要由集群管理员在 Kubernetes 之外配置。

在 Service 的定义中， `externalIPs` 可以和任何类型的 `.spec.type` 一通使用。在下面的例子中，客户端可通过 `80.11.12.10:80`（externalIP:port） 访问`my-service`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app: MyApp
  ports:
    - name: http
      protocol: TCP
      port: 80
      targetPort: 9376
  externalIPs:
    - 80.11.12.10
```

注：自己的理解 `将一个外部地址，不指向外部了，指向内部的的pod`

