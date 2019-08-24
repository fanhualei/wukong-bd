package wukong.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
 * 读取本地的一个文件,并且计算文件内容
 */
object Spark {

  def main(args: Array[String]): Unit = {

    val config = new SparkConf()
      .setAppName("WordCount")
      //在提交yarn的情况下要注释掉这行程序
      //.setMaster("local")

    val sc = new SparkContext(config)
    val filePath = "hdfs://127.0.0.1:9000/user/fan/input/a.txt"

    val fileRdd=sc.textFile(filePath)
    fileRdd.collect().foreach(println)

    val numAs = fileRdd.filter(line => line.contains("a")).count()
    val numBs = fileRdd.filter(line => line.contains("b")).count()

    println(s"Lines with a: $numAs, Lines with b: $numBs")

    println("\nok===================================\n")

  }

}
