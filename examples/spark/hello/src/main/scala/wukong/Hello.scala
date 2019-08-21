package wukong

object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello World！");

    var myList = Array(1.9, 2.9, 3.4, 3.5)
    for ( x <- myList ) {
      println( x )
    }

  }
}
