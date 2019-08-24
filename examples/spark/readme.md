# spark 例子



##  1. 快速入门-hello

撰写一个简单的spark代码



创建一个新的scala功能, 网上有很多教程,但是错误很多. 例如要通过maven来生成等,结果模板很旧.

我的具体步骤如下:

> 前提条件

idea中安装`scala`插件



### 1.1. scala开发



#### 1.1.1. 生成java工程

使用maven生成一个空的java工程

![alt](doc/imgs/new-project-step1.png)





#### 1.1.2. 编写scala代码

* 在main下建立一个scala的目录
* 生成一个scala的object



```scala
package wukong

object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
  }
}
```



![alt](doc/imgs/new-project-code.png)





#### 1.1.3. 运行程序

右键`Hello.scala`运行,然后显示hello world



#### 1.1.4. 添加Maven

不添加也行,主要是为了跟踪,看到scala的代码

```xml
    <properties>
        <scala.version>2.13.0</scala.version>
    </properties>

    <dependencies>
        <!-- 导入scala的依赖 -->
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```



### 1.2. spark开发



#### 1.2.1. 配置Maven

这里使用了 scala2.12.9版本

```xml
    <properties>
        <scala.version>2.12.9</scala.version>
        <spark.scala.version>2.12</spark.scala.version>
        <spark.version>2.4.3</spark.version>
    </properties>

    <dependencies>
        <!-- 导入scala的依赖 -->
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_${spark.scala.version}</artifactId>
            <version>${spark.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_${spark.scala.version}</artifactId>
            <version>${spark.version}</version>
        </dependency>

        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-streaming_${spark.scala.version}</artifactId>
            <version>${spark.version}</version>
        </dependency>

    </dependencies>
```



#### 1.2.2. 代码开发

从本地文件中,统计单词的数量

```scala
package wukong
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

/**
 * 读取本地的一个文件,并且计算文件内容
 */
object Spark {

  def main(args: Array[String]): Unit = {

    val config = new SparkConf().setAppName("WordCount").setMaster("local")
    val sparkContext = new SparkContext(config)

    // Should be some file on your system
    val logFile = "/opt/modules/apache/spark-2.4.3-bin-hadoop2.7/README.md"
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()


    logData.collect().foreach {println}

    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()

  }

}
```



#### 1.2.3. 本地调试

 用idea的开发环境,就可以对新开发的程序进行调试.	



### 1.3. 打包

直接使用Maven进行打包就行



### 1.4. Yarn中执行

[Spark on YARN-Cluster和YARN-Client的区别](https://blog.csdn.net/wjl7813/article/details/79968423)

#### 1.4.1. 启动相关服务



前提,要配置`yarn-site.xml`

```xml
    <property>
    		<name>yarn.nodemanager.pmem-check-enabled</name>
    		<value>false</value>
     </property>
     <property>
		<name>yarn.nodemanager.vmem-check-enabled</name>
    		<value>false</value>
     </property>
```



启动hadoop 与 yarn,可以不用启动`spark`

```shell
cd /opt/modules/apache/hadoop-2.9.2/
sbin/start-dfs.sh
sbin/start-yarn.sh
```

#### 1.4.2. 以client的方式启动



```shell
./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode client \
    --driver-memory 4g \
    --executor-memory 2g \
    examples/jars/spark-examples*.jar \
    10

./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode client \
    examples/jars/spark-examples*.jar \
    10


# can also be`yarn-client` for client mode
bin/spark-submit \
--class wukong.spark.Spark \
--master yarn \
--deploy-mode client \
/home/fan/001-db/wukong-bd/examples/spark/hello/target/hello-1.0.jar

```



#### 1.4.3 以群组方式处理



#### 1.4.4 相关技巧



#### 1.4.5 常见错误处理



##### 1.4.5.1 NoSuchMethodError 版本冲突

>  错误提示:

java.lang.NoSuchMethodError: scala.Predef$.refArrayOps([Ljava/lang/Object;)

原因是spark自带的版本与idea开始时用的maven依赖包不一致.

> 处理方法

版本一致就行.

[IDEA spark 中scala的编译版本问题解决NoSuchMethodError （jvm ）](https://www.jianshu.com/p/d4c109549f81)

##### 1.4.5.2. 内存不足

ERROR TransportClient: Failed to send RPC RPC 8072953585929920326 to /192.168.1.107:58682: java.nio.channels.ClosedChannelException


前提,要配置`yarn-site.xml`

```xml
    <property>
    		<name>yarn.nodemanager.pmem-check-enabled</name>
    		<value>false</value>
     </property>
     <property>
		<name>yarn.nodemanager.vmem-check-enabled</name>
    		<value>false</value>
     </property>
```







## 参考文档



* [Spark On YARN](https://www.cnblogs.com/langfanyun/p/8040136.html)

















