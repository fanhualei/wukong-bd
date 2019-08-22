package wukong.scala

object UseFor extends App{
  val filesHere = (new java.io.File(".")).listFiles
  for( file <-filesHere)
    println(file)


  //如果有必要的话，你可以使用多个过滤器，只要添加多个 if 语句即可
  for( file <-filesHere
       if file.isFile
       if file.getName.endsWith(".scala")
       )  println(file)

}
