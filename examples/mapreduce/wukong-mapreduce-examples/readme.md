# MapReduce开发

[TOC]






## 例子概述



### 学生成绩统计

| 数据文件           | 类名                         | 功能说明                                                     |
| ------------------ | ---------------------------- | ------------------------------------------------------------ |
| score/score.txt    | CourseScoreMaxMinAvg         | 求每个科目成绩的 最大值 最小值 平均值                        |
| score/score.txt    | CourseScoreGrouping          | 统计科目中分数相同的人有多少,每个人的人名                    |
| score/score.txt    | ScoreAverage                 | 统计学生的平均成绩                                           |
| scorePro/score.txt | CourseScoreAveragePro        | 求每个课程的考试人数与平均成绩                               |
| scorePro/score.txt | CourseScoreAverageProMult    | 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.使用了`MultipleOutputs` |
| scorePro/score.txt | CourseScoreAverageProPartion | 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.使用了`Partitioner` |
| scorePro/score.txt | CourseHighestScoreStudent    | 求每个科目中分数最高的学生,使用了分组工具                    |
|                    |                              |



### 数据去重



| 数据文件              | 类名       | 功能说明 |
| --------------------- | ---------- | -------- |
| distinct/distinct.txt | DistinctMR | 程序去重 |



### 好友关联分析

在求共同好友的时候,进行多个reduce的关联

| 数据文件            | 类名           | 功能说明            |
| ------------------- | -------------- | ------------------- |
| friends/friends.txt | SameFriendsMr  | 求共同好友-错误算法 |
| friends/friends.txt | SameFriendsMr2 | 求共同好友          |
| friends/friends.txt | EachFansMr     | 求相互粉丝的组合    |



### 用户访问行为分析



| 数据文件            | 类名            | 功能说明                     |
| ------------------- | --------------- | ---------------------------- |
| version/version.txt | VersionChangeMr | 版本变化分析                 |
| pv/pv.txt           | PvTimesMr       | 各个时间段每个页面的访问次数 |
|                     |                 |                              |



#### 版本变化分析

[参考文档](https://blog.csdn.net/jin6872115/article/details/79587948)

* `WritableComparator`的作用与group类似
  * 如果加上这个group,`reduce`内部的函数会被改变
    * 当inValues每循环一次,Key就变化一次.
    * 如果不做特殊处理,那么就只按照分组输出最大的数据.
* `WritableComparable` 完成了排序与读取的功能



#### 各个时间段每个页面的访问次数

[参考文档](https://blog.csdn.net/jin6872115/article/details/79589746)

将[页面+时间]作为`key`然后统计次数. 同时使用了`multipleOutputs`输出到不同的文件,感觉比参考文档中的方法好一点.





### 影评分析



### 气象分析

[MapReduce经典小案例：寻找每个月温度最高的两天](https://blog.csdn.net/weixin_44177758/article/details/89929224)




## 使用技巧



### 如何与SQL语句进行映射?



#### 使用SELECT获取数据

最一般的用法



#### 使用WHERE过滤数据

应该在map中对数据进行过滤,然后再进行处理.

使用`context.getConfiguration().getStrings`来进行过滤

```java
public static class MyMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
        private String area;
        
        @Override
        public void setup(Context context){
            this.area=context.getConfiguration().getStrings("area", "BeiJing")[0];
        }
        
        public void map(LongWritable key,Text value,Context context)
            throws IOException,InterruptedException{
            String data=value.toString();
            Phone iPhone=new Phone(data);
            if(this.area.equals(iPhone.area))
                context.write(new Text(iPhone.getType()), new IntWritable(iPhone.getCount()));
        }
 }
```



### 利用Partitioner进行分组

可以定义多个reduce,然后根据数据的不同,执行多个reduce,最终按照分组输出`多个文件`

例如有多个科目的成绩,可以利用partitioner进行分组



### 利用 Combiner 提高系统性能

在Map计算完毕,发送到Reduce前,提前进行一部分计算,来减少网络传输压力.

Combiner 也继承于Reducer,在job中进行注册.



### 自定义Key说明

自定定义一个`Java对象`对象,来对数据文件中的一行数据进行封装.可以实现排序 分组等功能.

> 有三个关键类:Writable、Comparable、WritableComparable

* Writable定了write readFields 用来写或读数据流.
* Comparable定义了compareTo方法用来做两个对象对比
* WritableComparable集成了上述方法.



### 数据分组与排序的方法

####  RawComparator 实现数据排序

既然可以通过自定义Key来进行排序,那么为啥还要使用`RawComparator`呢? 因为好多自定义Key是第三方,不能修改他们的源代码,所以要使用`RawComparator`进行二次封装.



#### 利用 WritableComparator 实现数据排序



#### 利用 WritableComparator 实现数据分组



### 数据集连接处理方式介绍

* Map 端连接查询
  * 两个数据集中有一个非常小而另一个非常大时
  * 利用 DistributeCache 做缓存处理,把较小的数据集加载到缓存
* Reduce 端连接查询
  * 当两个数据集中的数据都非常大时
  * 可以设置不同的 Mapper 数据读入类，把连接键作为 Mapper 的输出键
  * 设置 GroupingComparator 时按需要把连接键的属性作为分组处理的标识，这样就能确保两个数据集中相同连接键的数据会被同一个 reduce 方法处理。



> Map端连接方法

```java
job.addCacheFile(new URI(args[2]));
MyMapper.setup中
URI[] uris=context.getCacheFiles();
读到文件的名字,然后将文件的内容读取进来.

```



> Reduce一般比较复杂

* 定义一个key :WritableComparable
* 定义一个value :Writable
* 定义一个排序组件SortComparator:WritableComparator
* 定义一个分组组件GroupComparator:WritableComparator

```java
// 会定义两个Mapper,然后分别进行maper处理
MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class,OrderMapper.class);
MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class,SendMapper.class);
```







## 程序思路



### 成绩统计

[参考网址](https://blog.csdn.net/jin6872115/article/details/79585755)

#### 数据

```
计算机,黄晓明,85
计算机,徐铮,54
英语,赵本山,57
英语,刘亦菲,85
英语,刘亦菲,76
语文,刘涛,85
语文,马云,42
语文,莫言,81
数学,黄晓明,85
数学,刘嘉玲,85
```

#### 目标

```
1、每一个course的最高分，最低分，平均分
	例子：计算机	max=99	min=48	avg=75

2、求该成绩表当中科目出现了相同分数（人数大于1）的，求分数，次数，以及该分数的人
	返回结果的格式：
	科目	分数	次数	该分数的人
	例子：
	计算机 85	3	莫言,刘嘉玲,黄晓明

3 求每个学生的平均成绩
	例子:黄磊	考试了4门科目,平均成绩:84.0
```



#### 解决思路



第一题思路：在map以course作为key值，其余部分作为value，在reduce中设置变量max，min，avg，通过累计求出，并设置格式.



第二题思路：求某科目中出现系统分数的人数以及分数，map以科目和分数作为key值，进行分组，在reduce中进行计数，当计数结果大于1时，输出分数，人数和人名



第三题思路：求每个学生的平均成绩 , map key=学生姓名  value=成绩. reduce中 设置变量`count`与`agv`来计算每个学生的平均成绩.



### 成绩统计增强版

[参考网址](https://blog.csdn.net/jin6872115/article/details/79587210)

#### 数据

```
计算机,黄晓明,85,86,41,75,93,42,85
计算机,徐铮,54,52,86,91,42
英语,黄晓明,96,85,42,96,38
英语,黄磊,85,85,42,96,38
英语,刘嘉玲,75,85,42,96,38
语文,马云,42,42,96,38
语文,莫言,81,42,96,38
数学,徐铮,54,42,96,38
数学,黄晓明,85,42,96,38
数学,刘嘉玲,85,42,96,38
```

>  数据解释

* 数据字段个数不固定：

* 第一个是课程名称

* 第二个是学生姓名，后面是每次考试的分数



#### 目标

>  统计需求

* 统计每门课程的参考人数和课程平均分
  * 例如:数学   7 62.285713
* 统计每门课程参考学生的平均分，并且按课程存入不同的结果文件，要求一门课程一个结果文件，并且按平均分从高到低排序，分数保留一位小数
* 求出每门课程参考学生平均分最高的学生的信息：课程，姓名和平均分



#### 解决思虑

第一题思路： 实际上是group by 课程, 统计人数,统计平均分.

```
数学	 7 62.285713
英语	 9 63.11111
计算机	 10 66.5
语文	 6 63.0
```



第二题思路： 

* 计算出每个学生的平均分,这个与第一题思路一样.
* 导出到不同文件需要指定setNumReduceTasks，分区规则通过使用partitioner进行分区设定
* 平均成绩需要进行排序，可以使用封装对象的方式，通过实现WritableComparable接口进行设置排序规则



> 关于分页有两种思路

使用partitioner,但是这个必须要指定setNumReduceTasks,并且这个要大于实际返回的.



还有一中分页,是使用MultiPleOutputs







第三题思路： 实际上是group by 课程, 统计人数,统计平均分.



## 关键问题



### 如何输出到不同的文件?



### 如何进行排序?

首先Hadoop默认对Key进行排序,如果要对一个数据集进行排序,需要自定一个`key`对象,并且重载该对象的比较方法.

具体步骤:

* 定一个对象,继承`WritableComparable`接口

  * 在接口中实现比较的方法`compareTo`
    * 比较的方法返回:负整数、零或正整数=小于、等于或大于
    * 比较方法中还可以定义多个排序,例如按照地区\年龄\进行排序

* Map中返回key=定义对象,value=NullWritable

  * value也可以定义成其他的.

  

### 如何取得每组中最大的数值?












## 系统配置

> 注意事项

为每个要执行的类,指定参数

![alt](doc/imgs/idea-seting.png)





```
HADOOP_HOME /opt/modules/apache/hadoop-2.9.2
```






> 参考文档

* [MapReduce — 数据分类输出和小文件合并](https://blog.csdn.net/qq_41851454/article/details/79620347)
* [网友案例12篇](https://blog.csdn.net/jin6872115/article/category/7513962)
* [MapReduce解密](https://www.cnblogs.com/leslies2/p/9009574.html)

