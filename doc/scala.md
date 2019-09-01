# scala

可以快速将语法过一边，不用基于细节，在今后的操作过程中，再做处理。



将scala的函数部分重点看一下。



scala的六大特点：

1).Java和scala可以无缝混编，都是运行在JVM上的
2).类型推测(自动推测类型)，不用指定类型  
3).并发和分布式（Actor，类似Java多线程Thread） 
4).特质trait，特征(类似java中interfaces 和 abstract结合)
5).模式匹配，match case（类似java switch case）
6).高阶函数（函数的参数是函数，函数的返回是函数），可进行函数式编程







##  1. 环境配置

### 1.1. 简单安装

从 Scala 官网地址 http://www.scala-lang.org/downloads 下载 Scala 二进制包

然后解压到`/opt/modules/scala-2.13.0`

```shell
$ tar -xzvf  /media/sf_share/scala-2.13.0.tgz
$ vi /etc/profile
$ source /etc/profile
$ scala -version

```



`/etc/profile`中追加的内容

```
# SCALA
export SCALA_HOME=/opt/modules/scala-2.13.0
export PATH=$PATH:${SCALA_HOME}/bin
```





### 1.2. IDEA环境配置

[官方说明](https://docs.scala-lang.org/getting-started/intellij-track/getting-started-with-scala-in-intellij.html)

通过maven来创建项目，选择模板`scala-achetype-guice`





##  2. 基本语法



### 2.1. 变量

在 Scala 中，使用关键词 **"var"** 声明变量，使用关键词 **"val"** 声明常量。

与Java不同的可以创建Class与Object

Unit等于void，表示返回值为空或者没有返回值

Java中的基本类型都是小写，Scala中都是大写

java所有类的父类是Object，Scala的父类是Any,Any也是AnyVal的父类



### 2.2. 方法与函数

Scala 中的方法跟 Java 的类似，方法是组成类的一部分。

Scala 中的函数则是一个完整的对象，Scala 中的函数其实就是继承了 Trait 的类的对象。

Scala 中使用 **val** 语句可以定义函数，**def** 语句定义方法。



Scala 中定义匿名函数的语法很简单，箭头左边是参数列表，右边是函数体。

使用匿名函数后，我们的代码变得更简洁了。

下面的表达式就定义了一个接受一个Int类型输入参数的匿名函数:

```
var inc = (x:Int) => x+1
```





高阶函数（Higher-Order Function）就是操作其他函数的函数。

Scala 中允许使用高阶函数, 高阶函数可以使用其他函数作为参数，或者使用函数作为输出结果。

以下实例中，apply() 函数使用了另外一个函数 f 和 值 v 作为参数，而函数 f 又调用了参数 v：

```
object Test {
   def main(args: Array[String]) {

      println( apply( layout, 10) )

   }
   // 函数 f 和 值 v 作为参数，而函数 f 又调用了参数 v
   def apply(f: Int => String, v: Int) = f(v)

   def layout[A](x: A) = "[" + x.toString() + "]"
   
}
```



### 2.3. 数组与集合





### 2.4 伴生类

主要为了封装Object内部中的私有变量。

 





## 参考文档



* [菜鸟Scala 教程](https://www.runoob.com/scala/scala-tutorial.html)
* [w3c Scale教程](https://www.w3cschool.cn/scaladevelopmentguide/)
* [官网说明](https://www.scala-lang.org/)
* 

