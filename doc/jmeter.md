# 压力测试



# 1. 工具选型

下面这两篇文章写的挺好的。

* [性能测试工具 wrk,ab,locust,Jmeter 压测结果比较](https://testerhome.com/topics/17068)

* [压测工具如何选择? ab、locust、Jmeter、go压测工具【单台机器100w连接压测实战】](http://www.imooc.com/article/291715)



当选选择`jmeter`,因为：

* 支持集群压力测试。听说不容易上手。
* 支持多种目标：Http 数据库  Mqtt  WebSocket
* 有图形界面，报表好看。
* 缺点：速度慢



# 2. 安装



官网：http://jmeter.apache.org/



## ① 下载 jmeter

http://jmeter.apache.org/download_jmeter.cgi

Apache JMeter 5.1.1



## ② 下载JDK

Apache JMeter 5.1.1 (Requires Java 8+)

* [红帽子下载](https://developers.redhat.com/products/openjdk/download)

* 注册了一个账户：fanhualei
* 下载OpenJdk11 



## ③ 安装

[OpenJDK windows下安装过程](https://blog.csdn.net/jianzero/article/details/98483083)

1. 设置JAVA_HOME，指向openJDK目录

2. 新建系统变量CLASS_PATH，并设置为 %Java_Home%\bin;%Java_Home%\lib\dt.jar;%Java_Home%\lib\tools.jar;

3. 编辑系统变量Path，添加%JAVA_HOME%\bin 和 %JAVA_HOME%\jre\bin

4. 查看版本java -version



## ④ 运行Jmeter

 执行这个程序： /bin/jmeter.bat

或者新建一个`start.vbs` 文件 ，这样就不显示那个讨厌的dos框了。

```shell
set ws=WScript.CreateObject("WScript.Shell")
ws.Run "D:\jmeter\apache-jmeter-5.1.1\bin\jmeter.bat",0
```



## ⑤ 设置成中文

点击菜单栏【Options】按钮 -> 然后依次单击【Choose language】>【Chinese(simplified)】

[jmeter如何设置语言为中文](https://jingyan.baidu.com/article/b0b63dbf25733b4a4830708f.html)



# 3. 入门实践

[参考这个文档](https://blog.methodname.com/jmeterru-men-shi-jian/)







①②③④⑤⑥⑦⑧⑨







