# Phoenix使用

官网地址：http://phoenix.apache.org/



## 1. 安装

### 1.1. 下载



### 1.2. 解压与配置

```shell
# 解压

# 将安装phoenix-***-cilent.jar复制到hbase下的lib目录下

# 将phoenix下的lib下的phoenix-core-***.jar 复制到 hbase下的lib

# 将hbase的conf 目录下 hbase-site文件放到 phoenix的bin目录下
```



### 1.3. 启动

确定`hdfs yarn hbase`已经启动

```shell
# bin/sqlline.py  zk服务器:端口,第一次启动可能需要几分钟
bin/sqlline.py  localhost:2181
```

有可能python没有安装，可以按照提示进行安装，例如

```shell
# 如果报argparse错误
yum -y install python-argparse
```



### 1.4. 简单测试



```shell
> help
```

* 没有添加双引号，表明与字段名都是大写。 大小写区分。
* 有数据类型
* 在添加数据时，数值类型不加引号，字符串只能用单引号



> 创建表

```sql
CREATE TABLE IF NOT EXISTS us_population (
      state CHAR(2) NOT NULL,
      city VARCHAR NOT NULL,
      population BIGINT
      CONSTRAINT my_pk PRIMARY KEY (state, city)
);
```



> 插入数据

```sql
UPSERT INTO us_population VALUES('NY','New York',8143197);
UPSERT INTO us_population VALUES('CA','Los Angeles',3844829);
UPSERT INTO us_population VALUES('IL','Chicago',2842518);
UPSERT INTO us_population VALUES('TX','Houston',2016582);
UPSERT INTO us_population VALUES('PA','Philadelphia',1463281);
UPSERT INTO us_population VALUES('AZ','Phoenix',1461575);
UPSERT INTO us_population VALUES('TX','San Antonio',1256509);
UPSERT INTO us_population VALUES('CA','San Diego',1255540);
UPSERT INTO us_population VALUES('TX','Dallas',1213825);
UPSERT INTO us_population VALUES('CA','San Jose',912332);
```

`UPSERT`具有插入与修改功能

也可以将数据下如`CVS`文件，然后批量导入。 将网页[在HBase中用SQL：Phoenix](https://blog.csdn.net/nsrainbow/article/details/43776607?locationNum=16&fps=1)



> 查询

```sql
select * from us_population;

-- 修改数据 如何找到联合主键
UPSERT INTO us_population(state,city,population) values('NY','New York',800000)

-- 按照洲进行分组并查询 
SELECT state as "State",count(city) as "City Count",sum(population) as "Population Sum"
	FROM us_population
	GROUP BY state
	ORDER BY sum(population) DESC;
```



> 删除

```sql
delete from us_population where state ='CA';
```



### 1.5 将已有的表映射到phoenix

做这个一步要小心，有可能因为数据类型不一样，数据被清空。

假设`hbase`中已经有了一些表，那么需要映射到phoenix.

```sql
create table "teacher"(
	rowkeyy bigint primary key,
	"info"."name" varchar,
	...............
)
```



### 1.6 常见问题

> 如何添加列族名?



默认列族名为0

除了主键意外，加上列族名

```sql
CREATE TABLE IF NOT EXISTS "us_population" (
      "state" CHAR(2) NOT NULL,
      "city" VARCHAR NOT NULL,
      "info"."population" BIGINT
      CONSTRAINT my_pk PRIMARY KEY ("state", "city")
);
```



> 使用小松鼠客户端连接phoenix

`squirrel`来连接phoenix

将phoenix-***-client.jar复制到小松鼠的jar包目录中



> idea中连接phoenix

[Phoenix - Intellij-idea创建JDBC连接](https://www.jianshu.com/p/1a76bbe4a33f?utm_source=oschina-app)





## 2. shell使用





## 3. java的使用