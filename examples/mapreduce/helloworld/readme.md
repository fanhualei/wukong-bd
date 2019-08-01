# 执行方法

## 如何在idea中执行内

有两个类都有main函数,可以分别在idea中执行,也可以通过 ExampleDriver来执行

* ExampleDriver.java
    * 这个是入口类,如果要执行,需要加上具体的类名,例如:wordcount input output
    * 这样做的好处是可以在这个文件中添加很多方法
* WordCount.java
    * 这个类也有main函数,可以单独执行,例如:input/a.txt output
    

> 每次执行前要将output给删除了.

## 如何打包

打包文件配置好了,唯一做的是,选择那个是主文件. 建议在实际过程中,可以使用ExampleDriver作为入口

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>wukong.WordCount</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
```