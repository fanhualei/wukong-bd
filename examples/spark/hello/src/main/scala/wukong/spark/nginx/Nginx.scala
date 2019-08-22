package wukong.spark.nginx

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 统计nginx的日志情况
 * https://blog.csdn.net/wwwdc1012/article/details/85042315
 * https://blog.csdn.net/u010886217/article/details/83353050
 */
object Nginx {

  def main(args: Array[String]): Unit = {
    val config = new SparkConf()
      .setAppName("nginx-log")
      .setMaster("local")
    val sc = new SparkContext(config)

    val logFile = "/home/fan/001-db/wukong-bd/examples/spark/hello/input/nginx/access.log"

    val fileRdd: RDD[String] = sc.textFile(logFile)


    var mapedRdd=  fileRdd.map(line=>{
      val log = NginxLogParser.parseLog2Line(line);
      (log.getNewTime,log.ip,log.getPage())
    })

    //因为缓存不是立即操作的api，只有当调用了这块缓存的数据才会cache
    mapedRdd.cache()

    println("下面输出清理后的数据========================")
    mapedRdd.foreach(println)

    println("下面统计每小时的UV======reduceByKey==================")

    //educeByKey,在本机suffle后,再发送一个总map，发送到一个总机器上suffle汇总map，（汇总要压力小）
    mapedRdd.map(t=>(t._1.substring(0,13),1)).reduceByKey(_ + _).sortByKey().foreach(println)

    //groupByKey,发送本机所有的map,在一个机器上suffle汇总map（汇总压力大）
    println("下面统计每小时的UV======groupByKey==================")
    mapedRdd.map(t=>(t._1.substring(0,13),1)).groupByKey().map(t=>(t._1,t._2.sum)).sortByKey().foreach(println)

    println("下面统计每小时的PV======distinct去重==================")
    mapedRdd.map(t=>(t._1.substring(0,13)+" "+t._2,1))
      .distinct()
      .map(t=>(t._1.substring(0,13),t._2))
      .reduceByKey(_ + _)
      .sortByKey().foreach(println)

    println("统计访问最多的前5名用户========================")
    mapedRdd.map(t=>(t._2,1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)
      .take(5)
      .foreach(println)

    println("统计访问最多的前5个页面========================")
    mapedRdd.map(t=>(t._3,1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)
      .take(5)
      .foreach(println)

  }
}
