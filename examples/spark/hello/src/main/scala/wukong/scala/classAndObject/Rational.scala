package wukong.scala.classAndObject

/**
 * https://www.w3cschool.cn/scaladevelopmentguide/h3vr1jao.html
 * 类的定义方法,以及重构方法
 */

class Rational (n:Int, d:Int) {
  require(d!=0)
  override def toString = n + "/" +d
  // 添加成员变量
  val number =n
  val denom =d
  def add(that:Rational)  =
    new Rational(
      number * that.denom + that.number* denom,
      denom * that.denom
    )
}


object main extends App{
  val oneHalf=new Rational(1,2)
  print(oneHalf)

  val twoThirds=new Rational(2,3)

  println(oneHalf add twoThirds)
  println(oneHalf.number)


}