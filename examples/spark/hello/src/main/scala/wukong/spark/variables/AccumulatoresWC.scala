package wukong.spark.variables

import org.apache.spark.sql.SparkSession



/**
 * 累加器
 * Spark累加器及广播变量的探讨
 * https://www.jianshu.com/p/b9470347acf3
 */
object AccumulatoresWC {
  def main(args: Array[String]) {
    val spark = SparkSession.builder
      .appName("Simple Application")
      .master("local")
      .getOrCreate()

    val sc=spark.sparkContext

    //做了4个分区
    val rdd=sc.parallelize(Array(
      "hadoop,spark,hbase",
      "hive,spark,hbase",
      "hadoop,spark,hbase",
      "hadoop,spark,zoo",
      "hadoop,spark,zoo",
      "hadoop,spark,hbase",
      "hadoop,spark,hbase",
      "hadoop,spark,hbase",
      "hadoop,spark,hbase",
      "hadoop,spark,hbase",
      "hive,spark,hbase",
      "hadoop,spark,hbase",
      "hadoop,spark,hbase"
    ),4)

    val inputRecords=sc.accumulator(0,"input record size")
    val partitionNumbers=sc.accumulator(0,"partition")
    val outputRecorders=sc.accumulator(0,"outputRecorders")

    val result=rdd.flatMap(line=>{
      inputRecords+=1
      line.split(",")
    }).map((_,1)).reduceByKey(_+_)

    result.foreachPartition(iter=>{
      partitionNumbers+=1
      iter.foreach(data=>{
        outputRecorders+=1
        println(data)
      })
    })

    println("inputRecords:"+inputRecords)
    println("partitionNumbers:"+partitionNumbers)
    println("outputRecorders:"+outputRecorders)

    // 为了看 http://127.0.0.1:4040/ 这个界面
    Thread.sleep(10000000)

    spark.stop()
  }
}
