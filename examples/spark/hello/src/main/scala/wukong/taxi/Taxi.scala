package wukong.taxi

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 出租车信息统计
 * https://blog.csdn.net/Enche/article/details/79803321
 */
object Taxi {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
    val spark = SparkSession
      .builder
      .appName("出租车统计")
      .getOrCreate()

    val sc=spark.sparkContext
    val sqlContext=spark.sqlContext
    val basePath="input/taxi/"

    val schema=StructType(Array(
      StructField("tid",LongType),
      StructField("lat",LongType),
      StructField("lon",LongType),
      StructField("time",StringType)
    ))

    sqlContext.read
      .format("csv")
      .option("header","false")
      .schema(schema)
      .load(basePath+"taxi.csv")
      .createOrReplaceTempView("tmp_taxi")


    //整理数据
    sqlContext.sql(
      """
        |SELECT tid,
        |SUBSTRING(time,0,2) as hour
        |FROM tmp_taxi
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour")

    //计算各个小时出租车的载客次数
    sqlContext.sql(
      """
        |SELECT tid,hour,COUNT(1) as count
        |FROM tmp_id_hour
        |GROUP BY tid,hour
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_count")

    // 按照每个时间窗口，进行排序
    sqlContext.sql(
      """
        |SELECT tid,hour,count,
        |ROW_NUMBER() OVER (PARTITION BY hour ORDER BY count DESC) as rnk
        |FROM tmp_id_hour_count
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_rnk")

    sqlContext.sql(
      """
        |SELECT tid,hour,rnk
        |FROM tmp_id_hour_rnk
        |WHERE rnk<=5
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_rnk_top")


    val  resultDataFrame=  sqlContext.sql("select * from tmp_id_hour_rnk_top ")
    resultDataFrame.show()

    resultDataFrame.write
      .format("csv")
      .option("header","true")
      .partitionBy("hour")
      .mode(SaveMode.Overwrite)
      .save("output/taxi/taxi.csv")


  }
}
