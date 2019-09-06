package wukong.ml.statistics

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.stat.{Correlation, Summarizer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

/**
 * 关联
 * 计算两个数据系列之间的相关性是统计学中的常见操作。
 * 在spark.ml 我们提供的灵活性来计算多个系列两两之间的相关性。
 * 支持的相关方法目前是Pearson和Spearman的相关性。
 * Correlation 使用指定的方法计算输入数据集的相关矩阵。输出将是一个DataFrame，它包含向量列的相关矩阵。
 */

object Correlation {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
    val spark = SparkSession
      .builder
      .appName("SummarizerExample")
      .getOrCreate()

    import spark.implicits._
    import Summarizer._

    val data = Seq(
      (Vectors.dense(2.0, 3.0, 5.0), 1.0),
      (Vectors.dense(4.0, 6.0, 7.0), 2.0)
    )
    import spark.implicits._
    val df = data.toDF("features", "weight")
    df.show()


    val (meanVal, varianceVal) = df.select(metrics("mean", "variance")
      .summary($"features", $"weight").as("summary"))
      .select("summary.mean", "summary.variance")
      .as[(Vector, Vector)].first()

    println(s"with weight: mean = ${meanVal}, variance = ${varianceVal}")

    val (meanVal2, varianceVal2) = df.select(mean($"features"), variance($"features"))
      .as[(Vector, Vector)].first()

    println(s"without weight: mean = ${meanVal2}, sum = ${varianceVal2}")

    spark.stop()
  }
}
