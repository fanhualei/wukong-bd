# spark 例子



##  1. hello

撰写一个简单的spark代码



创建一个新的scala功能, 网上有很多教程,但是错误很多. 例如要通过maven来生成等,结果模板很旧.

我的具体步骤如下:

> 前提条件

idea中安装`scala`插件



### 1.1. 引入scala



#### 1.1.1. 生成java工程

使用maven生成一个空的java工程

![alt](doc/imgs/new-project-step1.png)





#### 1.1.2. 编写scala代码

* 在main下建立一个scala的目录
* 生成一个scala的object



```scala
package wukong

object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello World！");
  }
}
```



![alt](doc/imgs/new-project-code.png)





#### 1.1.3. 运行程序

右键`Hello.scala`运行,然后显示hello world



#### 1.1.4. 添加Maven

不添加也行,主要是为了跟踪,看到scala的代码

```xml
    <properties>
        <scala.version>2.13.0</scala.version>
    </properties>

    <dependencies>
        <!-- 导入scala的依赖 -->
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```



### 1.2. 引入spark

