package wukong.spark.variables

/**
 * 自定义一个累加器，可以避免shuffer的一些操作，替代reduceByKey等操作
 */


import org.apache.spark.sql.SparkSession
import org.apache.spark.util.AccumulatorV2

class  MyAccumulator extends AccumulatorV2[String,Int]{
  override def isZero: Boolean = ???

  override def copy(): AccumulatorV2[String, Int] = ???

  override def reset(): Unit = ???

  override def add(v: String): Unit = ???

  override def merge(other: AccumulatorV2[String, Int]): Unit = ???

  override def value: Int = ???
}


object MyAccumulatoresWC {

}
