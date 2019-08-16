# flume使用

官网地址：http://flume.apache.org/



[TOC]





## 1. 概述



### 1.1. 背景

 flume是由cloudera软件公司产出的可分布式日志收集系统，后与2009年被捐赠了apache软件基金会，为hadoop相关组件之一。尤其近几年随着flume的不断被完善以及升级版本的逐一推出，特别是flume-ng;同时flume内部的各种组件不断丰富，用户在开发的过程中使用的便利性得到很大的改善，现已成为apache top项目之一.



### 1.2. 什么是flume?

apache Flume 是一个从可以收集例如日志，事件等数据资源，并将这些数量庞大的数据从各项数据资源中集中起来存储的工具/服务，或者数集中机制。flume具有高可用，分布式，配置工具，其设计的原理也是基于将数据流，如日志数据从各种网站服务器上汇集起来存储到HDFS，HBase等集中存储器中。其结构如下图所示：

![alt](https://images2015.cnblogs.com/blog/539316/201607/539316-20160710192339483-1093743457.jpg)



### 1.3. 应用场景
比如我们在做一个电子商务网站，然后我们想从消费用户中访问点特定的节点区域来分析消费者的行为或者购买意图. 这样我们就可以更加快速的将他想要的推送到界面上，实现这一点，我们需要将获取到的她访问的页面以及点击的产品数据等日志数据信息收集并移交给Hadoop平台上去分析.而Flume正是帮我们做到这一点。现在流行的内容推送，比如广告定点投放以及新闻私人定制也是基于次，不过不一定是使用FLume,毕竟优秀的产品很多，比如facebook的Scribe，还有Apache新出的另一个明星项目chukwa，还有淘宝Time Tunnel。

[开源日志系统比较：scribe、chukwa、kafka、flume](https://www.cnblogs.com/likehua/p/3796826.html)



### 1.4. 相关介绍文章

* [Flume概念与原理、与Kafka优势对比](https://blog.csdn.net/gyshun/article/details/79710534)
* [Flume构建日志采集系统](https://www.jianshu.com/p/1183139ed3a0)
* [Flume初识与部署](http://blog.itpub.net/31441024/viewspace-2199588/)
* [部署Flume，大数据采集又快又稳！](http://rdc.hundsun.com/portal/article/941.html)



### 1.5. Flume原型图

![alt](https://upload-images.jianshu.io/upload_images/1784853-4015c20dd50bef6f.png?imageMogr2/auto-orient/)



### 1.6. Flume基本组件

- Event：消息的基本单位，有header和body组成
- Agent：JVM进程，负责将一端外部来源产生的消息转 发到另一端外部的目的地
  - Source：从外部来源读入event，并写入channel
  - Channel：event暂存组件，source写入后，event将会 一直保存,
  - Sink：从channel读入event，并写入目的地



### 1.7. Flume事件流

![alt](https://upload-images.jianshu.io/upload_images/1784853-1aa8fbe82b1057cd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)





### 1.8. 应用场景

* 日志--->Flume--->实时计算（如kafka/MQ+Storm/Spark Streaming）
* 日志--->Flume--->离线计算（如ODPS、HDFS、HBase）
* 日志--->Flume--->ElasticSearch等。



#### 1.8.1. 实时处理系统

下面结合一个大数据实时处理系统（Flume+Kafka+Spark Streaming+Redis）阐述下Flume在实际应用中所扮演的重要角色。该实时处理系统整体架构如下：

![alt](http://rdc.hundsun.com/portal/data/upload/201812/f_c50a0a6b604cbb4c10f4009e37b13b60.png)





#### 1.8.2. 日志采集系统

比如我们要实时统计用户在某个网站上的PV（页面浏览量）、UV（独立访客），那么，对于Flume而言，它的作用就是在于采集用户数据，并且将其发送到kafka集群中指定的topic上。

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_4210c951ca51fb6ba036eac69792360f.png)

在我们的场景中，需要配置三个Flume Agent，其中两个Flume Agent分别部署在两台Web服务器上，用来采集Web服务器上的日志数据，然后其数据的下沉方式都发送到另外一个Flume Agent上。



##### 1.8.2.1. 配置采集客户端

部署在Web服务器上的两个Flume Agent添加配置文件flume-sink-avro.conf，其配置内容如下：

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_9c3b22ab980d230aa31104c19e6b5740.png)

配置完成后，启动Flume Agent，即可对日志文件进行监听：

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_2c48553d8b2d78d4b80cb8591e8fd683.png)



##### 1.8.2.2. 配置聚合服务器

Flume Consolidation Agent添加配置文件flume-source_avro-sink_kafka.conf，其配置内容如下：

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_b3742a5cbea16f9ea41cfed3673e81b9.png)

配置完成后，启动Flume Agent，即可对avro的数据进行监听：

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_1a1d7937ed6f503b3e14f676aff25c9d.png)

完成上述操作后，如果在Web服务器上有新增的日志数据，就会被我们的Flume程序监听到，并且最终会传输到Kafka的f-k-s topic中，通过Flume强大的数据采集功能，为整个实时处理系统提供了数据保障，之后就可以进行后续的一系列操作。

另外，想要利用Flume采集到更有价值、更符合各自业务需求的数据，我们不得不谈到Flume的事务及拦截器的功劳。



### 1.9. 事务处理

事务保证了数据的可用性（有别于数据库中的事务）。下图的数据流是spooling directory source-> memory channel-> kafka sink，其中memory channel维护了两个事务，分别是PUT事务和Take事务。

![img](http://rdc.hundsun.com/portal/data/upload/201812/f_dc180624b263929292c00c34fafa9770.png)

1）PUT事务

（1）批量数据循环PUT到putList中；

（2）Commit，把putList队列中的数据offer到queue队列中，然后释放信号量，清空（clear）putList队列；

（3）Rollback，清空（clear）putList队列。

2）Take事务

（1）检查takeList队列大小是否够用，从queue队列中poll；

（2）Event到takeList队列中；

（3）Commit，表明被Sink正确消费掉，清空（clear）takeList队列；

（4）Rollback，异常出现，则把takeList队列中的Event返还到queue队列顶部。





## 2. Flume搭建

### 2.1.下载二进制安装包

下载地址：[http://flume.apache.org/download.html](https://link.jianshu.com/?t=http%3A%2F%2Fflume.apache.org%2Fdownload.html)



### 2.2.安装Flume

解压缩安装包文件

```shell
$ tar -zxvf apache-flume-1.9.0-bin.tar.gz 
```



### 2.3. 创建软连接【此步骤可省略】

```shell
$ ln -s /opt/modules/apache/flume-1.9.0 /usr/local/flume
```

有的教程，会提示要修改 `conf/flumen-env.sh` 中的`JAVA_HOME` ，也可以不用配置。但是如果不想使用系统默认的java，也可以单独设置这个选项。



### 2.4. 配置环境变量

编辑 /etc/profile文件，增加以下内容：

```
export FLUME_HOME=/opt/modules/apache/flume-1.9.0
export PATH=$PATH:${FLUME_HOME}/bin
```

启动环境变量

```shell
$ source /etc/profile
```



### 2.5. 配置flume脚本

创建一个目录，并建立相应的脚本

```建立一个目录
$ mkdir examples
$ vi examples/example.conf
```



配置example.conf 文件

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1

a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
a1.sources.r1.channels = c1

a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

a1.sinks.k1.type = logger
a1.sinks.k1.channel = c1
```



### 2.6. 启动flume

启动命令如下：

```shell
$ flume-ng agent --conf conf --conf-file  examples/example.conf --name a1 -Dflume.root.logger=INFO,console
```

* --conf  
  * 配置文件的目录
* --conf-file
  * 脚本的目录
* --name
  * agent的名字，名字不要写错，不然启动不了服务，也不会报错
* -Dproperty=value
  * 设置java的参数名称



启动成功后如下图所示：

```
........略
18/01/27 18:17:25 INFO node.AbstractConfigurationProvider: Channel c1 connected to [r1, k1]
18/01/27 18:17:25 INFO node.Application: Starting new configuration:{ sourceRunners:{r1=EventDrivenSourceRunner: { source:org.apache.flume.source.NetcatSource{name:r1,state:IDLE} }} sinkRunners:{k1=SinkRunner: { policy:org.apache.flume.sink.DefaultSinkProcessor@20470f counterGroup:{ name:null counters:{} } }} channels:{c1=org.apache.flume.channel.MemoryChannel{name: c1}} }
18/01/27 18:17:25 INFO node.Application: Starting Channel c1
18/01/27 18:17:25 INFO node.Application: Waiting for channel: c1 to start. Sleeping for 500 ms
18/01/27 18:17:25 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: CHANNEL, name: c1: Successfully registered new MBean.
18/01/27 18:17:25 INFO instrumentation.MonitoredCounterGroup: Component type: CHANNEL, name: c1 started
18/01/27 18:17:26 INFO node.Application: Starting Sink k1
18/01/27 18:17:26 INFO node.Application: Starting Source r1
18/01/27 18:17:26 INFO source.NetcatSource: Source starting
18/01/27 18:17:26 INFO source.NetcatSource: Created serverSocket:sun.nio.ch.ServerSocketChannelImpl[/127.0.0.1:44444]
```



### 2.7. 模拟发送数据

使用telnet发送数据

```shell
$ telnet localhost 44444
Trying ::1...
telnet: connect to address ::1: Connection refused
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Are you OK ?
OK
```

控制台打印如下：

```
Impl[/127.0.0.1:44444]
18/01/27 18:21:00 INFO sink.LoggerSink: Event: { headers:{} body: 41 72 65 20 79 6F 75 20 4F 4B 20 3F 0D          Are you OK ?. }
```



如无法使用telnet，请先安装telnet工具

```shell
$ yum -y install telnet
```



## 3. source组件

常用的组件如下，**更多的组件看官方文档**



### 3.1. Source组件清单 

- Source：对接各种外部数据源，将收集到的事件发送到Channel中，一个source可以向多个channel发送event，Flume内置非常丰富的Source，同时用户可以自定义Source

|        Source类型         |                    Type                     |                   用途                    |
| :-----------------------: | :-----------------------------------------: | :---------------------------------------: |
|        Avro Source        |                    avro                     | 启动一个Avro Server，可与上一级Agent连接  |
|        HTTP Source        |                    http                     |            启动一个HttpServer             |
|        Exec Source        |                    exec                     | 执行unix command，获取标准输出，如tail -f |
|      Taildir Source       |                   TAILDIR                   |              监听目录或文件               |
| Spooling Directory Source |                  spooldir                   |           监听目录下的新增文件            |
|       Kafka Source        | org.apache.flume.sourc  e.kafka.KafkaSource |               读取Kafka数据               |
|        JMS Source         |                     jms                     |              从JMS源读取数据              |

### 3.2. 将日志文件及avro做为数据源

avro Source Agent 和Exec Source Agent

* Avro Source
  * 启动一个Avro Server，可与上一级Agent连接
* Exec Source
  * 执行unix command，获取标准输出，如tail -f



#### 3.2.1 配置avroagent

> 配置一个avroagent，avrosource.conf 配置文件如下：

```
//avrosource.conf
avroagent.sources = r1
avroagent.channels = c1
avroagent.sinks = k1 
avroagent.sources.r1.type = avro
avroagent.sources.r1.bind = 192.168.43.20
avroagent.sources.r1.port = 8888
avroagent.sources.r1.threads= 3
avroagent.sources.r1.channels = c1
avroagent.channels.c1.type = memory
avroagent.channels.c1.capacity = 10000 
avroagent.channels.c1.transactionCapacity = 1000
avroagent.sinks.k1.type = logger
avroagent.sinks.k1.channel = c1
```

> 启动一个avrosource的agent

```shell
$ flume-ng agent --conf conf --conf-file avrosource.conf  --name avroagent -Dflume.root.logger=INFO,console
```

> 启动成功入下图所示：

```
...略
18/01/27 18:46:36 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: CHANNEL, name: c1: Successfully registered new MBean.
18/01/27 18:46:36 INFO instrumentation.MonitoredCounterGroup: Component type: CHANNEL, name: c1 started
18/01/27 18:46:36 INFO node.Application: Starting Sink k1
18/01/27 18:46:36 INFO node.Application: Starting Source r1
18/01/27 18:46:36 INFO source.AvroSource: Starting Avro source r1: { bindAddress: 192.168.43.20, port: 8888 }...
18/01/27 18:46:37 INFO instrumentation.MonitoredCounterGroup: Monitored counter group for type: SOURCE, name: r1: Successfully registered new MBean.
18/01/27 18:46:37 INFO instrumentation.MonitoredCounterGroup: Component type: SOURCE, name: r1 started
18/01/27 18:46:37 INFO source.AvroSource: Avro source r1 started
```



#### 3.2.2. 配置execAgent



>  配置一个execAgent，实现与sourceAgent实现串联，execsource.conf 配置文件如下：

```
execagent.sources = r1 
execagent.channels = c1
execagent.sinks = k1
execagent.sources.r1.type = exec 
execagent.sources.r1.command = tail -F /home/hadoop/apps/flume/execsource/exectest.log
execagent.sources.r1.channels = c1
execagent.channels.c1.type = memory
execagent.channels.c1.capacity = 10000 
execagent.channels.c1.transactionCapacity = 1000
execagent.sinks.k1.type = avro
execagent.sinks.k1.channel = c1
execagent.sinks.k1.hostname = 192.168.43.20
execagent.sinks.k1.port = 8888
```



> 启动 execAgent

启动一个execAgent,并实现execagent监控文件变化，sourceAgent接收变化内容

```shell
$ flume-ng agent --conf conf --conf-file execsource.conf --name execagent
```



> 启动成功如下下图所示：

```
18/01/27 18:58:43 INFO instrumentation.MonitoredCounterGroup: Component type: SINK, name: k1 started
18/01/27 18:58:43 INFO sink.AbstractRpcSink: Rpc sink k1: Building RpcClient with hostname: 192.168.43.20, port: 8888
18/01/27 18:58:43 INFO sink.AvroSink: Attempting to create Avro Rpc client.
18/01/27 18:58:43 WARN api.NettyAvroRpcClient: Using default maxIOWorkers
18/01/27 18:58:44 INFO sink.AbstractRpcSink: Rpc sink k1 started.
```



> 在execAgent监控的文件下写入内容，观察sourceagent是否接收到变化内容

```shell
$ echo 222 > exectest.log 
$ echo 5555 >> exectest.log 
$ cat exectest.log 
222
5555
```



> 在sourceagent控制打印台下查看监控消息如下：

```
18/01/27 18:58:50 INFO sink.LoggerSink: Event: { headers:{} body: 31 32 33                                        123 }
18/01/27 18:59:55 INFO sink.LoggerSink: Event: { headers:{} body: 35 35 35 35                                     5555 }
```

则说明2个串联agent传递信息成功。

 

**说明:**
 avroagent 配置文件配置项起始名称需要与服务启动 -name 名称相一致。



### 3.3. 将目录新增文件做为数据源

Source组件- Spooling Directory Source

* Spooling Directory Source

  * 监听目录下的新增文件

  

> 配置一个Spooling Directory Source ,spooldirsource.conf 配置文件内容如下：

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1
a1.sources.r1.type = spooldir
a1.sources.r1.channels = c1
a1.sources.r1.spoolDir = /home/hadoop/apps/flume/spoolDir
a1.sources.r1.fileHeader = true
a1.channels.c1.type = memory
a1.channels.c1.capacity = 10000
a1.channels.c1.transactionCapacity = 1000
a1.sinks.k1.type = logger
a1.sinks.k1.channel = c1
```

/home/hadoop/apps/flume/spoolDir 必须已经创建且具有用户读写权限。



>  启动 SpoolDirsourceAgent

```shell
$ flume-ng agent --conf conf --conf-file spooldirsource.conf  --name a1 -Dflume.root.logger=INFO,console
```



> 在spoolDir文件夹下创建文件并写入文件内容，观察控制台消息：

```
18/01/28 17:06:54 INFO avro.ReliableSpoolingFileEventReader: Preparing to move file /home/hadoop/apps/flume/spoolDir/test to /home/hadoop/apps/flume/spoolDir/test.COMPLETED
18/01/28 17:06:55 INFO sink.LoggerSink: Event: { headers:{file=/home/hadoop/apps/flume/spoolDir/test} body: 32 32 32                                        222 }
```

此时监测到SpoolDirSourceAgent 可以监控到文件变化。

 值得说明的是：**Spooling Directory Source Agent 并不能监听子级文件夹的文件变化,也不支持已存在的文件更新数据变化**.



### 3.4. 将kafka做为数据源

Source组件- Kafka Sourc





>  配置一个Kafa Source , kafasource.conf 配置文件内容如下：

```
kafkasourceagent.sources = r1
kafkasourceagent.channels = c1
kafkasourceagent.sinks = k1
kafkasourceagent.sources.r1.type = org.apache.flume.source.kafka.KafkaSource 
kafkasourceagent.sources.r1.channels = c1 
kafkasourceagent.sources.r1.batchSize = 100
kafkasourceagent.sources.r1.batchDurationMillis = 1000
kafkasourceagent.sources.r1.kafka.bootstrap.servers = 192.168.43.22:9092,192.168.43.23:9092,192.168.43.24:9092
kafkasourceagent.sources.r1.kafka.topics = flumetopictest1
kafkasourceagent.sources.r1.kafka.consumer.group.id = flumekafkagroupid
kafkasourceagent.channels.c1.type = memory
kafkasourceagent.channels.c1.capacity = 10000 
kafkasourceagent.channels.c1.transactionCapacity = 1000
kafkasourceagent.sinks.k1.type = logger
kafkasourceagent.sinks.k1.channel = c1
```



> 首先启动3个节点的kafka节点服务，在每个kafka节点执行，以后台方式运行

```shell
$ ./kafka-server-start.sh -daemon ../config/server.properties
```

在kafka节点上创建一个配置好的Topic flumetoptest1,命令如下：

```shell
$ ./kafka-topics.sh --create --zookeeper 192.168.43.20:2181 --replication-factor 1 --partitions 3 --topic flumetopictest1
Created topic "flumetopictest1".
```



> 创建成功后，启动一个kafka Source Agent，命令如下：

```
$ flume-ng  agent --conf conf --conf-file kafkasource.conf --name kafkasourceagent -Dflume.root.logger=INFO,console
```



> 创建一个Kafka 生产者，进行消息发送

```shell
$ ./kafka-console-producer.sh --broker-list 192.168.43.22:9092,192.168.43.23:9092 --topic flumetopictest1
```

发送消息，此时kafka 就可以接收到消息：

```
18/02/03 20:36:57 INFO sink.LoggerSink: Event: { headers:{topic=flumetopictest1, partition=2, timestamp=1517661413068} body: 31 32 33 31 33 32 32 31                         12313221 }
18/02/03 20:37:09 INFO sink.LoggerSink: Event: { headers:{topic=flumetopictest1, partition=1, timestamp=1517661428930} body: 77 69 20 61 69 79 6F 75 08 08 08                wi aiyou... }
```

#### 

### 3.5. 文件夹或者文件做为数据源

监听一个文件夹或者文件，通过正则表达式匹配需要监听的 数据源文件，Taildir Source通过将监听的文件位置写入到文件中来实现断点续传，并且能够保证没有重复数据的读取.

- 重要参数

  * type：source类型TAILDIR
  * positionFile：保存监听文件读取位置的文件路径

  * idleTimeout：关闭空闲文件延迟时间，如果有新的记录添加到已关闭的空闲文件

  *  taildir srouce将继续打开该空闲文件，默认值120000毫秒

  *  writePosInterval：向保存读取位置文件中写入读取文件位置的时间间隔，默认值
     3000毫秒

  *  batchSize：批量写入channel最大event数，默认值100

  * maxBackoffSleep：每次最后一次尝试没有获取到监听文件最新数据的最大延迟时 间，默认值5000毫秒

  * cachePatternMatching：对于监听的文件夹下通过正则表达式匹配的文件可能数量 会很多，将匹配成功的监听文件列表和读取文件列表的顺序都添加到缓存中，可以提高性能，默认值true

  * fileHeader ：是否添加文件的绝对路径到event的header中，默认值false

  * fileHeaderKey：添加到event header中文件绝对路径的键值，默认值file

  * filegroups：监听的文件组列表，taildirsource通过文件组监听多个目录或文件

  * filegroups.<filegroupName>：文件正则表达式路径或者监听指定文件路径

  * channels：Source对接的Channel名称

  

 > 配置一个taildir Source,具体taildirsource.conf 配置文件内容如下：

```
taildiragent.sources=r1
taildiragent.channels=c1
taildiragent.sinks=k1
taildiragent.sources.r1.type=TAILDIR
taildiragent.sources.r1.positionFile=/home/hadoop/apps/flume/taildir/position/taildir_position.json
taildiragent.sources.r1.filegroups=f1 f2
taildiragent.sources.r1.filegroups.f1=/home/hadoop/apps/flume/taildir/test1/test.log
taildiragent.sources.r1.filegroups.f2=/home/hadoop/apps/flume/taildir/test2/.*log.*
taildiragent.sources.r1.channels=c1
taildiragent.channels.c1.type=memory
taildiragent.channels.c1.transcationCapacity=1000
taildiragent.sinks.k1.type=logger
taildiragent.sinks.k1.channel=c1
```



>  启动一个taildirSource agent ,代码如下：

```shell
$ flume-ng agent --conf conf --conf-file taildirsource.conf --name taildiragent -Dflume.root.logger=INFO,console
```

开始在test1和test2文件夹写入文件，观察agent消息接收。



### 3.6. Http数据源

htttp source：采集http中的日志



## 4. Channel组件

Channel：Channel被设计为event中转暂存区，存储Source 收集并且没有被Sink消费的event ，为了平衡Source收集 和Sink读取数据的速度，可视为Flume内部的消息队列。

Channel是线程安全的并且具有事务性，支持source写失 败重复写和sink读失败重复读等操作

常用的Channel类型有：Memory Channel、File Channel、
 Kafka Channel、JDBC Channel等



### 4.1. 基于内存

- Memory Channel：使用内存作为Channel，Memory Channel读写速度 快，但是存储数据量小，Flume进程挂掉、服务器停机或者重启都会 导致数据丢失。部署Flume Agent的线上服务器内存资源充足、不关 心数据丢失的场景下可以使用
   关键参数：

```
type ：channel类型memory
capacity ：channel中存储的最大event数，默认值100
transactionCapacity ：一次事务中写入和读取的event最大数，默认值100。
keep-alive：在Channel中写入或读取event等待完成的超时时间，默认值3秒
byteCapacityBufferPercentage：缓冲空间占Channel容量（byteCapacity）的百分比，为event中的头信息保留了空间，默认值20（单位百分比）
byteCapacity ：Channel占用内存的最大容量，默认值为Flume堆内存的80%
```



### 4.2. 基于文件

- File Channel：将event写入到磁盘文件中，与Memory Channel相比存 储容量大，无数据丢失风险。
- File Channle数据存储路径可以配置多磁盘文件路径，提高写入文件性能
- Flume将Event顺序写入到File Channel文件的末尾，在配置文件中通
   过设置maxFileSize参数设置数据文件大小上限
- 当一个已关闭的只读数据文件中的Event被完全读取完成，并且Sink已经提交读取完成的事务，则Flume将删除存储该数据文件
- 通过设置检查点和备份检查点在Agent重启之后能够快速将File Channle中的数据按顺序回放到内存中
   关键参数如下：

```
 type：channel类型为file 
 
 checkpointDir：检查点目录，默认在启动flume用户目录下创建，建议单独配置磁盘路径 
 
 useDualCheckpoints：是否开启备份检查点，默认false，建议设置为true开启备份检查点，备份检查点的作用是当Agent意外出错导致写 入检查点文件异常，在重新启动File  Channel时通过备份检查点将数据回放到内存中，如果不开启备份检查点，在数据回放的过程中发现检查点文件异常会对所数据进行全回放，全回放的过程相当耗时 
 
 backupCheckpointDir：备份检查点目录，最好不要和检查点目录在同 一块磁盘上 
 
 checkpointInterval：每次写检查点的时间间隔，默认值30000毫秒
 
 dataDirs：数据文件磁盘存储路径，建议配置多块盘的多个路径，通过磁盘的并行写入来提高file channel性能，多个磁盘路径用逗号隔开
 
 transactionCapacity：一次事务中写入和读取的event最大数，默认值 10000
 
 maxFileSize：每个数据文件的最大大小，默认值：2146435071字节
 
 minimumRequiredSpace：磁盘路径最小剩余空间，如果磁盘剩余空 间小于设置值，则不再写入数据
 
 capacity：file channel可容纳的最大event数
 
 keep-alive：在Channel中写入或读取event等待完成的超时时间，默认值3秒
 
```



#### 4.2.1. 配置FileChannel

配置一个FileChannel,filechannel.conf 的配置内容如下：

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
a1.sources.r1.channels = c1
a1.channels.c1.type = file
a1.channels.c1.dataDirs = /home/hadoop/apps/flume/filechannel/data
a1.channels.c1.checkpointDir = /home/hadoop/apps/flume/filechannel/checkpoint 
a1.channels.c1.useDualCheckpoints = true
a1.channels.c1.backupCheckpointDir = /home/hadoop/apps/flume/filechannel/backup
a1.sinks.k1.type = logger
a1.sinks.k1.channel = c1
```



#### 4.2.2. 启动FileChannel

启动一个FileChannel,启动命令如下：

```shell
$ flume-ng agent --conf conf --conf-file filechannle.conf --name a1 -Dflume.root.logger=INFO,console
```



#### 4.2.3. 模拟发送数据

向配置文件端口44444发送数据，观察Channel记录情况

```
telnet localhost asdfasd
```



#### 4.2.4. 查看数据是否正确

此时可以观察到控制台打印监控结果

```
18/02/04 21:15:44 INFO sink.LoggerSink: Event: { headers:{} body: 61 64 66 61 64 66 61 64 66 61 73 66 0D          adfadfadfasf. }
18/02/04 21:15:48 INFO file.EventQueueBackingStoreFile: Start checkpoint for /home/hadoop/apps/flume/filechannel/checkpoint/checkpoint, elements to sync = 1
18/02/04 21:15:48 INFO file.EventQueueBackingStoreFile: Updating checkpoint metadata: logWriteOrderID: 1517749968978, queueSize: 0, queueHead: 0
18/02/04 21:15:48 INFO file.EventQueueBackingStoreFile: Attempting to back up checkpoint.
18/02/04 21:15:48 INFO file.Serialization: Skipping in_use.lock because it is in excludes set
18/02/04 21:15:48 INFO file.Serialization: Deleted the following files: , checkpoint, checkpoint.meta, inflightputs, inflighttakes.
18/02/04 21:15:48 INFO file.Log: Updated checkpoint for file: /home/hadoop/apps/flume/filechannel/data/log-2 position: 170 logWriteOrderID: 1517749968978
18/02/04 21:15:49 INFO file.EventQueueBackingStoreFile: Checkpoint backup completed.
```



### 4.3. 基于Kafka

Kafka Channel：将分布式消息队列kafka作为channel相对于Memory Channel和File Channel存储容量更大、 容错能力更强，弥补了其他两种Channel的短板，如果合理利用Kafka的性能，能够达到事半功倍的效果。
 关键参数如下：

```
type：Kafka Channel类型org.apache.flume.channel.kafka.KafkaChannel

kafka.bootstrap.servers：Kafka broker列表，格式为ip1:port1, ip2:port2…，建 议配置多个值提高容错能力，多个值之间用逗号隔开

kafka.topic：topic名称，默认值“flume-channel”

kafka.consumer.group.id：Consumer Group Id，全局唯一

parseAsFlumeEvent：是否以Avro FlumeEvent模式写入到Kafka Channel中，  默认值true，event的header信息与event body都写入到kafka中

pollTimeout：轮询超时时间，默认值500毫秒

kafka.consumer.auto.offset.reset：earliest表示从最早的偏移量开始拉取，latest表示从最新的偏移量开始拉取，none表示如果没有发现该Consumer组之前拉 取的偏移量则抛异常
```



#### 4.3.1. 配置KafakChannel

配置一个KafakChannel， kafkachannel.conf 配置内容如下：

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
a1.sources.r1.channels = c1
a1.channels.c1.type = org.apache.flume.channel.kafka.KafkaChannel
a1.channels.c1.kafka.bootstrap.servers = 192.168.43.22:9092,192.168.43.23:9092
a1.channels.c1.kafka.topic = flumechannel2
a1.channels.c1.kafka.consumer.group.id = flumecgtest1
a1.sinks.k1.type = logger
a1.sinks.k1.channel = c1
```



#### 4.3.2. 启动Kafak

启动kafak服务，创建一个kafka主题，命令如下：

```shell
$ ./kafka-server-start.sh -daemon ../config/server.properties
$ ./kafka-topics.sh --create --zookeeper 192.168.43.20:2181 --replication-factor 1 --partitions 3 --topic flumechannel2
```

查看创建的主题信息

```shell
$  ./kafka-topics.sh --list --zookeeper 192.168.43.20:2181
__consumer_offsets
flumechannel2
topicnewtest1
```



#### 4.3.3. 启动kafka agent

启动kafka agent,

```shell
$ flume-ng agent --conf conf --conf-file kafkachannel.conf --name a1 -Dflume.root.logger=INFO,console

```



#### 4.3.4. 模拟数据发送

使用telnet发送数据

```shell
$  clear
$  telnet localhost 44444 
Trying ::1...
telnet: connect to address ::1: Connection refused
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
abc
OK
```



#### 4.3.5. 查看数据是否正确

监听信息如下：

```
18/02/04 21:39:33 INFO sink.LoggerSink: Event: { headers:{} body: 61 62 63 0D                                     abc. }
```



## 5. Sink组件

- Sink：从Channel消费event，输出到外部存储，或者输出到下一个阶段的agent
- 一个Sink只能从一个Channel中消费event
- 当Sink写出event成功后，就会向Channel提交事务。Sink  事务提交成功，处理完成的event将会被Channel删除。否 则Channel会等待Sink重新消费处理失败的event
- Flume提供了丰富的Sink组件，如Avro Sink、HDFS Sink、Kafka Sink、File Roll Sink、HTTP Sink等



### 5.1. 写入flumen代理

Sink组件- Avro Sink

- Avro Sink常用于对接下一层的Avro Source，通过发送RPC请求将Event发送到下一层的Avro Source
- 为了减少Event传输占用大量的网络资源， Avro Sink提供了端到端的批量压缩数据传输

关键参数说明

```
type：Sink类型为avro。

hostname：绑定的目标Avro Souce主机名称或者IP

port：绑定的目标Avro Souce端口号

batch-size：批量发送Event数，默认值100
compression-type：是否使用压缩，如果使用压缩设则值为
“deflate”， Avro Sink设置了压缩那么Avro Source也应设置相同的 压缩格式，目前支持zlib压缩，默认值none
compression-level：压缩级别，0表示不压缩，从1到9数字越大压缩
效果越好，默认值6
```



### 5.2 写入HDFS

Sink组件- HDFS Sink

- HDFS Sink将Event写入到HDFS中持久化存储
- HDFS Sink提供了强大的时间戳转义功能，根据Event头信息中的
- timestamp时间戳信息转义成日期格式，在HDFS中以日期目录分层存储

关键参数信息说明如下：

```
type：Sink类型为hdfs。
hdfs.path：HDFS存储路径，支持按日期时间分区。
hdfs.filePrefix：Event输出到HDFS的文件名前缀，默认前缀FlumeData
hdfs.fileSuffix：Event输出到HDFS的文件名后缀
hdfs.inUsePrefix：临时文件名前缀
hdfs.inUseSuffix：临时文件名后缀，默认值.tmp
hdfs.rollInterval：HDFS文件滚动生成时间间隔，默认值30秒，该值设置 为0表示文件不根据时间滚动生成
```

[flume中HdfsSink参数说明](https://www.cnblogs.com/30go/p/9956210.html)



#### 5.2.1. 配置hdfsSink

配置一个hdfsink.conf文件，配置内容如下：

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = timestamp
a1.sources.r1.interceptors.i1.preserveExisting = false
a1.sources.r1.channels = c1
a1.channels.c1.type = memory
a1.channels.c1.capacity = 10000 
a1.channels.c1.transactionCapacity = 1000
a1.sinks.k1.type = hdfs
a1.sinks.k1.channel = c1
a1.sinks.k1.hdfs.path = /data/flume/%Y%m%d
a1.sinks.k1.hdfs.filePrefix = hdfssink
a1.sinks.k1.hdfs.fileType = DataStream
a1.sinks.k1.hdfs.writeFormat = Text
a1.sinks.k1.hdfs.round = true
a1.sinks.k1.hdfs.roundValue = 1
a1.sinks.k1.hdfs.roundUnit = minute
a1.sinks.k1.hdfs.callTimeout = 60000
```



#### 5.2.2. 启动hdfssink 

启动一个hdfssink agent，命令如下：

```shell
$ flume-ng agent --conf conf --conf-file hdfssink.conf --name a1 -Dflume.root.logger=INFO,console
```



#### 5.2.3. 模拟发送数据

使用telnet 向44444发送数据，观察数据写入结果

```shell
$ telnet localhost 44444
Trying ::1...
telnet: connect to address ::1: Connection refused
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
abc
OK
2323444
OK
```



#### 5.2.4. 查看数据接收情况

此时控制台打印，在HDFS文件系统生成一个临时文件

```
8/02/04 22:41:52 INFO hdfs.HDFSDataStream: Serializer = TEXT, UseRawLocalFileSystem = false
18/02/04 22:41:52 INFO hdfs.BucketWriter: Creating /data/flume/20180204/hdfssink.1517755312242.tmp
18/02/04 22:42:24 INFO hdfs.BucketWriter: Closing /data/flume/20180204/hdfssink.1517755312242.tmp
18/02/04 22:42:24 INFO hdfs.BucketWriter: Renaming /data/flume/20180204/hdfssink.1517755312242.tmp to /data/flume/20180204/hdfssink.1517755312242
18/02/04 22:42:24 INFO hdfs.HDFSEventSink: Writer callback called.
```

值得注意的是：**请使用hadoop用户来执行agent的创建和消息的发送，避免因权限导致HDFS文件无法写入**



### 5.3. 写入Kafda

Sink组件- Kafka Sink

Flume通过KafkaSink将Event写入到Kafka指定的主题中
 主要参数说明如下：

```
 type：Sink类型，值为KafkaSink类路径  org.apache.flume.sink.kafka.KafkaSink。
 
 kafka.bootstrap.servers：Broker列表，定义格式host:port，多个Broker之间用逗号隔开，可以配置一个也可以配置多个，用于Producer发现集群中的Broker，建议配置多个，防止当个Broker出现问题连接 失败。
 
 kafka.topic：Kafka中Topic主题名称，默认值flume-topic。
 
 flumeBatchSize：Producer端单次批量发送的消息条数，该值应该根据实际环境适当调整，增大批量发送消息的条数能够在一定程度上提高性能，但是同时也增加了延迟和Producer端数据丢失的风险。 默认值100。
 
 kafka.producer.acks：设置Producer端发送消息到Borker是否等待接收Broker返回成功送达信号。0表示Producer发送消息到Broker之后不需要等待Broker返回成功送达的信号，这种方式吞吐量高，但是存 在数据丢失的风险。1表示Broker接收到消息成功写入本地log文件后向Producer返回成功接收的信号，不需要等待所有的Follower全部同步完消息后再做回应，这种方式在数据丢失风险和吞吐量之间做了平衡。all（或者-1）表示Broker接收到
 
 Producer的消息成功写入本 地log并且等待所有的Follower成功写入本地log后向Producer返回成功接收的信号，这种方式能够保证消息不丢失，但是性能最差。默 认值1。
 
 useFlumeEventFormat：默认值false，Kafka Sink只会将Event body内 容发送到Kafka Topic中。如果设置为true，Producer发送到KafkaTopic中的Event将能够保留Producer端头信息
```



#### 5.3.1. 配置kafkaSink

配置一个kafkasink.conf,具体配置内容如下：

```
a1.sources = r1
a1.channels = c1
a1.sinks = k1
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
a1.sources.r1.channels = c1
a1.channels.c1.type = memory
a1.channels.c1.capacity = 10000 
a1.channels.c1.transactionCapacity = 1000
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.channel = c1
a1.sinks.k1.kafka.topic = FlumeKafkaSinkTopic1
a1.sinks.k1.kafka.bootstrap.servers = 192.168.43.22:9092,192.168.43.23:9092
a1.sinks.k1.kafka.flumeBatchSize = 100
a1.sinks.k1.kafka.producer.acks = 1
```



#### 5.3.2. 启动kafka

例如：启动kafka Broker节点22和Broker节点23

```shell
$ ./kafka-server-start.sh -daemon ../config/server.properties 
```

按配置文件创建主题信息

```shell
$ ./kafka-topics.sh --create --zookeeper 192.168.43.20:2181 --replication-factor 1 --partitions 3 --topic FlumeKafkaSinkTopic1
Created topic "FlumeKafkaSinkTopic1".
```



#### 5.3.3. 启动kafkasink agent

启动一个kafkasink agent，启动命令如下：

```shell
$  flume-ng agent --conf conf --conf-file kafkasink.conf --name a1 >/dev/null 2>&1 &
```



#### 5.3.4. 查看是否写入kafka



### 5.4. 写入HBase



这里使用了`serializer`，这个可以是自己编写的java代码



```
a1.channels = c1
a1.sinks = k1
a1.sinks.k1.type = hbase2
a1.sinks.k1.table = foo_table
a1.sinks.k1.columnFamily = bar_cf
a1.sinks.k1.serializer = org.apache.flume.sink.hbase2.RegexHBase2EventSerializer
a1.sinks.k1.channel = c1
```

[flume自定义Serializer收集日志入elasticsearch](https://blog.csdn.net/yujimoyouran/article/details/59104131)







## 6. Interceptor拦截器

- Source将event写入到Channel之前调用拦截器
- Source和Channel之间可以有多个拦截器，不同的拦截器使用不同的 规则处理Event
- 可选、轻量级、可插拔的插件
- 通过实现Interceptor接口实现自定义的拦截器
- 内置拦截器：Timestamp Interceptor、Host Interceptor、UUID Interceptor、Static Interceptor、Regex Filtering Interceptor等





### 6.1. 时间拦截器

Timestamp Interceptor

- Flume使用时间戳拦截器在event头信息中添加时间戳信息， Key为timestamp，Value为拦截器拦截Event时的时间戳

- 头信息时间戳的作用，比如HDFS存储的数据采用时间分区存储，Sink可以根据Event头信息中的时间戳将Event按照时间分区写入到 HDFS

- 关键参数说明： 

  - type:拦截器类型为timestamp
  - preserveExisting：如果头信息中存在timestamp时间戳信息是否保留原来的时间戳信息，true保留，false使用新的时间戳替换已经存在的时间戳，默认值为false

  

### 6.2. 主机拦截器

Host Interceptor

- Flume使用主机戳拦截器在Event头信息中添加主机名称或者IP
- 主机拦截器的作用：比如Source将Event按照主机名称写入到不同的Channel中便于后续的Sink对不同Channnel中的数据分开处理
- 关键参数说明： 
  - type:拦截器类型为host
  - preserveExisting：如果头信息中存在timestamp时间戳信息是否保留原来的时间戳信息，true保留，false使用新的时间戳替换已经存在的时间戳，默认值为false
  - useIP：是否使用IP作为主机信息写入都信息，默认值为false
  - hostHeader：设置头信息中主机信息的Key，默认值为host
    

### 6.3. 静态信息拦截器

Host InterceptorStatic Interceptor

- Flume使用static  interceptor静态拦截器在evetn头信息添加静态信息
- 关键参数说明：
- type:拦截器类型为static 
  - preserveExisting：如果头信息中存在timestamp时间戳信息是否保留原来的时间戳信息，true保留，false使用新的时间戳替换已经 存在的时间戳，默认值为false
  - key：头信息中的键
  - value：头信息中键对应的值



### 6.4. 正则过滤拦截器

该拦截器可以过滤掉不需要的日志，也可以根据需要收集满足正则条件的日志。

Source连接到正则过滤拦截器的配置：

a1.sources.r1.interceptors=regex

a1.sources.r1.interceptors.regex.type=REGEX_FILTER a1.sources.r1.interceptors.regex.regex=.* a1.sources.r1.interceptors.regex.excludeEvents=false

其中regex=.*匹配除“\n”之外的任何字符，excludeEvents=false默认收集匹配到的事件；若为true，则会删除匹配到的event，收集未匹配到的。







## 7. Selector选择器

- Source将event写入到Channel之前调用拦截器，如果配置了Interceptor拦截器，则Selector在拦截器全部处理完之后调用。通过
   selector决定event写入Channel的方式
- 内置Replicating Channel Selector复制Channel选择器、 Multiplexing  Channel Selector复用Channel选择器



![alt](https://upload-images.jianshu.io/upload_images/1784853-9b701d7d5da1facc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/902/format/webp)



### 7.1. 复用型选择器

- 如果Channel选择器没有指定，默认是Replicating Channel Selector。即一个Source以复制的方式将一个event同时写入到多个Channel中，不同的Sink可以从不同的Channel中获取相同的event。
- 关键参数说明： 
  - selector.type：Channel选择器类型为replicating
  - selector.optional：定义可选Channel，当写入event到可选Channel失败时，不会向Source抛出异常，继续执行。多个可选Channel之 间用空格隔开



一个source将一个event拷贝到多个channel，通过不同的sink消费不同的channel，将相同的event输出到不同的地方



#### 7.1.1. 配置选择器

 配置文件：replicating_selector.conf

```
a1.sources = r1
a1.channels = c1 c2
a1.sinks = k1 k2
#定义source
a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444
#设置复制选择器
a1.sources.r1.selector.type = replicating
#设置required channel
a1.sources.r1.channels = c1 c2
#设置channel c1
a1.channels.c1.type = memory 
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 1000
#设置channel c2
a1.channels.c2.type = memory 
a1.channels.c2.capacity = 1000
a1.channels.c2.transactionCapacity = 1000
#设置kafka sink
a1.sinks.k1.channel = c1
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.kafka.topic = FlumeSelectorTopic1
a1.sinks.k1.kafka.bootstrap.servers = 192.168.43.22:9092,192.168.23.103:9092
a1.sinks.k1.kafka.flumeBatchSize = 5
a1.sinks.k1.kafka.producer.acks = 1
#设置file sink
a1.sinks.k2.channel = c2
a1.sinks.k2.type = file_roll
a1.sinks.k2.sink.directory = /home/hadoop/apps/flume/selector
a1.sinks.k2.sink.rollInterval = 60
```

分别写入到kafka和文件中



#### 7.1.2. 创建kafka主题

创建主题FlumeKafkaSinkTopic1

```shell
$ bin/kafka-topics.sh --create --zookeeper 192.168.183.100:2181 --replication-factor 1 --partitions 3 --topic FlumeSelectorTopic1
```



#### 7.1.3. 启动flume agent

```shell
$ bin/flume-ng agent --conf conf --conf-file conf/replicating_selector.conf --name a1
```



#### 7.1.4. 模拟数据

使用telnet发送数据

```shell
telnet localhost 44444
```



#### 7.1.5. 查看接收数据结果

查看/home/hadoop/apps/flume/selector路径下的数据

查看kafka FlumeSelectorTopic1主题数据

```shell
bin/kafka-console-consumer.sh --zookeeper 192.168.183.100:2181 --from-beginning --topic FlumeSelectorTopic1
```





### 7.2. 多路复用选择器

根据头文件，写入到不同的channle中

-Multiplexing Channel Selector多路复用选择器根据event的头信息中不
 同键值数据来判断Event应该被写入到哪个Channel中

- 三种级别的Channel，分别是必选channle、可选channel、默认channel
- 关键参数说明：

```
selector.type：Channel选择器类型为multiplexing
selector.header：设置头信息中用于检测的headerName
selector.default：默认写入的Channel列表
selector.mapping.*：headerName对应的不同值映射的不同Channel列表
selector.optional：可选写入的Channel列表
```



#### 7.2.1. 配置多路选择器

配置文件multiplexing_selector.conf、avro_sink1.conf、avro_sink2.conf、avro_sink3.conf
 向不同的avro_sink对应的配置文件的agent发送数据，不同的avro_sink配置文件通过static interceptor在event头信息中写入不同的静态数据
 multiplexing_selector根据event头信息中不同的静态数据类型分别发送到不同的目的地 

>  multiplexing_selector.conf

```
a3.sources = r1
a3.channels = c1 c2 c3
a3.sinks = k1 k2 k3
a3.sources.r1.type = avro
a3.sources.r1.bind = 192.168.183.100
a3.sources.r1.port = 8888
a3.sources.r1.threads= 3
#设置multiplexing selector
a3.sources.r1.selector.type = multiplexing
a3.sources.r1.selector.header = logtype
#通过header中logtype键对应的值来选择不同的sink
a3.sources.r1.selector.mapping.ad = c1
a3.sources.r1.selector.mapping.search = c2
a3.sources.r1.selector.default = c3
a3.sources.r1.channels = c1 c2 c3
a3.channels.c1.type = memory
a3.channels.c1.capacity = 10000
a3.channels.c1.transactionCapacity = 1000
a3.channels.c2.type = memory
a3.channels.c2.capacity = 10000
a3.channels.c2.transactionCapacity = 1000
a3.channels.c3.type = memory
a3.channels.c3.capacity = 10000
a3.channels.c3.transactionCapacity = 1000
#分别设置三个sink的不同输出
a3.sinks.k1.type = file_roll
a3.sinks.k1.channel = c1
a3.sinks.k1.sink.directory = /home/hadoop/apps/flume/multiplexing/k11
a3.sinks.k1.sink.rollInterval = 60
a3.sinks.k2.channel = c2
a3.sinks.k2.type = file_roll
a3.sinks.k2.sink.directory = /home/hadoop/apps/flume/multiplexing/k12
a3.sinks.k2.sink.rollInterval = 60
a3.sinks.k3.channel = c3
a3.sinks.k3.type = file_roll
a3.sinks.k3.sink.directory = /home/hadoop/apps/flume/multiplexing/k13
a3.sinks.k3.sink.rollInterval = 60
```

avro_sink1.conf

```
agent1.sources = r1
agent1.channels = c1
agent1.sinks = k1
agent1.sources.r1.type = netcat
agent1.sources.r1.bind = localhost
agent1.sources.r1.port = 44444
agent1.sources.r1.interceptors = i1
agent1.sources.r1.interceptors.i1.type = static
agent1.sources.r1.interceptors.i1.key = logtype
agent1.sources.r1.interceptors.i1.value = ad
agent1.sources.r1.interceptors.i1.preserveExisting = false
agent1.sources.r1.channels = c1
agent1.channels.c1.type = memory
agent1.channels.c1.capacity = 10000 
agent1.channels.c1.transactionCapacity = 1000
agent1.sinks.k1.type = avro
agent1.sinks.k1.channel = c1
agent1.sinks.k1.hostname = 192.168.183.100
agent1.sinks.k1.port = 8888
```

avro_sink2.conf

```
agent2.sources = r1
agent2.channels = c1
agent2.sinks = k1
agent2.sources.r1.type = netcat
agent2.sources.r1.bind = localhost
agent2.sources.r1.port = 44445
agent2.sources.r1.interceptors = i1
agent2.sources.r1.interceptors.i1.type = static
agent2.sources.r1.interceptors.i1.key = logtype
agent2.sources.r1.interceptors.i1.value = search
agent2.sources.r1.interceptors.i1.preserveExisting = false
agent2.sources.r1.channels = c1
agent2.channels.c1.type = memory
agent2.channels.c1.capacity = 10000 
agent2.channels.c1.transactionCapacity = 1000
agent2.sinks.k1.type = avro
agent2.sinks.k1.channel = c1
agent2.sinks.k1.hostname = 192.168.183.100
agent2.sinks.k1.port = 8888
```

avro_sink3.conf

```
agent3.sources = r1
agent3.channels = c1
agent3.sinks = k1
agent3.sources.r1.type = netcat
agent3.sources.r1.bind = localhost
agent3.sources.r1.port = 44446
agent3.sources.r1.interceptors = i1
agent3.sources.r1.interceptors.i1.type = static
agent3.sources.r1.interceptors.i1.key = logtype
agent3.sources.r1.interceptors.i1.value = other
agent3.sources.r1.interceptors.i1.preserveExisting = false
agent3.sources.r1.channels = c1
agent3.channels.c1.type = memory
agent3.channels.c1.capacity = 10000 
agent3.channels.c1.transactionCapacity = 1000
agent3.sinks.k1.type = avro
agent3.sinks.k1.channel = c1
agent3.sinks.k1.hostname = 192.168.183.100
agent3.sinks.k1.port = 8888
```



#### 7.2.2. 启动代理

在/home/hadoop/apps/flume/multiplexing目录下分别创建看k1 k2 k3目录

```shell
$ bin/flume-ng agent --conf conf --conf-file conf/multiplexing_selector.conf --name a3 -Dflume.root.logger=INFO,console

$ bin/flume-ng agent --conf conf --conf-file conf/avro_sink1.conf --name agent1 >/dev/null 2>&1 &

$ bin/flume-ng agent --conf conf --conf-file conf/avro_sink2.conf --name agent2 >/dev/null 2>&1 &

$ bin/flume-ng agent --conf conf --conf-file conf/avro_sink3.conf --name agent3 >/dev/null 2>&1 &
```



#### 7.2.3. 模拟发送数据

使用telnet发送数据
 telnet localhost 44444



#### 7.2.4. 查看收到的数据





## 8. Sink 处理器

对写入进行负载均衡

- Sink Processor协调多个sink间进行load balance和fail over
- Default Sink Processor只有一个sink，无需创建Sink Processor
- Sink Group：将多个sink放到一个组内，要求组内一个sink消费channel
- Load-Balancing Sink Processor（负载均衡处理器）round_robin(默认)或 random
- Failover Sink Processor（容错处理器）可定义一个sink优先级列表，根据优先级选择使用的sink





### 8.1. 负载均衡性处理器

Load-Balancing Sink Processor

关键参数说明：

```
sinks：sink组内的子Sink，多个子sink之间用空格隔开
processor.type：设置负载均衡类型load_balance
processor.backoff：设置为true时，如果在系统运行过程中执行的Sink失败，会将失败的Sink放进一个冷却池中。默认值false
processor.selector.maxTimeOut：失败sink在冷却池中最大驻留时间，默认值30000ms
processor.selector：负载均衡选择算法，可以使用轮询“round_robin”、随机“random”或者是继承AbstractSinkSelector类的自定义负载均衡实现类
```

示例：

![img](https:////upload-images.jianshu.io/upload_images/1784853-2e86bae3b2af0531.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/747/format/webp)



### 8.2. 故障转移型处理器

Failover Sink Processor

关键参数说明：

```
sinks：sink组内的子Sink，多个子sink之间用空格隔开
processor.type：设置故障转移类型“failover”
processor.priority.<sinkName>：指定Sink组内各子Sink的优先级别，优先级从高到低，数值越大优先级越高
processor.maxpenalty：等待失败的Sink恢复的最长时间，默认值30000毫秒
```

示例：

![img](https:////upload-images.jianshu.io/upload_images/1784853-01c63fe5f0586cb0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/643/format/webp)



#### 8.2.1. 应用场景

- 分布式日志收集场景
- 多个agent收集不同机器上相同类型的日志数据，为了保障高可用，采用分层部署，日志收集层Collector部署两个甚至多个，Agent通过Failover  SinkProcessor实现其中任何一个collector挂掉不影响系统的日志收集服务

![alt](https://upload-images.jianshu.io/upload_images/1784853-782d0cca4d4aa7a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/658/format/webp)





## 9. 注意事项



### 9.1. Flume的停止

​      使用kill停止Flume进程，不可使用kill -9，因为Flume内部注册了很多钩子函数执行善后工作，如果使用kill -9会导致钩子函数不执行，使用kill时，Flume内部进程会监控到用户的操作，然后调用钩子函数，执行一些善后操作，正常退出。



### 9.2. Flume数据丢失问题

​      Flume可能丢失数据的情况是Channel采用memoryChannel，agent宕机导致数据丢失，或者Channel存储数据已满，导致Source不再写入，未写入的数据丢失。另外，Flume有可能造成数据的重复，例如数据已经成功由Sink发出，但是没有接收到响应，Sink会再次发送数据，此时可能会导致数据的重复。

​    

### 9.3. Sink从Channel中读取数据的方式

   默认情况下，Sink获取数据的方式是：当Source向Channel发送一条数据的时候，Sink会通过循环的方式获取一条数据，然后再发送给客户端。

   Sink可以分为KafkaSink和AvroSink， 它们都是通过循环的方式获取数据，但是 KafkaSink可以通过配置topic进行批量从客户端读取。但原理还是一条一条的从Channel读取数据，只是在Sink中存在缓存机制，当数据量达到某一数量的时候，会将数据批量发送到客户端。



###   9.4 CPU占用过高的问题

  若程序运行出现CPU占用过高的现象，则可以在代码中加入休眠sleep，这样的话，就可以释放CPU资源，注意，内存资源不会释放，因为线程还未结束，是可用状态。





## 10. 优化处理

Flume经常被用在生产环境中收集后端产生的日志，一个Flume进程就是一个Agent，要充分发挥Flume的性能最主要的是要调好Flume的配置参数。

Flume agent配置分为三个部分：Source、Channel、Sink。

**1、Source**

（1）增加Source个数（使用tairDirSource时可增加filegroups个数）可以增大Source读取数据的能力。例如：当某一个目录产生的文件过多时需要将这个文件目录拆分成多个文件目录，同时配置好多个Source以保证Source有足够的能力获取到新产生的数据。

（2）batchSize参数决定Source一次批量传输到Channel的event条数，适当调大这个参数可以提高Source搬运Event到Channel时的性能。

**2、Channel** 

（1）type选择memory时Channel的性能最好，但是如果Flume进程意外挂掉可能会丢失数据；type选择file时Channel的容错性更好，但是性能上会比memory channel差。使用file Channel时dataDirs配置多个不同盘下的目录可以提高性能。

（2）capacity参数决定Channel可容纳最大的event条数；transactionCapacity参数决定每次Source往channel里面写的最大event条数和每次Sink从channel里面读的最大event条数；transactionCapacity需要大于Source和Sink的batchSize参数；byteCapacity是Channel的内存大小，单位是byte。  



![img](http://rdc.hundsun.com/portal/data/upload/201812/f_fbc4f5295d60e55b5ee8f23917e459a1.png)

**3、Sink** 

（1）增加Sink的个数可以增加Sink消费event的能力。当然Sink也不是越多越好，够用就行，过多的Sink会占用系统资源，造成系统资源不必要的浪费。

（2）batchSize参数决定Sink一次批量从Channel读取的event条数，适当调大这个参数可以提高Sink从Channel搬出event的性能。





## 11. 过滤器



官方的正则过滤器用来过滤被正则匹配的日志。

1.excludeEvents属性
当 excludeEvents 属性值为 true 则把正则匹配到的日志 过滤掉，不读取到channel，通过sink 进行输出。

当 excludeEvents 属性值为 false 则把正则没有匹配到的日志 过滤掉，将正则匹配到的日志信息读取到channel，通过sink 进行输出。

excludeEvents 默认值为false 。



> 参考文档

[flume 的官方正则过滤器](https://blog.csdn.net/u012373815/article/details/54346219)



##  12. 监控系统状态

监控系统的执行状态，以便调试错误





## 13. 第三方插件介绍





## 14. 自定义组件开发

通过java程序开发自己的组件。 详细内容可以查看[官方文档](http://flume.apache.org/releases/content/1.9.0/FlumeDeveloperGuide.html#data-flow-model)