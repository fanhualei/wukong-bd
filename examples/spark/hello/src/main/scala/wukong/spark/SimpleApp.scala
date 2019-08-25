package wukong.spark

import org.apache.spark.sql.SparkSession

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "input/a.txt" // Should be some file on your system
    val spark = SparkSession.builder
      .appName("Simple Application")
      .getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    logData.collect().foreach(println)
    val numAs = logData.filter(line => line.contains("c")).count()
    val numBs = logData.filter(line => line.contains("d")).count()
    println(s"\nLines with c: $numAs, Lines with d: $numBs")
    spark.stop()
  }
}
