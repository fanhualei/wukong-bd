> jp目录

![alt](imgs/hadoop-menu-use.png)



[TOC]









> 参考网址

* [官方网址](http://hadoop.apache.org/)
* [官方推荐版本](http://hadoop.apache.org/docs/stable/)





# 1：开始安装

当前官方推荐的是`2.9.2`

* [官方参考文档](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html)
* [w3cSchool教程](https://www.w3cschool.cn/hadoop)



## 1.1：必要软件

* 必须安装Java™。
* 必须安装ssh并且必须运行sshd才能使用管理远程Hadoop守护进程的Hadoop脚本
* 

### 1.1.1：安装java

参考文档

* [Hadoop支持的Java版本](https://cwiki.apache.org/confluence/display/HADOOP2/HadoopJavaVersions)
* [centos7安装jdk](https://jingyan.baidu.com/article/9f7e7ec0f8c26b6f28155433.html)

> 具体安装命令

下载一个jdk，并放到共享目录share中。 关于共享目录，见[VirtualBox共享文件夹设置](virtualbox.md#共享文件夹)。

当前apache推荐的是hadoop2.9x，配置的jdk是1.7版本。所以使用了`jdk-7u67-linux-x64.tar.gz`

```shell
# 查找机器上是否安装过jdk
$ rpm -qa | grep jdk
# 如果有，可以使用rmp -e --nodeps 来进行卸载

$ cd /opt
$ mkdir modules
# 查看共享目录中的jdk
$ ls /media/sf_share/  
$  tar -zxf  /media/sf_share/jdk-7u67-linux-x64.tar.gz -C /opt/modules
$ pwd
$ vi /etc/profile #追加java路径
$ source /etc/profile
$ java -version

```

也有网友对配置profile文件有歧义，因为在hadoop中还要配置一次。



> vi /etc/profile 的内容

```
export JAVA_HOME=/opt/modules/jdk1.7.0_67
export PATH=$PATH:$JAVA_HOME/bin
```





### 1.1.2：安装ssh

```shell
# 安装ssh于rsync ,其中rsync是用来数据同步的
$ yum install ssh
$ yum install rsync


# 补充信息，如何查询是否安装过软件了

$ rpm -qa | grep ssh 
$ ps -ef | grep ssh
$ service sshd status 

```



### 1.1.3：其他准备

网上推荐要做一下准备，但是我感觉没有必要做，所以先记录下来。

* 修改hostname
* 关闭防火墙





## 1.2: 安装Hadoop



### 1.2.1：下载Hadoop

当前官方指定的稳定版本是2.9.1，所以下载后，放到`share`目录下。



### 1.2.2：安装Hadoop

一共有三种安装模式：

- [本地（独立）模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Standalone_Operation)
- [伪分布式模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Pseudo-Distributed_Operation)
- [全分布式模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Fully-Distributed_Operation)



#### 前期准备

将hadoop 解压到指定目录，并指定`JAVA_HOME`

```shell
$ cd /opt/modules
$ mkdir apache
$ ll
# 查看共享目录下的hadoop是否存在
$ ll /media/sf_share/
# 解压到/opt/modules/apache
$ tar -zxf /media/sf_share/hadoop-2.9.2.tar.gz -C /opt/modules/apache
$ cd apache/hadoop-2.9.2
$ vi etc/hadoop/hadoop-env.sh 
# 将JavaHome 修改成export JAVA_HOME=/opt/modules/jdk1.7.0_67
```



#### 本地模式

默认情况下，Hadoop配置为以非分布式模式运行，作为单个Java进程。这对调试很有用。

以下示例复制解压缩的conf目录以用作输入，然后查找并显示给定正则表达式的每个匹配项。输出将写入给定的输出目录。

```shell
$ mkdir input
$ cp etc/hadoop/*.xml input
$ bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input output 'dfs[a-z.]+'
$ cat output/*
```

上面命令，实际是从`input`目录中按照`grep`正则表达式，找到对应的字符串，并且输出到`output`目录中。关于`mapreduce-examples`里面的例子，可以参考网友撰写的[运行 MapReduce 样例](https://blog.csdn.net/chengqiuming/article/details/78826143)



#### 伪分布式模式

为了防止配置错误，先备份一下hadoop，` tar -czvf hadoop-2.9.2-back.tar.gz hadoop-2.9.2/`。

具体配置按照官方的方法进行配置。



##### 修改配置文件

> etc/hadoop/core-site.xml:

```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>
```

注：这里要配置临时文件

```xml
 <property>
  <name>hadoop.tmp.dir</name>
  <value>file:/opt/modules/apache/hadoop-2.9.2/tmp</value>
  <description>Abasefor other temporary directories.</description>
 </property>
```

我按照官方文档没有配置tmp目录，后来有一次启动hadoop发现进入了安全模式。我怀疑是tmp目录的问题。

当然可以使用这个方式离开安全模式，但是为了不再出现这个问题，我决定把路径给修改了。如果修改了tmp是否需要重新格式化呢？ 另外原先的文件还有保存吗？

```shell
# 离开安全模式的方法
./hdfs dfsadmin -safemode leave
```









>  etc/hadoop/hdfs-site.xml:

```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
</configuration>
```

配置只分发一份。





##### 设置SSH免登录

检查你是否可以免密码登录

```shell
$ ssh localhost
```

如果你不能免密码登录，那么执行下面的命令

```shell
$ ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
$ chmod 600 ~/.ssh/authorized_keys
```

如果对上面命令不太清楚，请看这篇文档:[ssh免密码登录](https://www.cnblogs.com/lingyejun/p/7367596.html)



##### 执行MapReduce任务

下面介绍了执行MapReduce任务，如果你想执行一个YARN任务，请看下一节。



###### 1：格式化文件系统

```shell
$ bin/hdfs namenode -format
```



###### 2：启动HDFS

启动`NameNode`与`DataNode`

```shell
 $ sbin/start-dfs.sh
```

hadoop进程的log写入到了` $HADOOP_LOG_DIR`目录（默认在` $HADOOP_HOME/logs`）

![alt](imgs/hadoop-start-namenode.png)



###### 3：浏览HDFS-WEB页面

浏览`NameNode`web

- NameNode - `http://localhost:50070/`   ` http://192.168.56.102:50070/`  

大多数情况下centos中是命令行的，那么在命令行下如何看这个地址是否显示呢？

```shell
# 在命令行下安装 links后，可以看到这个网址
$ yum install links
$ links http://localhost:50070/
```

![alt](imgs/hadoop-start-namenode-links.png)



如何在其他的机器上访问这个地址？ 默认不能访问的，因为有防火墙，所以需要先执行下面命令

```shell
# 关闭防火墙，如果你不想关闭防火墙，也可以单独是打开接口，见linux-mini.md中的配置。
$  systemctl stop firewalld.service
```

关闭完后防火墙，就可以用嗅探器看到服务器的端口了。

![alt](imgs/hadoop-system-ports.png)





可以使用浏览器看到相关的信息

![alt](imgs/hadoop-web-show.png)



###### 4：创建HDFS目录

创建MapReduce要执行任务的HDFS目录

```shell
$ bin/hdfs dfs -mkdir /user
$ bin/hdfs dfs -mkdir /user/root
```



###### 5：将数据文件发布到HDFS

```shell
$ bin/hdfs dfs -put etc/hadoop input
```

上面的命令，将本地的`etc/hadoop`目录下的好多`xml`文件复制到`/user/root/input`目录下，每个文件都占用了`128MB`的空间，所以尽量不要上传小文件。


> 参考文档
* [hdfs dfs命令指南](https://www.cnblogs.com/ximiaomiao/p/7003591.html)
* [HDFS常用命令](https://www.jianshu.com/p/ced993604727)

* [hadoop fs,hadoop dfs以及hdfs dfs区别](https://blog.csdn.net/u013019431/article/details/78485555)



###### 6：执行MapReduce任务

模糊检索

```shell
 $ bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input output 'dfs[a-z.]+'
```

上面命令会在分布式服务器上的`/user/root/output`中放两个结果文件。



###### 7：检查输出结果

从分布式系统中copy文件到本地的output目录，然后查看

```shell
$ rm -rf output/
$ bin/hdfs dfs -get output output
$ cat output/*
```

或者

直接查看分布式系统上的文件

```shell
 $ bin/hdfs dfs -cat output/*
```



###### 8：执行完毕后，关闭应用

在执行下面的命令的时候，可以使用`jps`来看看执行前后的变化。

```
$ sbin/stop-dfs.sh
```



##### 通过YARN执行MapReduce

通过设置几个参数并运行`ResourceManager`守护进程和`NodeManager`守护进程，您可以在伪分布式模式下在`YARN`上运行MapReduce作业。

为了执行下面`YARN`指令，需要您提前执行[上一章节:1-4步](#执行mapreduce任务)

###### 1：配置参数

`etc/hadoop/mapred-site.xml`:

```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
</configuration>
```

`etc/hadoop/yarn-site.xml`:

```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
</configuration>
```



###### 2：启动YARN

启动`ResourceManager` 与 `NodeManager` 进程:

```shell
$ sbin/start-yarn.sh
```



###### 3：浏览ResourceManager-WEB页面



```
ResourceManager - `http://localhost:8088/
```

> jps看到多出了`ResourceManager` 与 `NodeManager` 进程

![alt](imgs/yarn-jsp.png)



> 需要把防火墙的8088端口开放出来

![alt](imgs/hadoop-firewall-yarn.png)



> web界面

![alt](imgs/yarn-result.png)



###### 4：执行MapReduce任务

按照教程，使用yarn执行MapReduce的速度比上一章节的慢很多，应该是配置的问题。下一次好好看看。

同上一章节6步，建议删除分布式环境的`output`，或者写成`output1`

模糊检索

```shell
 $ bin/yarn jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input output1 'dfs[a-z.]+'
```

上面命令会在分布式服务器上的`/user/root/output`中放两个结果文件。

可以使用`yarn jar`或者`hadoop jar`



###### 5：关闭YARN

```shell
$ sbin/stop-yarn.sh
```

这个命令只执行了关闭`yarn`，如果要关闭分布式文件系统，还要执行上一章节的第8步。



#### 集群模式



##### 硬件分配

* 一台机器安装NameNode
* 一台机器安装ResourceManger
* Web App Proxy Server和MapReduce作业历史记录服务器通常在专用硬件或共享基础架构上运行
* 集群中的其余计算机充当DataNode和NodeManager



# 2：编写MapReduce



## 2.1：基本观念

网上有很多误导，造成开发者很难入手。下面说几个概念，能帮助你快速开发。

* 开发MapReduce，不需要搭建Hadoop环境与使用HDFS分布式环境。
* 不建议使用Eclipse，因为太老了，用IDEA或者VSCODE。
* 建议在linux系统上开发，这样省了一些配置权限的问题。
  * 将参考文档中的windows上开发需要处理的内容
* 学习MapReduce开发，建议从Hadoop官方的提供的Example学起。
* 如果你不会Java开发，也可以使用其他工具来代替。



在windows系统中可能会遇到很奇怪的问题，所以建议在linux环境下开发．

## 2.2：配置本地hadoop

在ubuntu上安装hadoop环境

### 查看当前版本

看看当前版本就行，也不用修改．因为hadoop用的jdk比较老，是1.7的．

我当前系统配置的是jdk1.8，我就准备修改hadoop的配置文件，让其指向jdk1.7

```shell
# 看看当前的java环境
$ java -version
$ less /etc/profile
$ less ~/.bashrc

# 看看那些端口被使用了
$ sudo netstat -tlpn

# （参考一下，不用操作下面的命令）将java的一些功能放入到sudo 命令中
$ sudo visudo
# 在secure_path后加上JDK工具的路径，如：:/opt/jdk1.8.0_161/bin
$ ctrl+o  # 保存数据
$ ctrl+x  # 退出

```



### 安装hadoop环境

主要是安装jdk与hadoop

```shell
# 安装环境
$ cd /opt
$ sudo mkdir modules

# 改变权限，让当前登录用户可以操作这个目录
$ sudo chmod 767 modules

# 解压jdk
$ tar xvf jdk-7u67-linux-x64.tar.gz

# 解压 hadoop到apache目录下
$ mkdir apache
tar xvf hadoop-2.9.2.tar.gz -C apache/
```



### 配置hadoop环境变量

```shell
# 进入hadoop目录
$ cd /opt/modules/apache/hadoop-2.9.2/

# 看看这个目录下有那些文件使用了 JAVA_HOME
$ grep  'JAVA_HOME'  etc/hadoop/*.sh

$ vi etc/hadoop/hadoop-env.sh 
# 将JavaHome 修改成export JAVA_HOME=/opt/modules/jdk1.7.0_67

```



### 测试：本地模式

默认情况下，Hadoop配置为以非分布式模式运行，作为单个Java进程。这对调试很有用。

```shell
$ mkdir input
$ touch ./input/a.txt
$ vim ./input/a.txt
# 在a.txt 输入一些字符串，可以是多行，其中一行或多行中，其中包含有a的字符串
$ bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input output 'a[a-z.]+'
$ cat output/*
```



### 测试：伪分布模式

见[1.2.2：安装Hadoop](#1.2.2：安装Hadoop)中的描述．但是这次只上传一个简单的a.txt文件，然后进行测试，如上一步．



> 具体步骤

- 1：修改配置文件
  - etc/hadoop/core-site.xml
    - 指定hdfs的内部接口9000
  - etc/hadoop/hdfs-site.xml
    - 指定备份分数１
- 2：设置SSH免密码登录
- 3：执行MapReduce任务
  - 格式化文件系统
    - bin/hdfs namenode -format
    - 默认保存在：/tmp/hadoop-fan/
  - 启动HDFS
    - sbin/start-dfs.sh
  - 创建HDFS目录
    - bin/hdfs dfs -mkdir /user
    - bin/hdfs dfs -mkdir /user/(这里填写当前的用户名，当然-P可以自动建立父目录)
  - 将数据文件发布到HDFS
    - bin/hdfs dfs -mkdir input
    - bin/hdfs dfs -put input/a.txt input/
  - 执行MapReduce任务
    - bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input/a.txt output 'a[a-z.]+'
  - 检查输出结果
    - bin/hdfs dfs -cat output/*
  - 执行完毕后，关闭应用
    - sbin/stop-dfs.sh

> 遇到的问题

- 在执行start-dfs.sh:   提示:sed: -e 表达式 #1, 字符 19: “s”的未知选项
  - 错误原因是因为我在hadoop-env.sh文件中添加了一个echo,输出当前javaHome造成的.
  - 解决方法,删除自己添加的内容.



### 测试：YARN

还是在伪分布式环境的基础上进行测试

- 配置参数
  - etc/hadoop/mapred-site.xml
    - 配置mapreduce的框架使用yarn
  - etc/hadoop/yarn-site.xml
    - 配置mapreduce_shuffle
- 启动YARN
  - 启动hdfs
    - sbin/start-dfs.sh
  - 启动yarn
    - sbin/start-yarn.sh
- 浏览ResourceManager-WEB页面
  - http://localhost:8088/
- 执行MapReduce任务
  - bin/yarn jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input/a.txt output1 'a[a-z.]+'
- 查看结果
  - bin/hdfs dfs -cat output1/*
- 关闭YARN
  - sbin/stop-yarn.sh
  - 如果要正式退出,还需要把fds给关闭了.





## 2.2：IDEA环境的配置

IDEA是JAVA开发的主流开发工具.

### 2.2.1：主要问题

#### 选择依赖包

有两连个选择:

* 选项1:选择Hadoop下Share目录中的Jar包
  * 问题1 选那个,不知道?
* 使用Maven
  * 问题1 选那个版本?
    * 初步判断选择2.9.2这个版本
  * 问题2 选那些?
    * 不确定,但是发现`hadoop-client`看起来把主要的包都打入了,见下面的图.



![alt](imgs/idea-maven.png)



> Maven 依赖包的分类

* Compile Dependencies
  * 打包时的依赖,这些依赖会直接打入Jar包中的.
* Provided Dependencies
  * 打Jar包时,不打入
* Runtime Dependencies
  * 运行时的需要的jar包,例如tomcat环境下的jsp依赖包
* Test Dependencies
  * 测试需要的依赖包



#### 怎么打包?

官网上没有相关说明,网友的文章大多是新手实验的,所以最好把hadoop的源码下载下来研究一下.

* 是否包含依赖文件?
  * 看看hadoop提供的例子文件吧
    * 里面有pom.xml文件,打包的时候没有将这些类打包
* `hadoop-mapreduce-examples`apache
  * 源代码放在哪里了?
    * 这个例子的源码,放在了hadoop源码包的mapreduce工程下了.



### 2.2.2：开发步骤



#### 2.2.2.1：使用IDEA新工程

* 建立一个Maven工程
* 选择JDK要与hadoop保持一致,例如jdk1.7



#### 2.2.2.2：引入Hadoop依赖

只用`hadoop-client`一个就行,测试用.

```xml
    <properties>
        <hadoop.version>2.9.2</hadoop.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
    </dependencies>
```



#### 2.2.2.3：编写JAVA类

为了省事,在本地建立一个类`WordCount`,复制[hadoop官方提供的例子代码](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)到本地.



#### 2.2.2.4：建立输入文件

* 建立一个目录`input`
* 在这个目录中建立一个a.txt文件,并输入几个单词



#### 2.2.2.5：配置参数

配置main函数执行的input与output目录

![alt](imgs/idea-set-para.png)





#### 2.2.2.6：进行Debug

![alt](imgs/idea-debug.png)



#### 2.2.2.7：查看结果

在output目录中可以看到具体的结果.

如果下次再看的话,需要删除`output`目录,不然提示错误.

![alt](imgs/idea-mapreduce-result.png)



#### 2.2.2.8：到伪分布系统上执行



##### 配置打包工具

通过maven进行打包,编辑pom.xml.

* 追加一个`maven-jar-plugin`
* 指定`mainClass`

```xml
    <packaging>jar</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>wukong.WordCount</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
     </build>   
```

##### 进行打包

![alt](imgs/idea-packageing.png)



> 打包后的结果放在target

![alt](imgs/idea-jar-file.png)



##### 进行测试

先得到生成Jar包的路径,然后进入到hadoop目录中

```shell
# 进入到hadoop目录中
$ cd /opt/modules/apache/hadoop-2.9.2/

# 由于配置的hdfs服务,所有要先启动这个服务
$ sbin/start-dfs.sh

# 查看input与output是否存在
$ bin/hdfs dfs -ls

# 如果存在output就删除
$ bin/hdfs dfs -rm -r output

# 执行命令,要写上mapduce的jar路径
$ bin/hadoop jar /home/fan/001-db/wukong-bd/examples/mapreduce/helloworld/target/mapreduce-exapmles-hello-1.0.jar input/a.txt output

# 查看结果
$ bin/hdfs dfs -cat output/*

# 关闭服务
$ sbin/stop-dfs.sh
```

> 哈哈终于出结果了

![alt](imgs/idea-jar-run-server.png)









## 2.3：例子分析



> MapReduce 接口

* `Mapper`和`Reducer`接口。应用程序通常会实现它们以提供`map`和`reduce`方法。
* 核心接口，包括`Job`，`Partitioner`，`InputFormat`，`OutputFormat`等。
* 一些有用功能，如`DistributedCache`，`IsolationRunner`等。





### 2.3.1：Hello-World

hadoop官网上的入门程序,[官网的说明](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html).

我在IDEA中debug的过程,已经上传到服务器上了.[源代码与文档](https://github.com/fanhualei/wukong-bd/tree/master/examples/mapreduce/helloworld)

* 一个基本的例子
* 一个复杂的例子,里面待了过滤器等.

### 2.3.2：Hadoop-mapreduce-examples

hadoop官方的例子,可以参考一下,[源代码与文档](https://github.com/fanhualei/wukong-bd/tree/master/examples/mapreduce/hadoop-examples)



### 2.3.3:wukong-mapreduce-examples

[参考了网友的案例分析](https://blog.csdn.net/jin6872115/article/category/7513962)



> 参考文档

* [IDEA 开发hadoop项目配置及打包](https://blog.csdn.net/a377987399/article/details/80510776)

