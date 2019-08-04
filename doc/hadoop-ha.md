

# 搭建集群环境



## 1：做三台虚拟机

## 2：进行时间同步

将其中一台做为时间服务器,也可以同步外网的服务器时间

```
service ntpd status
```

然后做一个定时任务来同步时间





## 3：服务器规划

第一台机器：

* namenode
* datanode
* nodemanager
* 

第二台机器：

* datanode
* nodemanager
* resourcemanager
  * 不放在第一台机器上，为了是让namenode服务器性能更平均

第三台机器:

* datanode
* nodemananger
* historyserver
  * 这个服务比较小
* secondarynamenode



## 4：开始配置

* 配置core-site.xml
  * 指定服务器地址hdfs://*****:9000
  * 指定存储路径
* hdfs-site.xml
  * 指定第三台机器为sencondarynamenode



如果在windows下配置的虚拟机，还要在windows下配置hosts文件，可以访问到这三天机器



## 5：免登陆配置

这样三台可以相关登录

配置完毕后，可以将配置好的hadoop目录复制到其他的服务器上。





## 6：启动服务





## 7：验证是否可以用



* 上传一个文件
  * 可以在任意一台机器上上传一个文件
* 测试yarn
  * 在第三台测试hadoop的例子文件



## 8：配置namendoe多个

* 只能有一个namenode对外提供服务（active,stanby>
* 多个namenode的元数据信息必须保持一致
  * JOURNALNODE替代了secondarynamenode
* 必须有一个代理，让client知道那一台namenode对外提供服务。
  * 必须在一个地方记录那个是active，



### 8.1：zookeeper

>  用途

* hadoop中记录了active机器
* Hbase中记录了 hbase集群的请求入口
* kafaka中，记录生产者与消费者与偏移量



> 搭建

* 单机版
  * 解压
  * 指定存储路径
  * 启动
    * sbin/zkServer.sh start
  * 查看状态
    * sbin/zkServer.sh status
  * jps 看zk的进程
* 集群版
  *  1：解压
  * 2：修改conf/zoo.cfg文件
  * 3：指定zk的存放路径
    * 指定到datadir={zk-home/data/zkData}
  * 4：指定所有zk的节点与端口号
    * server.1=ddkdkdkk
  * 5：在datadir目录下创建myid文件
    * 在这个文件中写入自己的编号
  * 6：将配置分发到其他机器上
    * 也就是将zk的程序与目录都分发到其他节点
  * 7：分发完毕后，修改每个机器上的myid文件
  * 8：启动所有节点
  * 9：查看所有节点



### 8.2：journalNode 



## 9：启动过程



### 9.1:配置手动切换



* 启动zookeeper(三台都要启动)
  
  * bin/zkServer.sh start
* 启动三台journalnode(这个是用来同步两台namenode的数据的)
  
  * sbin/hadoop-deamon.sh start journalnode
* 操作namenode(只格式化一台，另外一台会自动同步，不能两台都格式化)
  * 第一台namenode
    * bin/hdfs namenode -format
    * 启动namenode: sbin/hadoop-deamon.sh start namenode
  * 第二台namenode
    * 同步数据：bin/hdfs namenode -bootstrapStandby
    * 启动namenode:sbin/hadoop-deamon.sh start namenode
* 查看web(两台都是stanby)
  * http://host1:50070
  * http://host2:50070

* 手工切换namenode状态

  * bin/hdfs haadmin -transitionToactive nn1
  * 

  ### 9.2：配置自动转移

* 配置自动转移，三台都配置
* 格式化zkfc
  
  * bin/hdfs zkfc -formatZKs





> 参考文档

* [关于Hadoop-HA的配置---从零开始](https://blog.csdn.net/lsr40/article/details/77165453)
* [官方的HA的安装方法](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/ClusterSetup.html)
* [官方Yarn的HA安装](http://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/ResourceManagerHA.html)







