# Hive高级功能





## ETL

主要步骤：

* 1、得到一个完整的数据
* 2、清洗表，按照不同维度抽取数据
* 3、聚合数据
* 4、最后进行展示



## Hive常用语法操作



### 读取数据的场景



#### 一、HDFS上已经存在了数据文件

可以在hive建立一个表，将已有的数据文件与这个表关联起来。



> 假设在本地input/student.txt有个文件

```txt
1,小明
2,宣萱
3,悠悠
4,安安
5,小鱼
```

在一般情况下，如果在相对路径下，所有的数据默认放在`/user/root`，由于我是root登录的。

```shell
# 假设我是root登录的,下面三条显示的效果是一样的。建议加上完整的路径
$  bin/hdfs dfs -ls /user/root
$  bin/hdfs dfs -ls
$  bin/hdfs dfs -ls ./
```




> 第一步：通过hdfs，建立一个目录，并上传文件

```shell
# 建立目录
$ bin/hdfs dfs -mkdir hivetest

# 上传文件
$ bin/hdfs dfs -put input/student.txt hivetest
```




> 第二步：通过hive，建立一个表，并关联上一步的文件。

```shell
$ hive
$hive> use wukong;
$hive> show tables;
$hive> # 执行下面的sql语句  

```



sql语句如下: 以`,`为分隔符，并且放在另外一个目录中

```sql
create table wukong.student(
    id int,
    name string
) 
row format delimited fields terminated by ',' 
location '/user/root/hivetest';
```



> 第三步：通过hive，查询这个表，看是否关联成功

```shell
$hive>select * from student;
$hive>insert into student(id,name) values(6,'dddd');
```



#### 二、从HIVE开始建表

在hive中建立一个表，然后通过hive的命令将数据传入。

默认的路径在`/user/hive/warehouse`。如果建立一个`wukong`表，那么会建立这么一个路径`/user/hive/warehouse/wukong.db`.

![alt](imgs/hive-database.png)





> 如果想修改默认的存储路径，可以修改hive-seit.xml这个配置文件中的内容

```
  <property>
    <name>hive.metastore.warehouse.dir</name>
    <value>/user/hive/warehouse</value>
    <description>location of default database for the warehouse</description>
  </property>

```







### 编辑数据



#### 清空数据与表

```sql
// 强制清空一个表
drop database dbName cascade;

// 清空表的数据
truncate table tableName;

//删除表
drop table if exists tableName;

```



#### 追加数据

```sql
//上传数据，会追加数据,如果重复执行下面的命令，会重复多条 
load data local inpath '/opt/datas/test' into talbe teset;

//新版本支持insert 语句，尽量不用，很慢，有时候会死机
insert into table student(6,'爸爸');

//
insert into tablename  select * from tablename2
```



#### 创建表的三种方法



通过`show create table test;` 可以看到生成这个表的语句。



> 最普通的方法：create table

以前已经提到过，这里不说了。



>子查询法，检索出的结果生成一张表

```sql
create table test_02 as select name from test;
```



> 只复制表结构，不复制数据，就像视图一样

```
create table test_like like test;
```



### 导出数据

除了导出到本地，也可以到出到hdfs空间中。



#### 方式一：使用insert导出到本地

insert overwrite [local] directory 'path'  select sql;

* 一般要指定分割符号
* 可以控制导出的数据量
* 如果指定目录存在，系统会自动删除
* 如果目录不存在，那么会自动追加

```
insert overwrite local directory '/opt/datas/hive-output'
row format delimited
fields terminated by ','
select * from test limit 100;
```



#### 方式二：使用hive -e -f 进行导出

--使用hive的-e参数

`hive –e “select * from wyp” >> /local/wyp.txt`

 

--使用hive的-f参数, wyp.hql中为hql语句

`hive –f wyp.hql >> /local/wyp2.tx`



### 增大Hive默认内存

修改hive-env.sh





### 常用hsql语句



#### 过滤语句

* where
* limit
* distinct
* between and
* is null  & is no null



#### 聚合函数

* count
* avg
* max
* min

#### 分组

* group by



#### 多表查询

* join
* join left
* join right
* join full







## hive案例



### 案例1：部门职位



#### 1：准备数据

在`/opt/datas/`目录下建立这两个文件

emp.txt

```
7369    SMITH   CLERK   7902    1980-12-17      800.0   NULL    20
7499    ALLEN   SALESMAN        7698    1981-2-20       1600.0  300.0   30
7521    WARD    SALESMAN        7698    1981-2-22       1250.0  500.0   30
7566    JONES   MANAGER 7839    1981-4-2        2975.0  NULL    20
7654    MARTIN  SALESMAN        7698    1981-9-28       1250.0  1400.0  30
7698    BLAKE   MANAGER 7839    1981-5-1        2850.0  NULL    30
7782    CLARK   MANAGER 7839    1981-6-9        2450.0  NULL    10
7788    SCOTT   ANALYST 7566    1987-4-19       3000.0  NULL    20
7839    KING    PRESIDENT       NULL    1981-11-17      5000.0  NULL    10
7844    TURNER  SALESMAN        7698    1981-9-8        1500.0  0.0     30
7876    ADAMS   CLERK   7788    1987-5-23       1100.0  NULL    20
7900    JAMES   CLERK   7698    1981-12-3       950.0   NULL    30
7902    FORD    ANALYST 7566    1981-12-3       3000.0  NULL    20
7934    MILLER  CLERK   7782    1982-1-23       1300.0  NULL    10
```

dept.txt

```
10      ACCOUNTING      NEW YORK
20      RESEARCH        DALLAS
30      SALES   CHICAGO
40      OPERATIONS      BOSTON
```



#### 2：建立表并上传数据











### 基本知识



#### 表的区别

* 内部表
  * 删除内部表时，删除数据
* 外部表
  * create external table tableName
  * 删除外部表时，不删除数据。
    * 如果要删除数据 用 dfs -rm 进行删除
* 临时表
  * 当关闭hive时，会自动删除临时表
  * create temporary table tableName
* 桶表
  * 用的不多，可以不使用
  * 把一个文件分成多个文件





> 参考文档

* [Hive基础之表操作](https://www.jianshu.com/p/c41292b98bed)