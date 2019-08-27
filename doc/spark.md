# spark



## 1. 基本概念



Spark2.4.3在Windows和类UNIX系统（例如Linux，Mac OS）上运行。在一台机器上本地运行很容易 - 您只需要`java`在系统上安装`PATH`，或者`JAVA_HOME`指向Java安装的环境变量。

Spark2.4.3运行在Java 8 +，Python 2.7 + / 3.4 +和R 3.1+上。对于Scala API，Spark 2.4.3使用Scala 2.12与Scala2.11。如果您下载的是官方编译好的版本，那个版本默认是使用的是Scala2.11.12(这个版本经常变)。

请注意，自Spark 2.2.0起，对2.6.5之前的Java 7，Python 2.6和旧Hadoop版本的支持已被删除。自2.3.0起，对Scala 2.10的支持被删除。自Spark 2.4.1起，对Scala 2.11的支持已被弃用，将在Spark 3.0中删除。



### 1.1. 运行模式

* 本地
* standalone
  * spark做了一个类似yarn的东西，不常用．
* yarn



### 1.2. 日志聚合 

spark会产生很多日志，那么日志保存在哪里？ 如何将这些日志聚合在一起。

* 配置参数开启日志
* 使用`sbin/start-history-server.sh`开启日志服务
* 可以通过http访问18080端口上的日志。





## 2. 快速入门

参考文档:[Spark学习之路 （五）Spark伪分布式安装](https://www.cnblogs.com/qingyunzong/p/8903714.html)

### 2.1. 安装必要软件



#### 2.1.1. 安装hadoop

我安装了的是`/opt/modules/apache/hadoop-2.9.2`,配置了伪分布式模式.

可以启动hdfs与yarn

具体安装步骤省略,可以参考上面的文档,或者看hadoop章节的文档.



#### 2.1.2. 安装scala



请安装用Scala11,因为当前用的spark-2.4.3默认的是[**scala11.12**](https://www.scala-lang.org/download/2.11.12.html)

```
最新版本是 Scala 2.13.0 不要用，也不要使用 Scala 2.12.x
```

具体安装步骤省略,可以参考上面的文档,



### 2.２ 安装spark

> 如何知道spark的默认scala的版本？

启动`bin/spark-shell` 看提示，例如：`scala11.12` 

如果spark版本与Idea开发的版本不一致,那么在提交到yarn时,会出现错误.



> 配置spark-env.sh

```shell
cd /opt/modules/apache/spark-2.4.3-bin-hadoop2.7
cd conf/
cp spark-env.sh.template spark-env.sh
vi spark-env.sh
```

> 具体内容

```
export JAVA_HOME=/opt/jdk1.8.0_161
export SCALA_HOME=/opt/modules/apache/scala-2.11.12
export HADOOP_HOME=/opt/modules/apache/hadoop-2.9.2
export HADOOP_CONF_DIR=/opt/modules/apache/hadoop-2.9.2/etc/hadoop
export SPARK_MASTER_IP=127.0.0.1
export SPARK_MASTER_PORT=7077
```



### 2.3. 使用spark-shell

假设hfds上上面的 input目录下,已经有了一个a.txt文件

spark-shell，可以不再启动spark的状态下使用．

```shell
# 进入shell命令区域
$ bin/spark-shell

# 读取hadoop上的一个文件
scala> val file = sc.textFile("input/a.txt")

# 查看文件的内容
scala> file.collect

# 查看第一行
scala> file.first

# 查看前2行
scala> file.take(2)

# 输出所有内容
scala> file.collect().foreach {println}

# 统计单词数量, 撰写下面的代码,并通过:paste复制到命令行
scala> :paste 

# 退出 spark shell
:quit
```

复制下面的脚本

```
file.flatMap(_.split(" "))
			.map((_,1))
			.reduceByKey(_+_)
			.sortBy(tuple => tuple._2,true)
			.foreach {println}
			
```

![alt](imgs/spark-shell-count.png)



### 2.4. 使用yarn

在实际环境中常用这种模式，经常用这种形式.

下面执行spark自带的例子

```shell
./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode client \
    examples/jars/spark-examples*.jar \
    10
```





### 2.5. 使用spark-standalone

spark-standalone不常用，可以跳过这个章节



> 启动

进入spark根目录

```shell
sbin/start-all.sh 
jps
```

![alt](imgs/spark-start-all.png)



>  验证

理论上要将jar提交到这里，但是这个不常用，所有就不演示了．

输入:http://127.0.0.1:8080/  可以看到相关界面.

![alt](imgs/spark-server.png)



>  关闭

```shell
# 退回到spark home
$ sbin/stop-all.sh
```





## 3. 如何开发Spark程序？

可以使用IDEA开发Spark，具体见：[如何开发一个Spark程序](../examples/spark/readme.md)

* 代码开发
  * 在IDEA添加scala插件
  * 新建一个Maven的Java工程
  * 修改pom.xml，添加spark依赖
  * 在main目录中，建立scala目录，放scala文件
  * 撰写一个scala文件，并进行调试
* 打包
  * 注释掉本地调试代码
  * 修改maven中plugin，进行scala打包

* 执行
  * 通过spark命令，在yarn中执行．
  * 通过窗口或日志，查看执行结果．



## 4.  开发技巧



### 4.1. 构建sc

任何Spark程序的第一步都是先创建SparkSession。在Spark-Shell或者其他交互模式中，SparkSession已经预先被创建好了，但在正常编写的Spark程序中，我们必须手动创建SparkSession。

在一些遗留的Spark代码中，我们一般使用 **new SparkContext** 这种模式。但在新的Spark版本中，我们应该避免使用这种模式，尽量使用SparkSession，因为它可以更健壮地实例化Spark和SQL Contexts，并确保没有Context冲突。

> 下面是新版本的做法

如果在idea中本地执行，需要VM参数中设定：`-Dspark.master=local`

```scala
package wukong.spark
import org.apache.spark.sql.SparkSession
object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "input/a.txt" // Should be some file on your system
    val spark = SparkSession.builder
      .appName("Simple Application")
      .getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    logData.collect().foreach(println)
    val numAs = logData.filter(line => line.contains("c")).count()
    val numBs = logData.filter(line => line.contains("d")).count()
    println(s"\nLines with c: $numAs, Lines with d: $numBs")
    spark.stop()
  }
}
```

在yarn中执行

```shell
./bin/spark-submit --class wukong.spark.SimpleApp \
    --master yarn \
    --deploy-mode client \
    /home/fan/001-db/wukong-bd/examples/spark/hello/target/hello-1.0.jar \
    
```



使用`SparkSession`发现经常提示错误，[解决Spark2.0之后，报错No implicits found for parameter evidence$6: Encoder](https://blog.csdn.net/dz77dz/article/details/88802577)

![alt](imgs/spark-error-rdd.png)

### 4.2. 累加器与共享变量

[Spark共享变量(广播变量、累加器)](https://www.cnblogs.com/itboys/p/10596497.html)



### 4.3. 依赖第三方Jar包

有两种做法，一个是在生成自己包时，直接把第三方包打入到jar文件中．　这样做，jar文件比较大．

还有一种是，将jar提前上传．

`bin/spark-shell --jars **** ` 

* [[Spark Core\] Spark 使用第三方 Jar 包的方式](https://www.cnblogs.com/share23/p/9768308.html)



也可以将数据提交到hdfs

* [spark-on-yarn作业提交缓慢优化之spark jar包处理](http://blog.itpub.net/29609890/viewspace-2214787/)



## 5. Spark Sql



### 5.1. RDD DataFrame DataSet

* RDD 最原始的数据集，以一行为单位
* DataFrames 带有schema元数据
  * 知道每一列的名称与类型
* DataSet
  * 可以使用map，flatmap等函数进行转换
  * DataFrame是一个Dataset的特例，等价于Dataset[Row]; 
  * Dataset的操作分为transformation和action 两种，transformation用于创建新的Dataset，而action用于计算操作；
  * 它和RDD一样具有惰性，只有action操作被调用的时候才会进行计算；
  * DataSet创立需要一个显式的Encoder，把对象序列化为二进制

#### 5.1.1 相同点

* RDD、DataFrame、Dataset全都是spark平台下的分布式弹性数据集
* 三者都有惰性机制，在进行创建、转换，如map方法时，不会立即执行。只有在执行action操作的时候，才会进行计算。



#### 5.1.2. 不同点

> RDD和 DataFrame的比较

* 前者没有schema信息；后者有schema信息
* RDD无法得知所存的数据元素的具体内部结构，Spark Core只能在stage层面进行简单的优化；后者因为有schema信息，Sparl SQL的查询优化器就可以进行针对性的优化
* RDD通过函数式调用API，虽然简洁明了，但是需要创建新的对象，不容易重用旧的对象，给GC带来挑战；DataFrame是尽可能的重用对象



> DataFrame和 Dataset的比较

* DataFrame是带有类型的，即需要指定类型；但是DataSet无需指定类型。DataFrame是一个Dataset的特例，等价于Dataset[Row]
* DataFrame带有schema，而DataSet没有schema



#### 5.1.3. 三者相互转换



##### 5.1.3.1 创建RDD

```scala
//创建rdd
val rdd=sc.makeRDD(List("Mina,19","Andy,30","Michael,29"))

```



##### 5.1.3.2 RDD与DataFrame互转



###### 5.1.3.2.1 使用toDF("fieldName1","fieldName2")函数

通过`.toDF("name","age")`

```
import spark.implicits._

//RDD转换成DataFrame
val　df=　rdd.map{x=>val par=x.split(",");(par(0),par(1).toInt)}.toDF("name","age")
df.show
```



###### 5.1.3.2.2 通过一个Java类转

```scala
case class Person(name:String,age:Int)
val df = rdd.map{x => val par = x.split(",");Person(par(0),par(1).toInt)}.toDF
df.show
```



###### 5.1.3.2.3. DataFrame转RDD

df.rdd 返回的是 RDD[Row] 如果需要获取Row中的数据，那么可以通过`getString(0)  getAs[String]("name")   `

```
df.rdd.map(x=>x.getAs[String]("name")).collect
```



##### 5.1.3.3 RDD与DataSet互转



###### 5.1.3.3.1. 使用calss类

将toDF换成toDS

```shell
val ds = rdd.map{x => val par = x.split(",");Person(par(0),par(1).trim().toInt)}.toDS
```



###### 5.1.3.3.2. DataSet转换成RDD

ds.rdd 返回的是 RDD[Person] 直接读取Person的属性即可

```shell
ds.rdd
ds.rdd.map(_.name).collect
```



##### 5.1.3.4 DataFrame与DataSet互转

###### 5.1.3.4.1 DataSet转换成DataFrame

```scala
val df =ds.toDF
```



##### 5.1.3.4.2 DataFrame转换成DataSet

df.as[Person] 注意 dataframe中的列名和列的数量应该和case class 一一对应

```shell
case class Person(name:String,age:Int) 
val ds=df.as[Person]
```



### 5.2. 开始



### 5.3. 数据源

Spark SQL通过DataFrame接口对各种数据源进行操作。

DataFrame可以使用`relational transformations `进行操作，也可以用于创建临时视图。通过临时视图可以运行SQL查询。

本节介绍使用Spark数据源加载和保存数据的一般方法，然后介绍可用于内置数据源的特定选项。

[官方文档](http://spark.apache.org/docs/latest/sql-data-sources.html)

### 5.3.1. 通用加载与保存函数

http://spark.apache.org/docs/latest/sql-data-sources-load-save-functions.html

##### 5.3.1.1 手动指定参数

可以支持的文件格式： (`json`, `parquet`, `jdbc`, `orc`, `libsvm`, `csv`, `text`)

例如：

```scala
val peopleDF = spark.read.format("json").load("examples/src/main/resources/people.json")
peopleDF.select("name", "age").write.format("parquet").save("namesAndAges.parquet")

val peopleDFCsv = spark.read.format("csv")
  .option("sep", ";")
  .option("inferSchema", "true")
  .option("header", "true")
  .load("examples/src/main/resources/people.csv")

usersDF.write.format("orc")
  .option("orc.bloom.filter.columns", "favorite_color")
  .option("orc.dictionary.key.threshold", "1.0")
  .save("users_with_options.orc")


```

也有另外一种写法

```scala
val df = spark.read.json(resourcePath+"/people.json")
```





##### 5.3.1.2 通过文件或目录加载



##### 5.3.1.3 保存的模式



##### 5.3.1.4 保存持久表中



##### 5.3.1.5 木桶，排序，分区









参考文档：

* [Spark学习之路 （十八）SparkSQL简单使用](https://www.cnblogs.com/qingyunzong/p/8987579.html)
* [行与列存储的对比](https://my.oschina.net/u/3754001/blog/1811926)
* [官方英文文档](http://spark.apache.org/docs/latest/sql-programming-guide.html)









> 参考文档

* [官方首页](http://spark.apache.org/)
  * [快速开始](http://spark.apache.org/docs/latest/quick-start.html)
* 网友文章
  * [Spark学习老铁](https://www.cnblogs.com/qingyunzong/category/1202252.html)
  * [Spark项目实践](https://www.cnblogs.com/qingyunzong/category/1219125.html)
  * 安装
    * [spark单机搭建](https://www.cnblogs.com/zixilonglong/p/9382343.html)
    * [Spark学习之路 （五）Spark伪分布式安装](https://www.cnblogs.com/qingyunzong/p/8903714.html)



