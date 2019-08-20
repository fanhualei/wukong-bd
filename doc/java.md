# Java 使用



JDK的版本，一定要和Hadoop Hive HBase ZooKeeper 等保持一致。

按照当前的版本，我在前期使用了1.7版本，后来发现也可以使用1.8版本，所以后来总体升级到了1.8版本。



> Linux环境变量设置文件

/etc/profile 全局用户，应用于所有的Shell。

/$HOME/.profile 当前用户，应用于所有的Shell。

/etc/bash_bashrc 全局用户，应用于Bash Shell。

~/.bashrc 局部当前，应用于Bash Sell。



参考文档

- [Hadoop支持的Java版本](https://cwiki.apache.org/confluence/display/HADOOP2/HadoopJavaVersions)
- [centos7安装jdk](https://jingyan.baidu.com/article/9f7e7ec0f8c26b6f28155433.html)



## 安装java1.7



> 具体安装命令

下载一个jdk，并放到共享目录share中。 关于共享目录，见[VirtualBox共享文件夹设置](https://github.com/fanhualei/wukong-bd/blob/master/doc/virtualbox.md#共享文件夹)。

当前apache推荐的是hadoop2.9x，配置的jdk是1.7版本。所以使用了`jdk-7u67-linux-x64.tar.gz`

```
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





## 安装JDK1.8

[下载地址](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

如果是linux，可以下载Linux x64： [dk-8u221-linux-x64.tar.gz](javascript: void(0))

使用了`fanhl@189.cn`这个用户名登录了Oracle

```shell
$ tar -zxvf /media/sf_share/jdk-8u221-linux-x64.tar.gz -C /opt/modules/
$ cd /opt/modules/
## /opt/modules/jdk1.8.0_221
```





> vi /etc/profile 的内容

```
export JAVA_HOME=/opt/modules/jdk1.7.0_67
export PATH=$PATH:$JAVA_HOME/bin
```

使用`source /etc/profile`后，退出系统，重新登录。



## 使用了Java-home的文件

```shell
# 查看java 版本与地址
$ java -version
$ whereis java
$ echo ${JAVA_HOME}

#grep -r "{关键字}"  {路径}
$ grep  -r  '/opt/modules/jdk*' ./

# 配置文件
$ less /etc/profile 


# hadoop-2.9.2
$ cd /opt/modules/apache/hadoop-2.9.2
$ grep  -r  '/opt/modules/jdk*' ./
$ less ./etc/hadoop/hadoop-env.sh

# hive-2.3.5
$ cd /opt/modules/apache/hive-2.3.5
$ grep  -r  '/opt/modules/jdk*' ./


# sqoop-1.4.7
$ cd /opt/modules/apache/sqoop-1.4.7
$ grep  -r  '/opt/modules/jdk*' ./

```









