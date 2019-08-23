package wukong.spark.nginx

import java.sql.DriverManager

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

    println("\n下面输出清理后的数据========================")
    mapedRdd.foreach(println)


    println("\n下面统计每小时的PV======reduceByKey==================")

    //educeByKey,在本机suffle后,再发送一个总map，发送到一个总机器上suffle汇总map，（汇总要压力小）
    mapedRdd.map(t=>(t._1.substring(0,13),1)).reduceByKey(_ + _).sortByKey().foreach(println)

    //groupByKey,发送本机所有的map,在一个机器上suffle汇总map（汇总压力大）
    println("\n下面统计每小时的PV======groupByKey==================")
    val pvRdd=mapedRdd.map(t=>(t._1.substring(0,13),1)).groupByKey().map(t=>(t._1,t._2.sum)).sortByKey()
    pvRdd.foreach(println)

    println("\n下面统计每小时的UV======distinct去重==================")
    val uvRdd=mapedRdd.map(t=>(t._1.substring(0,13)+" "+t._2,1))
      .distinct()
      .map(t=>(t._1.substring(0,13),t._2))
      .reduceByKey(_ + _)
      .sortByKey()
    uvRdd.foreach(println)


    println("\n下面统计每小时的UV======reduceByKey去重2==================")
    mapedRdd.map(t=>(t._1.substring(0,13)+" "+t._2,1))
      .reduceByKey(
        (a,b)=>a
      ).map(
        t=>(t._1.substring(0,13),1)
      )
      .reduceByKey(
        _+_
      )
      .sortByKey().foreach(println)



    println("\n下面统计每小时的UV======groupbykey去重==================")

    mapedRdd.map(t=>(t._1.substring(0,13),t._2))
        .groupByKey()
        .map(t=>{
          val date =t._1
          val iPs=t._2
          // groupByKey会返回一个集合,这里根据toSet的去重特性,去掉那些重复的内容
          val uv=iPs.toSet.size
          (date,uv)
        })
        .sortByKey()
        .foreach(println)


    println("\n统计访问最多的前5名用户========================")
    mapedRdd.map(t=>(t._2,1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)
      .take(5)
      .foreach(println)

    println("\n统计访问最多的前5个页面========================")
    mapedRdd.map(t=>(t._3,1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)
      .take(5)
      .foreach(println)


    println("\n uv pv 结合在一起 ==========fullOuterJoin==============")

    val pvuvRdd= pvRdd.fullOuterJoin(uvRdd)
      .map(t=>{
        val date=t._1
        val pv=t._2._1.getOrElse(0)
        val uv=t._2._2.getOrElse(0)
        (date,pv,uv)
      })

    pvuvRdd.foreach(println)

    //保存到output
    pvuvRdd.saveAsTextFile("output/pvuv")

    //保存到mysql
    val con=DriverManager.getConnection("url","useranme","pw")
    val sql="insert into tablename values(?,?,?) "
    val pstmt =con.prepareStatement(sql);
    pvuvRdd.foreachPartition(iter=>{
      iter.foreach(t=>{
        val date =t._1;
        val pv=t._2
        val uv=t._3
        pstmt.setString(1,date)
        pstmt.setInt(2,pv)
        pstmt.setInt(3,uv)
        pstmt.executeUpdate()
      })
    })
    pstmt.close()
    con.close()


    // 为了看 http://127.0.0.1:4040/ 这个界面
    //Thread.sleep(10000)



  }
}
