package wukong.spark.topn

import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

object TopN {
  def main(args: Array[String]) {
    val logFile = "input/topn/topn.txt" // Should be some file on your system

    val spark = SparkSession.builder
      .appName("Simple Application")
      .master("local")
      .getOrCreate()
    val logData = spark.read.textFile(logFile).cache()


    //第一步：数据清理后的rdd
    val extractedRdd = logData.rdd.map(line => {
      val fileds = line.split(" ")
      val name = fileds(0)
      val value = fileds(1).toInt
      (name, value)
    }).cache()

    println("\n 输出全部数据===============================")
    extractedRdd.foreach(println)


    println("\n 输出前三名===============================")

    extractedRdd.groupByKey()
      .map({
        case (key, itr) => {
          (key, itr.toList.sorted.takeRight(3))
        }
      })
      .foreach(println)

    println("\n 输出前三名竖排===========正序====================")
    extractedRdd.groupByKey()
      .flatMap({
        case (key, itr) => {
          val values = itr.toList.sorted.takeRight(3)
          values.map(it => (key, it))
        }
      })
      .foreach(println)

    println("\n 输出前三名竖排===========sortWith(_>_)=====倒序===============")
    extractedRdd.groupByKey()
      .map(group => (group._1, group._2.toList.sortWith(_ > _).take(3)))
      .foreach(group => {
        group._2.map(it => println((group._1, it)))
      })


    println("\n 输出前三名竖排===========aggregateBykey====这个性能更高比groupby===========")
    extractedRdd.aggregateByKey(ListBuffer[Int]())((u, v) => {
      u += v
      u.sorted.takeRight(3)
    }, (u1, u2) => {
      u1 ++= u2
      u1.sorted.takeRight(3)
    }).foreach(println)


    println("\n ====foreachPartition是将一个分区中的数据全部读取打印，有可能内存泄漏．mapPartition一样")
    extractedRdd.foreachPartition(f=>{
      f.foreach(println)
    })

    spark.stop()
  }
}
