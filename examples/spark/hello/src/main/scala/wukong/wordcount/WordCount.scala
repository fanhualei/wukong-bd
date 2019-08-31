package wukong.wordcount

import org.apache.spark.sql.SparkSession

object WordCount {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
    val spark = SparkSession
      .builder
      .appName("出租车统计")
      .getOrCreate()

    val basePath = "input/wordcount/"

    val lines=spark.read.text(basePath+"a.txt")
    lines.show()
    import spark.implicits._
    val words = lines.as[String].flatMap(_.split(" "))
    words.show()
    val wordCounts = words.groupBy("value").count()
    wordCounts.show()

  }
}
