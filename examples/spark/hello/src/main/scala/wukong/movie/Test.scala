package wukong.movie
import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.types._
import org.apache.spark.mllib.recommendation.{ALS,MatrixFactorizationModel,Rating}



object Test{


  def parseRating(str: String): Rating = {
    val fields=str.split("::")
    assert(fields.size==4)
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toInt)
  }


  def main(args: Array[String]): Unit = {
    println("Hello World！");
    val spark = SparkSession
      .builder
      .appName("AlsExamples")
      .getOrCreate()

    import spark.implicits._

    val sc = spark.sparkContext
    val ratingText = sc.textFile("input/ml-1m/ratings.dat")
    println(ratingText.first())
    val ratingRDD = ratingText.map(parseRating).cache()
    println("Total number of ratings: " + ratingRDD.count())
    println("Total number of movies rated: " + ratingRDD.map(_.product).distinct().count())
    println("Total number of users who rated movies: " + ratingRDD.map(_.user).distinct().count())

    //Create DataFrames
    val ratingDF = ratingRDD.toDF();
    ratingDF.printSchema()
    ratingDF.createOrReplaceTempView("ratings")

    //_.user==4169
    val set=Set(2493,668,1180,670,3462)
    ratingRDD.filter(rating=>{
      if(rating.user==4169 && set.contains(rating.product)){
        true
      }else{
        false
      }
    }).foreach(println)

  }
}
