# Hadoop



> 参考网址

* [官方网址](http://hadoop.apache.org/)
* [官方推荐版本](http://hadoop.apache.org/docs/stable/)





## 1：开始安装

当前官方推荐的是`2.9.2`

* [官方参考文档](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html)
* [w3cSchool教程](https://www.w3cschool.cn/hadoop)



### 1.1：必要软件

* 必须安装Java™。
* 必须安装ssh并且必须运行sshd才能使用管理远程Hadoop守护进程的Hadoop脚本
* 

#### 1.1.1：安装java

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





#### 1.1.2：安装ssh

```shell
# 安装ssh于rsync ,其中rsync是用来数据同步的
$ yum install ssh
$ yum install rsync


# 补充信息，如何查询是否安装过软件了

$ rpm -qa | grep ssh 
$ ps -ef | grep ssh
$ service sshd status 

```



#### 1.1.3：其他准备

网上推荐要做一下准备，但是我感觉没有必要做，所以先记录下来。

* 修改hostname
* 关闭防火墙





### 1.2: 安装Hadoop



#### 1.2.1：下载Hadoop

当前官方指定的稳定版本是2.9.1，所以下载后，放到`share`目录下。



#### 1.2.2：安装Hadoop

一共有三种安装模式：

- [本地（独立）模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Standalone_Operation)
- [伪分布式模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Pseudo-Distributed_Operation)
- [全分布式模式](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html#Fully-Distributed_Operation)



##### 前期准备

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



##### 配置本地模式

默认情况下，Hadoop配置为以非分布式模式运行，作为单个Java进程。这对调试很有用。

以下示例复制解压缩的conf目录以用作输入，然后查找并显示给定正则表达式的每个匹配项。输出将写入给定的输出目录。

```shell
$ mkdir input
$ cp etc/hadoop/*.xml input
$ bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.9.2.jar grep input output 'dfs[a-z.]+'
$ cat output/*
```

上面命令，实际是从`input`目录中按照`grep`正则表达式，找到对应的字符串，并且输出到`output`目录中。关于`mapreduce-examples`里面的例子，可以参考网友撰写的[运行 MapReduce 样例](https://blog.csdn.net/chengqiuming/article/details/78826143)





