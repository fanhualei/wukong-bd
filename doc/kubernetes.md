# Kubernetes



> 目录

* [虚拟机安装](virtualbox.md)

* [kubernetes安装](kubernetes-install.md)

* [Kubernetes 入门](kubernetes-quict-start.md)

* Kubernetes 进阶

  * [使用docker私有仓库](https://segmentfault.com/a/1190000015108428)
  * [Pod详解](kubernetes-detail-pod.md)
  * [控制器详解](kubernetes-detail-controller.md)
  * [service详解](kubernetes-detail-service.md)
  * [volume存储详解](kubernetes-detail-volume.md)
  
* 管理员

  * [给NameSpace分配权限](kubernetes-admin-usercount.md)
  * [不同NameSpace网络隔离](kubernetes-network.md)
  * [资源配置](kubernetes-admin-resource.md)
  * [NFS网络存储](nfs.md)

* 开发人员

  * [kubernetes互动教程](kubernetes-code.md)
  * [docker基本技巧](docker-menu.md)
* [helm使用](kubernetes-helm.md)
  
  



# [Linux ss命令详解](https://www.cnblogs.com/ftl1012/p/ss.html)



以前使这样的，有个项目要上线了，需要几台机器。网管就给他几台物理机或者虚拟机，以及外网的IP。

然后程序员，就去在不同的机器上部署程序。程序员要干这些事情：

1、安装操作系统

2、配置安全策略

3、安装应用软件

4、安装监控软件。

5、定期查看日志

6、随着时间推移，可能要升级底层软件，例如JDK版本,python版本。

7、在升级的过程，可能又版本冲突，无法升级，比如通一台机器会安装多个系统，他们可能对版本对要求不一样。

8、物理机与宿主机的使用率不高。



**引入k8s后，实际上网管的工作要求提高，程序会明确向网管提出一些更高级的要求。例如：**

1、需要一个centos的操作系统，并安装了Nginx环境。只用让我把网页与配置文件穿上去就行了。

2、我需要一个mysql的集群，给我一个可以用的IP地址就行，备份与负载均衡，网管来做。

3、我需要一个NFS网络存储空间，网管给我分配一个IP地址就行。

4、我需要动态的增加我的服务器，当网络流量增加时，也可以动态的减少。

5、kafka、redis、elasticsearch、RocketMQ、这些服务器，我想要的话，最后一分钟就能给我生成。

6、我想了解所以机器的负载情况。原先需要20台机器，现在只需要10台可以了。这个是领导想要的

7、我想看到每台服务器每台的情况，如果出现故障，能自动修复故障，或者迁移到其他机器上。

8、我想自动化的部署我的应用程序，如果出现错误的，一条命令，就可以进行回滚，回滚到我自己想要的版本。

9、我想发布AB两个版本的程序同时在线，看看那个版本，用户的使用效果比较好。

10、今后那些复杂的集群主从，都能一键部署，让网管来做。程序员就复杂开发业务代码。

11、那些hadoop hbase等大数据工具，最后也能一键部署。



**可能遇到的困难**

1、学习成本比较高。 学习与熟悉，需要1个月。 建议分步实施与部署，逐步把老的系统迁移到新的集群中，这个过程，需要6个月。

2、网管不干了，原先的责任小，就负责网络的问题。现在要向外提供服务。

3、网管担心，今后扯皮的事情多了，以前提供一台物理机，现在提供一个基础平台，项目出现的问题，会推到平台上。

4、规模小，机房的机器小于10台，或者应用的系统比较小，也没有必要用这套东西。 但是建议用docket，这个比原先的好多了。





## 参考文档

* 官网：https://kubernetes.io/zh

* [Kubernetes的几种主流部署方式01-minikube部署](https://segmentfault.com/a/1190000018607114)
* [最简单的kubernetes入门实验教程](https://www.jianshu.com/p/f4c2104ba90a)
  * 使用了kubeadm一步安装
* [Kubernetes中文社区](http://docs.kubernetes.org.cn/)
* [Kubernetes 安装文档推荐](https://www.kubernetes.org.cn/5650.html)
* [10分钟搭建Kubernetes容器集群平台（kubeadm）](https://blog.51cto.com/lizhenliang/2296100?tdsourcetag=s_pcqq_aiomsg)

