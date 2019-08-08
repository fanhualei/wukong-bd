# Sqoop使用



[TOC]





## 简介

sqoop是数据交换的工具，底层使用MapReduce工具。



* 功能
  * 关系数据库 ---> hdfs
  * hdfs ---> 关系数据库
* 优势
* 劣势
* 应用场景
* 可替代工具
  * spark





## 安装



### 安装

选择了sqoop1,因为官网说sqoop2不建议用在生产服务上。[sqoop1使用手册](http://sqoop.apache.org/docs/1.4.7/index.html)

```
Latest stable release is 1.4.7 (download, documentation). Latest cut of Sqoop2 is 1.99.7 (download, documentation). Note that 1.99.7 is not compatible with 1.4.7 and not feature complete, it is not intended for production deployment.
```

#### 下载并解压

`sqoop-1.4.7.bin__hadoop-2.6.0.tar.gz`

```shell
$ cd /opt/modules/apache/
$ tar -zxvf /media/sf_share/sqoop-1.4.7.bin__hadoop-2.6.0.tar.gz  -C ./
$ mv sqoop-1.4.7.bin__hadoop-2.6.0/ sqoop-1.4.7
$ cd sqoop-1.4.7/conf
$ cp sqoop-env-template.sh sqoop-env.sh
```



#### 修改sqoop-env.sh

```
export HADOOP_HOME=/opt/modules/apache/hadoop-2.9.2
export HIVE_HOME=/opt/modules/apache/hive-2.3.5
```



#### 复制mysql.jar

mysql 驱动包到 sqoop1.4.6/lib 目录下

```shell
cp /media/sf_share/mysql-connector-java-5.1.48.jar ./lib
```



#### 修改系统参数

```
vi ~/.bashrc
source ~/.bashrc

```

.bashrc的内容

```
#Sqoop
export SQOOP_HOME=/opt/modules/apache/sqoop-1.4.7
export PATH=$PATH:$SQOOP_HOME/bin
```





### 测试

#### 进入mysql

```shell
 $ mysql -uroot -proot@mysql
 $ mysql> create database sqoop_test;
```



```shell
$ bin/sqoop -version

```

#### 显示数据库

```shell
sqoop list-databases \
 --connect jdbc:mysql://127.0.0.1:3306/ \
 --username root \
 --password root@mysql
```



#### 显示表

```shell
sqoop list-tables \
 --connect jdbc:mysql://127.0.0.1:3306/sqoop_test \
 --username root \
 --password root@mysql
```



## 使用



### 导入到HDFS



####  普通用法	

```shell
sqoop import   \
--connect jdbc:mysql://hadoop1:3306/mysql   \
--username root  \
--password root   \
--table mysqlTableName   \
-m 1
```

保存在`/user/root/mysqlTableName`目录下





#### 指定分隔符和导入路径

```shell
sqoop import   \
--connect jdbc:mysql://hadoop1:3306/mysql   \
--username root  \
--password root   \
--table mysqlTableName   \
--target-dir /user/hadoop11/my_help_keyword1  \
--fields-terminated-by '\t'  \
-m 2
```



#### 带where条件

```shell
sqoop import   \
--connect jdbc:mysql://hadoop1:3306/mysql   \
--username root  \
--password root   \
--where "name='STRING' " \
--table help_keyword   \
--target-dir /sqoop/hadoop11/myoutport1  \
-m 1
```



#### 查询指定列

```shell
sqoop import   \
--connect jdbc:mysql://hadoop1:3306/mysql   \
--usernam"e root  \
--password root   \
--columns "name" \
--where "name='STRING' " \
--table help_keyword  \
--target-dir /sqoop/hadoop11/myoutport22  \
-m 1
```



#### 指定自定义查询SQL

```shell
sqoop import   \
--connect jdbc:mysql://hadoop1:3306/  \
--username root  \
--password root   \
--target-dir /user/hadoop/myimport33_1  \
--query 'select help_keyword_id,name from mysql.help_keyword where $CONDITIONS and name = "STRING"' \
--split-by  help_keyword_id \
--fields-terminated-by '\t'  \
-m 4
```

在以上需要按照自定义SQL语句导出数据到HDFS的情况下：
1、引号问题，要么外层使用单引号，内层使用双引号，$CONDITIONS的$符号不用转义， 要么外层使用双引号，那么内层使用单引号，然后$CONDITIONS的$符号需要转义
2、自定义的SQL语句中必须带有WHERE \$CONDITIONS



#### 在原先的数据基础上进行追加

* --check-column
* --last-value

希望自动读取最大值或者保存最大值

* 执行sqoop时，将输出内容写入到一个文件中

* 读取文件中的 last-value提取到。
  * cat 
* 然后将last-value写入到新的文件中





### 导入到HIVE

先导入到hdfs，然后再导入到hive







## 参考资料



* [sqoop知识整理](https://my.oschina.net/jiansin/blog/1803038)
* [Sqoop最佳实践](https://www.jianshu.com/p/be33f4b5c62e)
* [sqoop简介及sqoop1与sqoop2区别](https://blog.csdn.net/lilychen1983/article/details/80241368)
* 案例
  * [Sqoop学习](https://www.jianshu.com/p/9ee76314eac1)