# 系列案例



## 系统配置

> 注意事项

为每个要执行的类,指定参数

![alt](doc/imgs/idea-seting.png)





```
HADOOP_HOME /opt/modules/apache/hadoop-2.9.2
```





## 成绩统计



[参考网址](https://blog.csdn.net/jin6872115/article/details/79585755)

> 数据

```
计算机,huangxiaoming,85
计算机,xuzheng,54
英语,zhaobenshan,57
英语,liuyifei,85
英语,liuyifei,76
语文,liuyifei,75
语文,huangzitao,81
数学,wangbaoqiang,85
数学,huanglei,76
```

> 目标

```
1、每一个course的最高分，最低分，平均分
例子：计算机	max=99	min=48	avg=75

2、求该成绩表当中科目出现了相同分数（人数大于1）的，求分数，次数，以及该分数的人
返回结果的格式：
科目	分数	次数	该分数的人
例子：
计算机 85	3	huangzitao,liujialing,huangxiaoming
```



第一题思路：在map以course作为key值，其余部分作为value，在reduce中设置变量max，min，avg，通过累计求出，并设置格式.



第二题思路：求某科目中出现系统分数的人数以及分数，map以科目和分数作为key值，进行分组，在reduce中进行计数，当计数结果大于1时，输出分数，人数和人名



















> 参考文档

* [MapReduce — 数据分类输出和小文件合并](https://blog.csdn.net/qq_41851454/article/details/79620347)
* [网友案例12篇](https://blog.csdn.net/jin6872115/article/category/7513962)

