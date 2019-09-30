# 资源控制



# LimitRange

正对每个集群进行设置

需求：集群管理为每一个pod配置Requests和Limits限制是繁琐的,
解决方案： kubernetes提供了LimitRange机制对pod和容器的Requests和Limits作出限制
LimitRange几种限制方式： 最大最小范围, Requests或Limits的默认值, limits与Requests的最大比例上限

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: mylimits
  namespace: limitrange-example
spec:
  limits:
  - max:
      cpu: "2"
      memory: 4Gi
    min:
      cpu: 200m
      memory: 6Mi
    maxLimitRequestRatio:
      cpu: 2
      memory: 2
    type: Pod

  - default:
      cpu: 300m
      memory: 200Mi
    defaultRequest:
      cpu: 200m
      memory: 100Mi
    min:
      cpu: 100m
      memory: 3Mi      
    max:
      cpu: "2"
      memory: 1Gi

    maxLimitRequestRatio:
      cpu: 5
      memory: 4
    type: Container
```

```shell
#查看limitrange对象信息
kubectl describe limitrange mylimits -n limitrange-example
```



# ResourceQuota

esource Quotas（资源配额，简称quota）是对namespace进行资源配额，限制资源使用的一种策略。 K8S是一个多用户架构，当多用户或者团队共享一个K8S系统时，SA使用quota防止用户（基于namespace的）的资源抢占，定义好资源分配策略。

Quota应用在Namespace上，默认情况下，没有Resource Quota的，需要另外创建Quota，并且每个Namespace最多只能有一个Quota对象。
————————————————
版权声明：本文为CSDN博主「lykops」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/liyingke112/article/details/77369427





