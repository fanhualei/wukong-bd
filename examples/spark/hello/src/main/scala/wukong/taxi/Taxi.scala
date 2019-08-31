package wukong.taxi

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.types.{DoubleType, LongType, StringType, StructField, StructType}


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

    val sc = spark.sparkContext
    //    val sqlContext = spark.sqlContext
    val basePath = "input/taxi/"

    //如果定义的类型有问题，那么在进行转换的时候，数据都显示null
    val schema = StructType(Array(
      StructField("tid", LongType),
      StructField("lat", DoubleType),
      StructField("lon", DoubleType),
      StructField("time", StringType)
    ))

    val df = spark.read
      .format("csv")
      .option("header", "false")
      .schema(schema)
      .load(basePath + "taxi.csv")

    println(df.count())
    df.show(3)

    //    import spark.implicits._
    df.select("tid", "lat").show(3)
    df.printSchema()

    df.createOrReplaceTempView("tmp_taxi")



    //整理数据
    spark.sql(
      """
        |SELECT tid,
        |SUBSTRING(time,0,2) as hour
        |FROM tmp_taxi
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour")



    //计算各个小时出租车的载客次数
    spark.sql(
      """
        |SELECT tid,hour,COUNT(1) as count
        |FROM tmp_id_hour
        |GROUP BY tid,hour
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_count")

    // 按照每个时间窗口，进行排序
    spark.sql(
      """
        |SELECT tid,hour,count,
        |ROW_NUMBER() OVER (PARTITION BY hour ORDER BY count DESC) as rnk
        |FROM tmp_id_hour_count
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_rnk")

    spark.sql(
      """
        |SELECT tid,hour,count,rnk
        |FROM tmp_id_hour_rnk
        |WHERE rnk<=5
        |ORDER BY hour,rnk
        |""".stripMargin).createOrReplaceTempView("tmp_id_hour_rnk_top")


    val resultDataFrame = spark.sql("select * from  tmp_id_hour_rnk_top")
    resultDataFrame.show(1000)



    // 用来保存数据
    //    resultDataFrame.write
    //      .format("csv")
    //      .option("header","true")
    //      .partitionBy("hour")
    //      .mode(SaveMode.Overwrite)
    //      .save("output/taxi/taxi.csv")


  }
}
