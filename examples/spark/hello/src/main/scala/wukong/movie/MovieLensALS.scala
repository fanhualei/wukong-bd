package wukong.movie

import java.io.File

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.util.Random

object MovieLensALS {
  val newUserId=0

  //1. Define a rating elicitation function 定义一个采集用户数据的函数
  def elicitateRating(movies:Seq[(Int,String)])={
    val prompt="Please rate the following moview(1-5(best) or 0 if not seen):"
    println(prompt)
    val ratings=movies.flatMap{x=>
      var rating:Option[Rating]=None
      var valid=false
      while(!valid){
        println(x._2+" :")
        try{
          val r=scala.io.StdIn.readInt()
          if (r>5 || r<0){
            println(prompt)
          }else{
            valid=true
            if(r>0){
              rating=Some(Rating(newUserId,x._1,r))
            }
          }
        }catch {
          case e:Exception=>println(prompt)
        }
      }
      rating match {
        case Some(r)=>Iterator(r)
        case None => Iterator.empty
      }
    }
    if (ratings.isEmpty){
      sys.error("No ratings provided!")
    } else {
      ratings
    }
  }

  //2. Define a RMSE computation function 定义一个均方根差（RMSE）计算函数
  def computeRmse(model:MatrixFactorizationModel,data:RDD[Rating])={
    val prediction = model.predict(data.map(x=>(x.user, x.product)))

    val predictionRdd=prediction.map(x=> ((x.user,x.product),x.rating))
    val dataRdd=data.map(x=> ((x.user,x.product),x.rating))

    val predDataJoined =predictionRdd.join(dataRdd).values
    new RegressionMetrics(predDataJoined).rootMeanSquaredError
  }


  //3. Main 主函数
  def main(args: Array[String]) {
    //3.1 Setup env
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    println("Hello MovieLensALS！");
    val spark = SparkSession
      .builder
      .appName("MovieLensALS")
      .getOrCreate()

    val sc =spark.sparkContext

    //3.2 Load ratings data and know your data 得到评分的集合ratings
    val movieLensHomeDir="input/ml-1m/"

    val ratings = sc.textFile(new File(movieLensHomeDir, "ratings.dat").toString).map {line =>
      val fields = line.split("::")
      //timestamp, user, product, rating
      (fields(3).toLong%10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
    ratings.take(5).foreach(println)
    /*
      (0,Rating(1,1193,5.0))
      (9,Rating(1,661,3.0))
      (8,Rating(1,914,3.0))
      (5,Rating(1,3408,4.0))
      (1,Rating(1,2355,5.0))
     */

    //得到movie的map，可以方便得到一个movie名字
    val movies = sc.textFile(new File(movieLensHomeDir, "movies.dat").toString).map {line =>
      val fields = line.split("::")
      //movieId, movieName
      (fields(0).toInt, fields(1))
    }.collectAsMap()

    val numRatings = ratings.count()
    val numUser = ratings.map(x=>x._2.user).distinct().count()
    val numMovie = ratings.map(_._2.product).distinct().count()

    println("Got "+numRatings+" ratings from "+numUser+" users on "+numMovie+" movies.")
    /*
    Got 1000209 ratings from 6040 users on 3706 movies.
     */

    //3.3 Elicitate personal rating　　收集一个人的评分

    //得到前50名电影的编号
    //得到product,然后得到每个prodcut的评价数，例如(productId,count). 变成数组，然后再找第二个进行倒序排序，然后到50个，然后只取到第一列的数值．
    val topMovies = ratings.map(_._2.product).countByValue().toSeq.sortBy(-_._2).take(50).map(_._1)
    topMovies.foreach(println)
    /*
    2858
    260
    1196
    1210
    480
    2028
    ......more
     */
    val random = new Random(0)
    val selectMovies = topMovies.filter(x=>random.nextDouble() < 0.2).map(x=>(x, movies(x)))
    selectMovies.foreach(println)
    println("selectMovies.size:"+selectMovies.size)
    /*
    (1198,Raiders of the Lost Ark (1981))
    (608,Fargo (1996))
    (2762,Sixth Sense, The (1999))
    (1197,Princess Bride, The (1987))
    (1240,Terminator, The (1984))
    (1,Toy Story (1995))
    (3578,Gladiator (2000))
    (541,Blade Runner (1982))
    (2987,Who Framed Roger Rabbit? (1988))
    (1193,One Flew Over the Cuckoo's Nest (1975))
    (1127,Abyss, The (1989))
    selectMovies.size:11
     */

    //随机选择几个电影，让用户进行评分
    val myRatings = elicitateRating(selectMovies)
    val myRatingsRDD = sc.parallelize(myRatings, 1)
    myRatingsRDD.foreach(println)
    /*
    Rating(0,1198,1.0)
    Rating(0,608,2.0)
    Rating(0,2762,3.0)
    Rating(0,1197,4.0)
    Rating(0,1240,5.0)
    Rating(0,1,3.0)
    Rating(0,3578,2.0)
    Rating(0,541,4.0)
    Rating(0,2987,5.0)
    Rating(0,1193,2.0)
    Rating(0,1127,4.0)
     */

    //3.4 Split data into train(60%), validation(20%) and test(20%) 分割训练集　验证集　与测试集
    val numPartitions = 10
    val trainSet = ratings.filter(x=>x._1<6).map(_._2).union(myRatingsRDD).repartition(numPartitions).persist()
    val validationSet = ratings.filter(x=>x._1>=6 && x._1<8).map(_._2).persist()
    val testSet = ratings.filter(x=>x._1>=8).map(_._2).persist()

    val numTrain = trainSet.count()
    val numValidation = validationSet.count()
    val numTest = testSet.count()
    println("Training data: "+numTrain+" Validation data: "+numValidation+" Test data: "+numTest)
    /*
    Training data: 602252 Validation data: 198919 Test data: 199049
     */
    //3.5 Train model and optimize model with validation set　训练模型，并测试模式
    //分别定义不同的参数，来进行训练　得到最小的validationRmse
    val numRanks = List(8, 12)
    val numIters = List(10, 20)
    val numLambdas = List(0.1, 10.0)
    var bestRmse = Double.MaxValue
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestRanks = -1
    var bestIters = 0
    var bestLambdas = -1.0
    for(rank <- numRanks; iter <- numIters; lambda <- numLambdas){
      val model = ALS.train(trainSet, rank, iter, lambda)
      val validationRmse = computeRmse(model, validationSet)
      println("RMSE(validation) = "+validationRmse+" with ranks="+rank+", iter="+iter+", Lambda="+lambda)

      if (validationRmse < bestRmse) {
        bestModel = Some(model)
        bestRmse = validationRmse
        bestIters = iter
        bestLambdas = lambda
        bestRanks = rank
      }
    }
    /*
    RMSE(validation) = 0.8785840189333293 with ranks=8, iter=10, Lambda=0.1
    RMSE(validation) = 3.756322766790857 with ranks=8, iter=10, Lambda=10.0
    RMSE(validation) = 0.8730837423589937 with ranks=8, iter=20, Lambda=0.1
    RMSE(validation) = 3.756322766790857 with ranks=8, iter=20, Lambda=10.0
    RMSE(validation) = 0.8799921149759179 with ranks=12, iter=10, Lambda=0.1
    RMSE(validation) = 3.756322766790857 with ranks=12, iter=10, Lambda=10.0
    RMSE(validation) = 0.8712966831675377 with ranks=12, iter=20, Lambda=0.1
    RMSE(validation) = 3.756322766790857 with ranks=12, iter=20, Lambda=10.0
     */

    //3.6 Evaluate model on test set　，根据测试集合进行评估
    val testRmse = computeRmse(bestModel.get, testSet)
    println("The best model was trained with rank="+bestRanks+", Iter="+bestIters+", Lambda="+bestLambdas+
      " and compute RMSE on test is "+testRmse)


    //3.7 Create a baseline and compare it with best model
    //得到训练集合与校验集合的评分的平均值
    val meanRating = trainSet.union(validationSet).map(_.rating).mean()
    //将这个平均值，提供给测试集合，并求出均方根差
    val bestlineRmse = new RegressionMetrics(testSet.map(x=>(x.rating, meanRating))).rootMeanSquaredError
    // 查看提高的百分比
    val improvement = (bestlineRmse - testRmse)/bestlineRmse*100
    println("The best model improves the baseline by "+"%1.2f".format(improvement)+"%.")
    /*
    The best model improves the baseline by 21.90%.
     */

    //3.8 Make a personal recommendation

    val moviesId = myRatings.map(_.product)
    //去掉我已经看过的电影
    val candidates = sc.parallelize(movies.keys.filter(!moviesId.contains(_)).toSeq)
    //使用最好的模型去得到评分最高的前50个电影
    val recommendations = bestModel.get
      .predict(candidates.map(x=>(0, x)))
      .sortBy(-_.rating)
      .take(10)

    var i = 0
    println("Movies recommended for you:")
    recommendations.foreach{ line=>
      println("%2d".format(i)+" :"+movies(line.product)+" rating:"+line.rating)
      i += 1
    }

    /*
     0 :Specials, The (2000) rating:4.519337799485645
     1 :Circus, The (1928) rating:4.483802534531158
     2 :Bewegte Mann, Der (1994) rating:4.418311579818459
     3 :Man of the Century (1999) rating:4.337557225340195
     4 :General, The (1927) rating:4.323859819653418
     5 :Harmonists, The (1997) rating:4.319755541722625
     6 :To Live (Huozhe) (1994) rating:4.302488123573801
     7 :Time of the Gypsies (Dom za vesanje) (1989) rating:4.302470216840211
     8 :Children of Heaven, The (Bacheha-Ye Aseman) (1997) rating:4.288424480028921
     9 :Love and Other Catastrophes (1996) rating:4.273705081205094
    10 :Nobody Loves Me (Keiner liebt mich) (1994) rating:4.239558596792669
     */
    spark.stop()



  }
}
