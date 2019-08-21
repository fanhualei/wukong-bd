package wukong.scala.classAndObject

/**
 * https://www.w3cschool.cn/scaladevelopmentguide/86ce1jag.html
 */


import scala.collection.mutable.Map
class ChecksumAccumulator{
  private var sum=0
  def add(b:Byte) :Unit = sum +=b
  def checksum() : Int = ~ (sum & 0xFF) +1
}

object ChecksumAccumulator {
  private val cache = Map [String, Int] ()
  def calculate(s:String) : Int =
    if(cache.contains(s))
      cache(s)
    else {
      val acc=new ChecksumAccumulator
      for( c <- s)
        acc.add(c.toByte)
      val cs=acc.checksum()
      cache += ( s -> cs)
      cs
    }
}

object HelloWorld extends App{
  println("Hello, world!")
  val ren=ChecksumAccumulator.calculate("dddddd")
  print(ren)
}