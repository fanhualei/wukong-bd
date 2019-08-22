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

