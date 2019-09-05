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

