package wukong.movie

import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.types._
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}

case class Movie(movieId: Int, title: String, genres: Seq[String])

case class User(userId: Int, gender: String, age: Int, occupation: Int, zip: String)


object AlsExamples {

  def parseMovie(str: String): Movie = {
    val fields = str.split("::")
    assert(fields.size == 3)
    Movie(fields(0).toInt, fields(1).toString, Seq(fields(2)))
  }

  def parseUser(str: String): User = {
    val fields = str.split("::")
    assert(fields.size == 5)
    User(fields(0).toInt, fields(1).toString, fields(2).toInt, fields(3).toInt, fields(4).toString)
  }

  def parseRating(str: String): Rating = {
    val fields = str.split("::")
    assert(fields.size == 4)
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
    val movieDF = sc.textFile("input/ml-1m/movies.dat").map(parseMovie).toDF()
    val userDF = sc.textFile("input/ml-1m/users.dat").map(parseUser).toDF()
    ratingDF.printSchema()
    movieDF.printSchema()
    userDF.printSchema()
    ratingDF.createOrReplaceTempView("ratings")
    movieDF.createOrReplaceTempView("movies")
    userDF.createOrReplaceTempView("users")

    val sqlContext = spark.sqlContext


    //查询每个电影的最高评分，最低评分，以及评分的人数，并按照评分人数进行排序．同时要显示出电影的名称
    val ucntResult = sqlContext.sql(
      """select title,rmax,rmin,ucnt
        from
        (select product, max(rating) as rmax, min(rating) as rmin, count(distinct user) as ucnt
        from ratings
        group by product) ratingsCNT
        join movies on product=movieId
        order by ucnt desc""")
    ucntResult.show()

    /*
    +--------------------+----+----+----+
    |               title|rmax|rmin|ucnt|
    +--------------------+----+----+----+
    |American Beauty (...| 5.0| 1.0|3428|
    |Star Wars: Episod...| 5.0| 1.0|2991|
    |Star Wars: Episod...| 5.0| 1.0|2990|
    |Star Wars: Episod...| 5.0| 1.0|2883|
    |Jurassic Park (1993)| 5.0| 1.0|2672|
    |Saving Private Ry...| 5.0| 1.0|2653|
    |Terminator 2: Jud...| 5.0| 1.0|2649|
    |  Matrix, The (1999)| 5.0| 1.0|2590|
    |Back to the Futur...| 5.0| 1.0|2583|
    |Silence of the La...| 5.0| 1.0|2578|
    | Men in Black (1997)| 5.0| 1.0|2538|
    |Raiders of the Lo...| 5.0| 1.0|2514|
    |        Fargo (1996)| 5.0| 1.0|2513|
    |Sixth Sense, The ...| 5.0| 1.0|2459|
    |   Braveheart (1995)| 5.0| 1.0|2443|
    |Shakespeare in Lo...| 5.0| 1.0|2369|
    |Princess Bride, T...| 5.0| 1.0|2318|
    |Schindler's List ...| 5.0| 1.0|2304|
    |L.A. Confidential...| 5.0| 1.0|2288|
    |Groundhog Day (1993)| 5.0| 1.0|2278|
    +--------------------+----+----+----+
    */


    //查询出最活跃的前10名用户
    val mostActiveUser = sqlContext.sql(
      """select user, count(*) as cnt
        from ratings group by user order by cnt desc limit 10""")
    mostActiveUser.show()
    /*
    +----+----+
    |user| cnt|
    +----+----+
    |4169|2314|
    |1680|1850|
    |4277|1743|
    |1941|1595|
    |1181|1521|
    | 889|1518|
    |3618|1344|
    |2063|1323|
    |1150|1302|
    |1015|1286|
    +----+----+
     */

    //查询出4169 评分的电影
    val result4169 = sqlContext.sql(
      """select distinct title, rating
      from ratings join movies on movieId=product
      where user=4169 and rating>4""")
    result4169.show()

    /*
    +--------------------+------+
    |               title|rating|
    +--------------------+------+
    |Nikita (La Femme ...|   5.0|
    |All the King's Me...|   5.0|
    |Field of Dreams (...|   5.0|
    |  Richard III (1995)|   5.0|
    |Few Good Men, A (...|   5.0|
    |          Ran (1985)|   5.0|
    |     Rain Man (1988)|   5.0|
    |Wizard of Oz, The...|   5.0|
    |On Golden Pond (1...|   5.0|
    | Fitzcarraldo (1982)|   5.0|
    |  Chasing Amy (1997)|   5.0|
    |People vs. Larry ...|   5.0|
    |Maltese Falcon, T...|   5.0|
    |Groundhog Day (1993)|   5.0|
    |Miracle on 34th S...|   5.0|
    |        Dumbo (1941)|   5.0|
    |Everyone Says I L...|   5.0|
    |      Georgia (1995)|   5.0|
    |Innocents, The (1...|   5.0|
    |Secret of Roan In...|   5.0|
    +--------------------+------+
    only showing top 20 rows
     */

    //ALS
    val splits = ratingRDD.randomSplit(Array(0.8, 0.2), 0L)
    val trainingSet = splits(0).cache()
    val testSet = splits(1).cache()
    println("trainingSet count:" + trainingSet.count())
    println("testSet.count:" + testSet.count())

    val model = (new ALS().setRank(20).setIterations(10).run(trainingSet))

    //为４１６９推荐电影，可能会重复，另外每次的结果可能不一样．　建议将model保存起来，这样每次推加的可能一样
    val recomForTopUser = model.recommendProducts(4169, 5)
    recomForTopUser.map(rating => (rating.product, rating.rating)).foreach(println)
    /*
    (3092,5.6984129690941305)
    (715,5.5597603468182575)
    (1310,5.541362763120336)
    (670,5.514596130435564)
    (759,5.448494058675006)
     */

    // 显示出电影的名称，这样更友好点
    val recomForTopUserDF = sc.parallelize(recomForTopUser).toDF()
    recomForTopUserDF.createOrReplaceTempView("recommend")

    val resultRecommend = sqlContext.sql(
      """select  user,title,product, rating
      from recommend join movies on movieId=product
      order by rating desc """)
    resultRecommend.show()

    /*
    +----+--------------------+-------+------------------+
    |user|               title|product|            rating|
    +----+--------------------+-------+------------------+
    |4169|  Chushingura (1962)|   3092|5.6984129690941305|
    |4169|Horseman on the R...|    715|5.5597603468182575|
    |4169|        Hype! (1996)|   1310| 5.541362763120336|
    |4169|World of Apu, The...|    670| 5.514596130435564|
    |4169|Maya Lin: A Stron...|    759| 5.448494058675006|
    +----+--------------------+-------+------------------+
     */

    //进行测试
    val testUserProduct = testSet.map {
      case Rating(user, product, rating) => (user, product)
    }

    //使用model得到与测试数值，并且取前10个打印出来
    val testUserProductPredict = model.predict(testUserProduct)
    println(testUserProductPredict.take(10).mkString("\n"))
    /*
    Rating(4551,2439,4.347532036503664)
    Rating(4551,306,4.487923828540846)
    Rating(4551,2575,4.813875687809696)
    Rating(4551,3547,2.6970390755680618)
    Rating(4551,72,1.0330689255735295)
    Rating(4551,3730,4.8249022299530875)
    Rating(4551,3798,1.2980794626809193)
    Rating(4551,1844,5.405879933235911)
    Rating(4551,1411,3.802000155941365)
    Rating(4551,2330,4.918440580323034)
    */

    //做两个集合，让这两个集合有相同的key，然后可以比较
    val testSetPair = testSet.map {
      case Rating(user, product, rating) => ((user, product), rating)
    }
    val predictionsPair = testUserProductPredict.map {
      case Rating(user, product, rating) => ((user, product), rating)
    }

    val joinTestPredict = testSetPair.join(predictionsPair)
    println(joinTestPredict.take(10).mkString("\n"))
    /*
    ((233,1265),(4.0,3.98430178739076))
    ((952,2671),(4.0,3.7584176579214574))
    ((889,1132),(3.0,3.3626271548702977))
    ((3754,1135),(4.0,3.680518276916522))
    ((3462,1252),(3.0,3.969958218101568))
    ((1896,910),(5.0,4.375882060666348))
    ((409,2414),(4.0,4.013739332209311))
    ((2615,3005),(1.0,3.544301641891412))
    ((844,3176),(5.0,4.185310581666356))
    ((2010,3386),(3.0,3.1858408449193836))
     */

    //得到平均偏差值
    val mean = joinTestPredict.map {
      case ((user, product), (ratingT, ratingP)) =>
        val err = ratingT - ratingP
        Math.abs(err)
    }.mean()
    println("mean:"+mean)
    //FP,ratingT<=1, ratingP>=4
    //得到错误的估计值，用户真实的是1,但是预测是4以上
    val fp = joinTestPredict.filter {
      case ((user, product), (ratingT, ratingP)) =>
        (ratingT <= 1 & ratingP >= 4)
    }
    println("joinTestPredict.count:" + joinTestPredict.count())
    println("fp.count:" + fp.count())

    /*
    mean:0.7252771785887864
    joinTestPredict.count:200385
    fp.count:532
     */
    import org.apache.spark.mllib.evaluation._
    val ratingTP = joinTestPredict.map {
      case ((user, product), (ratingT, ratingP)) =>
        (ratingP, ratingT)
    }
    val evalutor = new RegressionMetrics(ratingTP)
    // 平均绝对误差meanAbsoluteError   平均方根误差rootMeanSquaredError
    println("evalutor.meanAbsoluteError:" + evalutor.meanAbsoluteError)
    println("evalutor.rootMeanSquaredError:" + evalutor.rootMeanSquaredError)
    /*
    evalutor.meanAbsoluteError:0.7239638710736644
    evalutor.rootMeanSquaredError:0.9437646164750814
     */

    spark.close()
  }


}
