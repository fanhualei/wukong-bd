# flume使用

官网地址：http://flume.apache.org/



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
$ ln -s /home/hadoop/apps/apache-flume-1.8.0-bin /usr/local/flume
```



### 2.4. 配置环境变量

编辑 /etc/profile文件，增加以下内容：

```
export FLUME_HOME=/usr/local/flume
export PATH=$PATH:${JAVA_HOME}/bin:${ZOOKEEPER_HOME}/bin:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${HIVE_HOME}/bin:${FLUME_HOME}/bin
```

### 2.5. 启动flume

使用example.conf 配置文件启动一个实例

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

启动命令如下：

```shell
$ pwd
/home/hadoop/apps/apache-flume-1.8.0-bin/conf
$ flume-ng agent --conf conf --conf-file  example.conf --name a1 -Dflume.root.logger=INFO,console
```

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

### 3.5. 一个文件夹或者文件做为数据源

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

