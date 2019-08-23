# 例子

## Nginx日志解析


### 准备数据
从服务器上下载access.log,到input/nginx

### 撰写代码

代码放在`src/main/scala/wukong/apark/nginx`中. 开发建议:

* 数据清洗
    * 新建立一个类,用来读取每行数据,并且用一个类封装.
    * 这样做的好处是,可以在读取类中进行单行的测试.
    * AccessLog是保存line数据的类.
    * NginxLogParser是用来解析行的类
        * 这个类有一个main方法,可以单独调试
* 统计分析
    * Nginx是统计分析类.
    * 每小时的PV
    * 每小时的UV
    * 当天访问量排名前5的IP地址
    * 当天访问量排名前5的页面数
    
    
    
### Spark应用参数设置

```scala
    val config = new SparkConf()
      .setAppName("nginx-log")
      .setMaster("local")
```    

有三个地方可以设置:
1. 在spark配置文件中设置参数 spark-defaults.conf
2. 在 bin/spark-submit 的命令中添加参数
3. 在代码中写

以上优先级是3>2>1

### 打包以及上传执行

要将代码中写死的给注释掉

1. 建立jar包,放到某个目录中
2. 通过bin/spark-submit提交应用

> 命令
#### 1. 默认的local提交方式

```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--master local[4] \
/home/jars/o2o22.jar
```

####  2. 在standalone中执行(这个不常用,可以不用执行)

##### 在standalone本地模式
只用修改--master
```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--master spark://hostname:7070 \
/home/jars/o2o22.jar
```

##### 在standalone运行模式
有两种模式,只用修改--deploy-mode

* client:


```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--deploy-mode client \
--master spark://hostname:7070 \
/home/jars/o2o22.jar
```

* cluster:
```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--deploy-mode cluster \
--master spark://hostname:6066 \
/home/jars/o2o22.jar
```

#### 3.在yarn上执行

* client:


```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--deploy-mode client \
--master yarn \
/home/jars/o2o22.jar
```

* cluster:
```shell
bin/spark-submit \
--class wukong.spark.nginx.Nginx \
--deploy-mode cluster \
--master yarn \
/home/jars/o2o22.jar
```