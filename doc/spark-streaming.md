# Spark Streaming 编程

[官方介绍](http://spark.apache.org/docs/latest/streaming-programming-guide.html)

## 概述

Spark Streaming是核心Spark API的扩展，可实现实时数据流的可扩展，高吞吐量，容错流处理。数据可以从许多来源（如Kafka，Flume，Kinesis或TCP套接字）中提取，并且可以使用以高级函数表示的复杂算法进行处理`map`，例如`reduce`，`join`和`window`。最后，处理后的数据可以推送到文件系统，数据库和实时仪表板。实际上，您可以在数据流上应用Spark的 [机器学习](http://spark.apache.org/docs/latest/ml-guide.html)和 [图形处理](http://spark.apache.org/docs/latest/graphx-programming-guide.html)算法。

![Spark Streaming](http://spark.apache.org/docs/latest/img/streaming-arch.png)

在内部，它的工作原理如下。Spark Streaming接收实时输入数据流并将数据分成批处理，然后由Spark引擎处理以批量生成最终结果流。

![Spark Streaming](http://spark.apache.org/docs/latest/img/streaming-flow.png)

Spark Streaming提供称为*离散流*或*DStream*的高级抽象，表示连续的数据流。DStream可以从来自Kafka，Flume和Kinesis等源的输入数据流创建，也可以通过在其他DStream上应用高级操作来创建。在内部，DStream表示为一系列 [RDD](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.rdd.RDD)。

本指南向您展示如何使用DStreams开始编写Spark Streaming程序。您可以使用Scala，Java或Python编写Spark Streaming程序（在Spark 1.2中引入），所有这些都在本指南中介绍。您可以在本指南中找到标签，让您在不同语言的代码段之间进行选择。

**注意：**有一些API在Python中不同或不可用。在本指南中，您将找到标记**Python API，**突出显示这些差异。