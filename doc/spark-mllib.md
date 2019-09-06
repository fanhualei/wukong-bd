

# 1. 基本概念

https://www.cnblogs.com/swordfall/p/9456222.html



## 1.1. 常用名词

| 单词               | 解释                               | 备注                                  |
| ------------------ | ---------------------------------- | ------------------------------------- |
| features           | 特征                               |                                       |
| variance           | 变化幅度                           |                                       |
| weight             | 权重                               |                                       |
| metrics            | 衡量指标                           |                                       |
| summary            | 汇总                               |                                       |
| mean               | 折中，平均值                       |                                       |
| Vector             | 向量                               |                                       |
| Dense Vector       | 稠密向量                           |                                       |
| Sparse Vector      | 稀疏向量                           |                                       |
| Labeled Point      | 标记点                             |                                       |
| LIBSVM             | 一种文件格式                       | label index1:value1 index2:value2 ... |
| Matrix             | 矩阵                               |                                       |
| Dense Matrix       | 局部矩阵                           |                                       |
| Row Matrix         | 最基本的分布式矩阵类型             | 是面向行且行索引无意义的分布式矩阵    |
| Indexed Row Matrix | 面向索引的分布式矩阵               |                                       |
| Coordinate Matrix  | 坐标列表(COO)格式存储的分布式矩阵  |                                       |
| Block Matrix       | 由RDD[MatrixBlock]支持的分布式矩阵 |                                       |
| Correlations       | 相关系数矩阵;相关性;               |                                       |
|                    |                                    |                                       |
|                    |                                    |                                       |



## 1.2. 基础统计

MLlib提供了很多统计方法，包括摘要统计、相关统计、分层抽样、假设校验、随机数生成等。这些都涉及统计学、概率论的专业知识。



### 1.2.1 摘要统计

调用Statistics类的colStats方法，可以获得RDD[Vector]的列的摘要统计。colStats方法返回了MultivariateStatisticalSummary对象，MultivariateStatisticalSummary对象包含了列的**最大值、最小值、平均值、方差、非零元素的数量以及总数**。

```scala
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.stat.{MultivariateStatisticalSummary, Statistics}
val observations: RDD[Vector] = ...
val summary: MultivariateStatisticalSummary = Statistics.colStats(observations)
println(summary.mean) //每个列值组成的密集向量
println(summary.variance) //列向量方差
println(summary.numNonzeros) //每个列的非零值个数
```



### 1.2.2 相关统计

计算两个序列之间的相关性是统计中通用的操作。

MLlib提供了计算多个序列之间相关统计的灵活性。目前支持的关联方法运用了皮尔森相关系数(Pearson correlation coefficient)和斯皮尔森相关系统(Spearman's rank correlation coefficient)。

* 皮尔森相关系数
* 斯皮尔森相关系统

[相关代码](http://spark.apache.org/docs/latest/mllib-statistics.html#correlations)



### 1.2.3 分层抽样

某市现有机动车共1万辆，其中大巴车500辆，小轿车6000辆，中拔车1000辆，越野车2000辆，工程车500辆。现在要了解这些车辆的使用年限，决定采用分层抽样方式抽取100个样本。按照车辆占比，各类车辆的抽样数量分别为5,60,10,20,5.

摘要统计和相关统计都集成Statistics中，而分层抽样只需要调用RDD[(K,V)]的sampleByKey和sampleByKeyExact即可。

[官方说明](http://spark.apache.org/docs/latest/mllib-statistics.html#stratified-sampling)



### 1.2.4 假设校验

假设校验(hypothesis testing) 是数理统计学中根据一定假设条件由样本推断总体的一种方法。

如果对总体的某种假设是真实的，那么不利于或不能支持这一假设的事件A(小概率事件)在一次试验中几乎不可能发生；要是在一次试验中A竟然发生了，就有理由怀疑该假设的真实性，拒绝这一假设。小概率原理可以用图表示。

![](https://images2018.cnblogs.com/blog/1217276/201808/1217276-20180813125000552-1724762827.png)

H0表示原假设，H1表示备选假设。常见的假设校验有如下几种：

- 双边校验：H0:u = u0，H1:u=/u0
- 右侧单边校验：H0:u<=u0，H1:u>u0
- 左侧单边校验：H0:u>=u0,H1:u<u0

假设校验是一个强大的工具，无论结果是否偶然的，都可以决定结果是否具有统计特征。MLlib目前支持皮尔森卡方测试。输入数据的类型决定了是做卡方适合度检测还是独立性检测。卡方适合度检测的输入数据类型应当是向量，而卡方独立性检测需要的数据类型是矩阵。RDD[LabeledPoint]可以作为卡方检测的输入类型。下列演示了如何使用假设校验。

[官方文档](http://spark.apache.org/docs/latest/mllib-statistics.html#hypothesis-testing)

"examples/src/main/scala/org/apache/spark/examples/mllib/HypothesisTestingExample.scala" 



### 1.2.5 随机数生成

RandomRDDs提供了工厂方法创建RandomRDD和RandomVectorRDD。下面的例子中生成了一个包含100万个double类型随机数的RDD[double]，其值符合标准正太分布N(0，1)，分布于10个分区，然后将其映射到N(1, 4)。

[官方文档](http://spark.apache.org/docs/latest/mllib-statistics.html#random-data-generation)

"examples/src/main/scala/org/apache/spark/examples/mllib/RandomRDDGeneration.scala" 

```
Generated RDD of 10000 examples sampled from the standard normal distribution
  First 5 samples:
    0.23566573739009636
    -0.7831160795384603
    -0.5851196752581529
    0.8561758467659356
    1.7175890143503154
Generated RDD of 10000 examples of length-2 vectors.
  First 5 samples:
    [0.6687899707584857,-0.7659275401389123]
    [1.8083451352746525,1.6253883395004034]
    [-0.008999435854191652,-1.1174563975800227]
    [2.5128197852609175,-0.05034005848024162]
    [-1.3291427770006714,-2.01906294568757]
```



## 1.3 回归与统计

| 问题类型   | 支持的方法                                                   |
| :--------- | :----------------------------------------------------------- |
| 二进制分类 | 线性SVM，逻辑回归，决策树，随机森林，梯度提升树，朴素贝叶斯  |
| 多类分类   | 逻辑回归，决策树，随机森林，朴素贝叶斯                       |
| 回归       | 线性最小二乘，套索，岭回归，决策树，随机森林，梯度增强树，等渗回归 |











# 机器学习库（MLlib）指南

MLlib是Spark的机器学习（ML）库。其目标是使实用的机器学习可扩展且简单。从较高的层面来说，它提供了以下工具：

- ML算法：常见的学习算法，如分类，回归，聚类和协同过滤
- 特征化：特征提取，转换，降维和选择
- 管道：用于构建，评估和调整ML管道的工具
- 持久性：保存和加载算法，模型和管道
- 实用程序：线性代数，统计，数据处理等。

# 声明：基于DataFrame的API是主要API

**基于MLlib RDD的API现在处于维护模式。**

从Spark 2.0开始，软件包中基于[RDD](http://spark.apache.org/docs/latest/rdd-programming-guide.html#resilient-distributed-datasets-rdds)的API `spark.mllib`已进入维护模式。Spark的主要机器学习API现在是[包中](http://spark.apache.org/docs/latest/sql-programming-guide.html)基于[DataFrame](http://spark.apache.org/docs/latest/sql-programming-guide.html)的API `spark.ml`。

*有什么影响？*

- MLlib仍将支持基于RDD的API `spark.mllib`以及错误修复。
- MLlib不会为基于RDD的API添加新功能。
- 在Spark 2.x版本中，MLlib将为基于DataFrames的API添加功能，以实现与基于RDD的API的功能奇偶校验。
- 在达到功能奇偶校验（粗略估计Spark 2.3）之后，将弃用基于RDD的API。
- 预计基于RDD的API将在Spark 3.0中删除。

*为什么MLlib会切换到基于DataFrame的API？*

- DataFrames提供比RDD更加用户友好的API。DataFrame的许多好处包括Spark数据源，SQL / DataFrame查询，Tungsten和Catalyst优化以及跨语言的统一API。
- 基于DataFrame的MLlib API跨ML算法和多种语言提供统一的API。
- DataFrames有助于实用的ML管道，特别是功能转换。有关详细信息，请参阅[管道指南](http://spark.apache.org/docs/latest/ml-pipeline.html)。

*什么是“Spark ML”？*

- “Spark ML”不是官方名称，但偶尔用于指代基于MLlib DataFrame的API。这主要是由于`org.apache.spark.ml`基于DataFrame的API使用的Scala包名称，以及我们最初用来强调管道概念的“Spark ML Pipelines”术语。

*MLlib已被弃用吗？*

- MLlib包括基于RDD的API和基于DataFrame的API。基于RDD的API现在处于维护模式。但是两个API都不被弃用，整个也没有MLlib。



# 基本统计



**目录**

- [关联](http://spark.apache.org/docs/latest/ml-statistics.html#correlation)
- [假设检验](http://spark.apache.org/docs/latest/ml-statistics.html#hypothesis-testing)
- [累积器](http://spark.apache.org/docs/latest/ml-statistics.html#summarizer)

## 关联

计算两个数据系列之间的相关性是统计学中的常见操作。在`spark.ml` 我们提供的灵活性来计算多个系列两两之间的相关性。支持的相关方法目前是Pearson和Spearman的相关性。

```scala
import org.apache.spark.ml.linalg.{Matrix, Vectors}
import org.apache.spark.ml.stat.Correlation
import org.apache.spark.sql.Row

val data = Seq(
  Vectors.sparse(4, Seq((0, 1.0), (3, -2.0))),
  Vectors.dense(4.0, 5.0, 0.0, 3.0),
  Vectors.dense(6.0, 7.0, 0.0, 8.0),
  Vectors.sparse(4, Seq((0, 9.0), (3, 1.0)))
)

val df = data.map(Tuple1.apply).toDF("features")
val Row(coeff1: Matrix) = Correlation.corr(df, "features").head
println(s"Pearson correlation matrix:\n $coeff1")

val Row(coeff2: Matrix) = Correlation.corr(df, "features", "spearman").head
println(s"Spearman correlation matrix:\n $coeff2")
```

在Spark repo中的“examples / src / main / scala / org / apache / spark / examples / ml / CorrelationExample.scala”中找到完整的示例代码。





> 参考文档

* [Spark机器学习库（MLlib）官方指南手册中文版](https://blog.csdn.net/liulingyuan6/article/details/53582300)
* [机器学习库（MLlib）指南](http://ifeve.com/spark-mllib/)
* [Spark MLlib 机器学习](https://www.cnblogs.com/swordfall/p/9456222.html)

