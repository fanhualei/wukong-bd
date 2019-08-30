package wukong.luck

import org.apache.spark.sql.SparkSession
import java.math.BigDecimal

/**
 * SparkSQL中的UDF相当于是1进1出，UDAF相当于是多进一出，类似于聚合函数。
 * 开窗函数一般分组取topn时常用。
 * https://yq.aliyun.com/articles/680259
 */

object UDFAndUDAF {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
    val spark = SparkSession
      .builder
      .appName("Spark Pi")
      .getOrCreate()

    val sc=spark.sparkContext;
    val basePath="input/luck/"


    val lines = sc.textFile(basePath+"people.txt")
    val lineLengths = lines.map(s => s.length)
    val totalLength = lineLengths.reduce((a, b) => a + b)
    println(totalLength);


    /**
     * UDF UDAF UDTF
     * 行转列　列转行
     * 1 a
     * 1 b   1 a,b,d
     * 2 d   2 d
     * 1 c
     *
     */
    val sqlc=spark.sqlContext;

    //自定义UDF
    sqlc.udf.register("format_double",(value:Double)=>{
      val bd=new BigDecimal(value)
      bd.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()
    })

    sqlc.udf.register("format_double_num",(value:Double,num:Int)=>{
      val bd=new BigDecimal(value)
      bd.setScale(num,BigDecimal.ROUND_HALF_UP).doubleValue()
    })

    sqlc.udf.register("my_avg",Avg_UDAF)

    import spark.implicits._
    sc.parallelize(Array(
      (1,4),
      (2,2),
      (1,1),
      (1,2),
      (3,4)
    )).toDF("dept","salary").createOrReplaceTempView("temp")

    //
    sqlc.sql(
      """
        |select dept,
        |avg(salary) as avg,
        |format_double(avg(salary)) as avg_format,
        |format_double_num(avg(salary),7) as avg_format_2,
        |format_double_num(my_avg(salary),7) as avg_format_3
        |from temp
        |group by dept
        |""".stripMargin
    ).show(20,false)



  }
}
