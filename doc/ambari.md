# Ambari

ambari的公司HDP被CDH收购了，所以ambari是否继续流行下去还有疑问。

ambari的安装，一般要使用HDP的一个类包，如果不使用，听说很麻烦。

CDH虽然很流行，但是有限制，如果超过100台机器，就不能用了。所以在技术选型的过程中，要考虑好选那个。



## 1. 安装



### 1.1.1 前提条件

> 安装JAVA



> 



> 安装Maven

```shell
tar -zxvf apache-maven-3.6.2-bin.tar.gz
vi /etc/profile
source /etc/profile
echo $MAVEN_HOME
mvn -v
```

配置maven的环境变量`/etc/profile`

```
# Maven
export MAVEN_HOME=/opt/modules/apache/apache-maven-3.6.2
export PATH=$PATH:${MAVEN_HOME}/bin
```



