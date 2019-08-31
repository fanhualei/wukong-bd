# ZooKeeper 



## 1. 入门指南

是一个分布式服务框架，是Apache Hadoop 的一个子项目，它主要是用来解决分布式应用中经常遇到的一些数据管理问题，如：统一命名服务、状态同步服务、集群管理、分布式应用配置项的管理等。



简单来说zookeeper=分布式文件系统+监听通知机制。



### 1.1. 下载

从[官方下载页面下载](http://zookeeper.apache.org/releases.html).  下载bin文件. 当前使用的版本是`3.5.5`



### 1.2. 安装单机模式

使用Standalone操作zookeeper,将下载包解压到:`/opt/modules/apache/zookeeper-3.5.5/`



> 配置zoo.cfg文件

从模板中复制一份

```
cp conf/zoo_sample.cfg conf/zoo.cfg
```

修改配置文件`dataDir=/opt/modules/apache/zookeeper-3.5.5/data`

> 启动服务

```
bin/zkServer.sh start
```

对于长期运行的生产系统，必须在外部管理ZooKeeper存储（dataDir和logs）。有关详细信息，请参阅[维护](http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance)部分。



### 1.3. 连接zkServer

可以通过shell连接到zooKeeper,创建目录,更新目录中的内容

```shell
# 打开客户端
$ bin/zkCli.sh -server 127.0.0.1:2181

# 查看帮助
$> help

# 显示根目录
$> ls /

# 创建一个目录,并将my_data写入
$> create /zk_test my_data

# 查看这个目录中的内容,显示my_data
$> get /zk_test

# 更新目录的内容
$> set /zk_test junk

# 删除这个目录
$> delete /zk_test

# 退出
$> quit
```

通过:`bin/zkServer.sh stop`关闭zkServer



### 1.4. 集群模式

集群模式很简单(例如三台):

* 配置zoo.cfg成集群模式,可以看下面例子
* `dataDir`目录中建立一个myid文件,里面写上1
* 复制到其他机器上
  * 修改myid
* 启动这三台服务器



Standalone模式下运行ZooKeeper便于评估，开发和测试。但在生产中，您应该以集群模式运行ZooKeeper。同一应用程序中的复制服务器组称为*仲裁*，在复制模式下，仲裁中的所有服务器都具有相同配置文件的副本。

注意

> 对于复制模式，至少需要三台服务器，强烈建议您使用奇数个服务器。如果您只有两台服务器，那么您处于这样的情况：如果其中一台服务器出现故障，则没有足够的机器来构成多数仲裁。两台服务器本质上**不如**单一服务器稳定，因为有两个单点故障。

复制模式所需的**conf / zoo.cfg**文件类似于独立模式中使用的文件，但有一些差异。这是一个例子：

```
tickTime=2000
dataDir=/var/lib/zookeeper
clientPort=2181
initLimit=5
syncLimit=2
server.1=zoo1:2888:3888
server.2=zoo2:2888:3888
server.3=zoo3:2888:3888
```

新条目**initLimit**是暂停ZooKeeper用于限制仲裁中ZooKeeper服务器连接到领导者的时间长度。

条目**syncLimit**限制服务器与领导者的过期时间。

使用这两个超时，您可以使用**tickTime**指定时间**单位**。在这个例子中，initLimit的超时是2000个milleseconds（滴答）或10秒的5个滴答。

表单*server.X*的条目列出构成ZooKeeper服务的服务器。当服务器启动时，它通过在数据目录中查找文件*myid*来知道它是哪个服务器。该文件包含服务器编号，ASCII格式。

最后，记下每个服务器名称后面的两个端口号：“2888”和“3888”。对等方使用以前的端口连接到其他对等方。这种连接是必要的，以便对等方可以进行通信，例如，就更新的顺序达成一致。更具体地说，ZooKeeper服务器使用此端口将关注者连接到领导者。当新的领导者出现时，跟随者使用此端口打开与领导者的TCP连接。由于默认的领导者选举也使用TCP，我们目前需要另一个端口进行领导者选举。这是服务器条目中的第二个端口。

注意

> 如果要在一台计算机上测试多个服务器，请将servername指定为*localhost*，并为该服务器中的每个server.X 指定唯一的仲裁和领导者选举端口（即上例中的2888：3888,2889：3889,2890：3890）。配置文件。当然，单独的_dataDir_s和distinct _clientPort_s也是必需的（在上面复制的例子中，在单个*localhost*上运行，你仍然会有三个配置文件）。
>
> 请注意，在一台计算机上设置多台服务器不会产生任何冗余。如果发生导致机器死亡的事情，所有zookeeper服务器都将脱机。完全冗余要求每台服务器都有自己的机器。它必须是完全独立的物理服务器。同一物理主机上的多个虚拟机仍然容易受到该主机的完全故障的影响。



## 2. 系统管理



### 2.1. 权限配置

zk还是比较重要的，为了安全需要配置其权限．

zk的节点有5种操作权限：

```
CREATE、READ、WRITE、DELETE、ADMIN 也就是 增、删、改、查、管理权限，这5种权限简写为crwda(即：每个单词的首字符缩写)
注：这5种权限中，delete是指对子节点的删除权限，其它4种权限指对自身节点的操作权限
```



身份的认证有4种方式：

```
- world：默认方式，相当于全世界都能访问
- auth：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
- digest：即用户名:密码这种方式认证，这也是业务系统中最常用的
- ip：使用Ip地址认证
```



 设置访问控制：

方式一：（推荐）

```
1）增加一个认证用户
addauth digest 用户名:密码明文
eg. addauth digest user1:password1

2）设置权限
setAcl /path auth:用户名:密码明文:权限
eg. setAcl /test auth:user1:password1:cdrwa

3）查看Acl设置
getAcl /path
```



方式二：
setAcl /path digest:用户名:密码密文:权限

注：这里的加密规则是SHA1加密，然后base64编码。



## 3. Java例子

https://www.cnblogs.com/rocky-fang/p/9030438.html











> 参考网址

* [官网地址](http://hadoop.apache.org/)

* [Zookeeper入门看这篇就够了！！](https://www.cnblogs.com/mayundalao/p/10974773.html)
* [zookeeper 事件监听机制](https://blog.csdn.net/xiaoliuliu2050/article/details/82500312)
* [zookeeper的简单搭建，java使用zk的例子和一些坑](https://www.cnblogs.com/ydymz/p/9626653.html)
* [[zookeeper\]2.zookeeper原理特性以及典型使用案例](https://www.cnblogs.com/codelifing/articles/5520721.html)

