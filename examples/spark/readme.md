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



#### 1.2.3. 代码分析

 



