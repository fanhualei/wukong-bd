package wukong.scala

/**
 *
 * https://www.w3cschool.cn/scaladevelopmentguide/i3jy1jaa.html
 *
 *
 *
 */
object NextStep {

  /**
   * 第七步：使用类型参数化数组
   */
  def useArray(): Unit ={
    val greeStrings=new Array[String](3)
    greeStrings(0)="Hello"
    greeStrings(1)=","
    greeStrings(2)="world!\n"
    for(i<-0 to 2){
      print(greeStrings(i))
    }
  }

  /**
   * 第八步: 使用Lists
   * 前面介绍的数组，它的元素是可以被修改的。如果需要使用不可以修改的序列，Scala 中提供了 Lists 类。
   * 和 Java 的 List 不同，Scala 的 Lists 对象是不可修改的。
   * 它被设计用来满足函数编程风格的代码。
   * 它有点像 Java 的 String，String 也是不可以修改的，如果需要可以修改的 String 对像，可以使用 StringBuilder 类。
   */
  def useList(): Unit ={
    val oneTwo = List(1,2)
    val threeFour = List(3,4)
    val oneTwoThreeFour=oneTwo:::threeFour
    println (oneTwo + " and " + threeFour + " were not mutated.")
    println ("Thus, " + oneTwoThreeFour + " is a new list")

    //::方法（操作符）是右操作符
    val oneTowThree = 1 :: 2 ::3 :: Nil
    // 左操作
    val oneTowThree2 =  Nil.::(3).::(2).::(1)
    println(oneTowThree)
    println(oneTowThree2)
  }


  /**
   * 第九步：使用元组（ Tuples )
   *
   */

    def useTuples(): Unit ={
      //目前 Scala 支持的元祖的最大长度为 22
      val pair=(99,"Luftballons")
      //一旦定义了一个元组，可以使用 ._和索引来访问员组的元素（矢量的分量，注意和数组不同的是，元组的索引从 1 开始）。
      println(pair._1)
      println(pair._2)
    }

  /**
   * 第十步： 使用 Sets 和 Maps
   * 因此它提供的集合类分成了可以修改的集合类和不可以修改的集合类两大类型。
   * 比如 Array 总是可以修改内容的，而 List 总是不可以修改内容的。
   *
   */
  def useSetAndMap(): Unit ={
    var jetSet = Set ("Boeing","Airbus")
    jetSet +="Lear"
    println(jetSet.contains("Cessna"))


    jetSet.foreach(println)

    val romanNumeral = Map ( 1 -> "I" , 2 -> "II",
      3 -> "III", 4 -> "IV", 5 -> "V")


    println (romanNumeral(4))


  }

  /**
   * 第十一步： 学习识别函数编程风格
   * 首先在看代码上要认识哪种是指令编程，哪种是函数式编程。
   * 一个简单的原则，如果代码中含有 var 类型的变量，这段代码就是传统的指令式编程，
   * 如果代码只有 val 变量，这段代码就很有可能是函数式代码，因此学会函数式编程关键是不使用 vars 来编写代码。
   * https://www.w3cschool.cn/scaladevelopmentguide/i3jy1jaa.html
   */


  def useFun(){
    val args=Array("hello",",","world!\n")


    printArgsForJava(args)
    printArgsForScala1(args)
    printArgsForScala2(args)
  }

  def printArgsForJava ( args: Array[String]) : Unit ={
    //来自 Java 背景的程序员开始写 Scala 代码很有可能写成上面的实现。
    var i=0
    while (i < args.length) {
      println (args(i))
      i+=1
    }
  }

  def printArgsForScala1 ( args: Array[String]) : Unit ={

    for( arg <- args)
      println(arg)
  }

  def printArgsForScala2 ( args: Array[String]) : Unit ={
    // 这个例子也说明了尽量少用 vars 的好处，代码更简洁和明了，从而也可以减少错误的发生。
    args.foreach(println)
  }


  def readFile(): Unit ={
    import scala.io.Source
    val fileName="/opt/modules/apache/spark-2.4.3-bin-hadoop2.7/README.md"
    var i=0
    for(line<-Source.fromFile(fileName).getLines()){
      i=i+1
      println(i+" "+line.length+" "+line)

    }

  }





  def main(args: Array[String]): Unit = {
    useArray()
    useList
    useTuples
    useSetAndMap
    useFun
    readFile
  }


}
