# Docker Compose 例子

[TOC]







# 1. 部署架构

![alt](imgs/docker-compose-exa-jiagou.png)



# 2. 前期准备

## 2.1. 定义工作目录

定义compose工作目录

```shell
mkdir /opt/myapp ;cd /opt/myapp

mkdir rabbitmq mysql redis tomcat-hz tomcat-wx
```



## 2.2. 定义存储空间

如果是使用本地目录，docker-compose会自动建立，**可以不执行下面的代码**。

如果使用nfs网络存储，需要重新挂载网络存储。

```shell
#如果是使用本地目录，docker-compose会自动建立，下面的代码是示例，没有实际用途
mkdir /data/myapp
cd /data/myapp
mkdir rabbitmq mysql redis tomcat-hz tomcat-wx
```





# 3. 具体实现





## 3.0 常见问题

### ① 容器的时区问题

docket安装的默认是美国时区，如果宿主机是北京时间，那么会发现双方相差几个小时

```shell
#测试方法：登录到容器中,执行这个指令，看看日期与宿主机器是否相同
date
```

> 解决方法

```shell
#在compose文件中的volumes，将宿主机器的文件给复制到容器中，:ro 表示只读，担心被容器给写了
    volumes:
      - /etc/localtime:/etc/localtime:ro
```





### ② Tomcat时区问题

即使Docker容器的时区设置对了，但是看Tomcat日期，时区还是不对。

这时候需要传递一个环境变量给容器。

> 解决方法

```shell
#在compose文件中给Tomcat添加系统变量
    environment:  
      TZ: 'Asia/Shanghai'  
```





### ③ Mysql时区问题

> 解决方法

将Mysql所在容器的时区配置正确，mysql时区取自所在容器。

具体解决方案见：***① 容器的时区问题***



### ④ 严格启动顺序

tomcat 只有在Mysql Redis 与 RabbitMq 启动后才可以用

使用诸如[wait-for-it](https://github.com/vishnubob/wait-for-it)， [dockerize](https://github.com/jwilder/dockerize)或sh-compatible [wait-for之类的工具](https://github.com/Eficode/wait-for)。这些是小型包装脚本，您可以在应用程序的映像中包括这些脚本，以轮询给定的主机和端口，直到它接受TCP连接为止。

```yaml
# 例子代码
depends_on:
  - mysql
  entrypoint: “bash /usr/local/bin/wait-for-it.sh mysql:3306 – java -jar /safebox-eureka.jar”
```



Docker官网也给出了一些解决方案：https://docs.docker.com/compose/startup-order/



### ⑤ 是否选择OpenJdk

OpenJdk与Oracle发行版的选择



## 3.1 定义变量

```shell
cd /opt/myapp
vi .env
```

> 文件内容

```properties
# 基础路径
DATA_PATH=/data/myapp
```



## 3.2 撰写Dockerfile



### Tomcat

有两种做法：

* 不适用DockerFile
  * 好处是，不用再编译镜像了，生了编译的过程与空间。
  * 将编译好的server.xml直接通过`-v`外挂到镜像中。
* 使用DockerFile
  * 坏处是多了一个镜像文件
  * 好处是为了未来，可以将应用程序等打包到镜像中，今后使用镜像来更新程序。



由于Tomcat需要单独的配置，所以这里单独进行了设置。

实际中`tomcat-wx tomcat-hz`要分别建立不同的Dockerfile文件，另外按照官网的提示，要将应用程序打包到镜像中。





#### ① 复制server.xml

先生成一个`tomcat:9.0.20-jre8-alpine`镜像，并将文件复制出来。

```shell
docker run --name my-tomcat-temp  -d tomcat:9.0.20-jre8-alpine
docker cp my-tomcat-temp:/usr/local/tomcat/conf/server.xml ./
docker rm -f  my-tomcat-temp
ls
```



#### ② 修改server.xml

在`</host>`前一行添加

```xml
<Context path="" docBase="/usr/local/tomcat/webapps" debug="0" reloadable="false"/>
```



> 参考资料

* [详解Tomcat 配置文件server.xml](https://www.cnblogs.com/kismetv/p/7228274.html)
* [tomcat安全配置参考](https://www.cnblogs.com/youqc/p/9402586.html)



#### ③ 创建Dockerfile

```shell
cd /opt/myapp/tomcat-wx
vi /opt/myapp/tomcat-wx/Dockerfile
```

> Dockerfile

```dockerfile
#带有管理界面的rabbitmq
FROM tomcat:9.0.20-jre8-alpine
#修改配置
COPY server.xml /usr/local/tomcat/conf
```





### 3.2.2 Mysql

官网给出了示例代码

```shell
#Creating database dumps
docker exec some-mysql sh -c 'exec mysqldump --all-databases -uroot -p"$MYSQL_ROOT_PASSWORD"' > /some/path/on/your/host/all-databases.sql

#Restoring data from dump files
docker exec -i some-mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < /some/path/on/your/host/all-databases.sql
```





#### ① 宿主机上导入数据

> 模拟一个SQL语句

定义一个文件：init.sql

```sql
create database wk;
use wk;
create table user (id int,name varchar(50));
insert into user values(1,'james');
insert into user values(2,'sophia');
```



> 进行导入

* 一定要加上-T 
* 执行这个一定要小心。应该在开发环境测试无误后，再提交到服务器上



```shell
docker-compose exec -T mysql sh -c 'exec mysql -uroot -pmysql@root ' < ./00-sql-init/init.sql
```





#### ② 宿主机上备份数据

导出一个以秒为日期的数据

```shell
docker-compose exec mysql sh -c 'exec mysqldump --opt -uroot -pmysql@root wk' >wk-$(date +"%Y%m%d-%H%M%S").sql
```



### 3.2.3 Backup定时备份



#### ① 注意事项

* docker守护进程

```
如果记得在生成一个alpine镜像时，要做一个for循环，不然镜像就退出。
由于/bin/sh ash 都是后台执行，所以容器会exit
这就时说为什么要做 crond -f  ， -f 是让crond在前台执行。
```

* 使用 bash或ash(alpine)进行登录

```
使用/bin/sh登录容器，删除键不好用
```

* ENTRYPOINT 还没有弄明白怎么做



#### ② 创建数据库备份脚本



> 定义工作目录

```shell
mkdir -p /opt/myapp/backup; cd /opt/myapp/backup
```

> 建立脚本

```shell
cd /opt/myapp/backup
vi backup.sh
```



> backup.sh

```sh
#!/bin/sh

#database info
DB_USER="root"
DB_PASS=$MYSQL_ROOT_PASSWORD
DB_HOST=$MYSQL_HOST
DB_NAME=$MYSQL_DB

# 定义在容器内部的路径
ADM_DIR="/myapp/mysqladm/"            #the backup.sh path
BCK_DIR="/myapp/mysqladm/files"    #the backup file directory

if [ ! -d $ADM_DIR  ];then
  mkdir $ADM_DIR
fi

if [ ! -d $BCK_DIR  ];then
  mkdir $BCK_DIR
fi


DATE=`date +%F`
#备份数据库的文件名
OUT_SQL="${DB_NAME}-${DATE}.sql"

#最终保存的数据库备份文件名
TAR_SQL="${DB_NAME}-${DATE}.tar"

mysqldump --opt -u$DB_USER -p$DB_PASS -h$DB_HOST $DB_NAME > $BCK_DIR/$OUT_SQL

#========================================
#DAYS=15代表删除15天前的备份，即只保留最近15天的备份
DAYS=15

#进入备份存放目录
cd $BCK_DIR
tar -czf $TAR_SQL ./$OUT_SQL

#删除.sql格式的备份文件
rm $OUT_SQL


#删除15天前的备份文件(注意：{} \;中间有空格)
find $BCK_DIR -name "${DB_NAME}-*" -type f -mtime +$DAYS -exec rm {} \;
```



> 可以执行

```shell
chmod +x backup.sh
```



#### ③ 创建应用程序备份脚本

主要备份的内容有：

* 应用程序
* 用户上传的数据，例如图片，文件等。

具体操作省略





#### ④  配置定时任务



```shell
cd /opt/myapp/backup
#设定定时任务,每天早上3点1分进行备份
cat <<EOF > crontab.bak
1 3 * * * /myapp/mysqladm/backup.sh
EOF
#做结尾行，不然会出现错误
echo "" >> crontab.bak

```



下面是测试用，每分钟备份一次

```
*/1 * * * * /myapp/mysqladm/backup.sh
```





#### ⑤ 撰写Dockerfile

```shell
cd /opt/myapp/backup
vi /opt/myapp/backup/Dockerfile
```

> Dockerfile

```dockerfile
#备份镜像
FROM alpine

#复制备份脚本
COPY backup.sh    /myapp/mysqladm/
#复制定时任务
COPY crontab.bak  /myapp/


# 安装mysql客户端
RUN apk add --no-cache mysql-client \
      # 启动定时任务
      && crontab /myapp/crontab.bak    

# 启动定时任务,必须添加-f ,不然容器启动不了
CMD ["crond","-f"]
```



### 3.2.4 Rabbitmq

由于Rabbitmq需要启动MQTT插件，所以这里单独定制了一个镜像。



#### ① Dockerfile

```shell
cd /opt/myapp/rabbitmq
vi /opt/myapp/rabbitmq/Dockerfile
```

> Dockerfile

```dockerfile
#带有管理界面的rabbitmq
FROM rabbitmq:3.8.0-management-alpine
# 启动mqtt插件
RUN rabbitmq-plugins enable --offline rabbitmq_mqtt
```



#### ② 映射端口

RabbitMQ 已经有一些自带管理插件的镜像。用这些镜像创建的容器实例可以直接使用默认的 15672 端口访问，默认账号密码是`guest/guest`





- `4369` (epmd), `25672` (Erlang distribution)
- `5672` 是amqp默认端口 , `5671` (AMQP 0-9-1 without and with TLS)
- `15672` (if management plugin is enabled) 是rabbitmq management管理界面默认访问端口
- `61613`, `61614` (if STOMP is enabled)
- `1883`, `8883` (if MQTT is enabled)  mqtt tcp协议默认端口





#### ③  参考资料

> 相关文档

- [RabbitMQ手册之rabbitmq-plugins](https://www.jianshu.com/p/0ff7c2e5c7cb)
- [Docker安装RabbitMQ配置MQTT](https://blog.csdn.net/hololens/article/details/80059991)
- [Docker 部署 RabbitMQ 集群](https://www.jianshu.com/p/52546bcf8723?utm_source=oschina-app)
- [RabbitMQ的简单使用](https://blog.csdn.net/wangbing25307/article/details/80845641)







## 3.3 撰写compose文件

```shell
cd /opt/myapp
vi docker-compose.yml
```





```yml
version: '3'
services:

  tomcat-wx:
    hostname: tomcat-wx
    restart: always
    build: ./tomcat-wx
    #容器的映射端口，21080是宿主机的端口
    ports:
      - 21080:8080    
    #定义挂载点
    volumes:
      - ${DATA_PATH}/tomcat-wx/webapps:/usr/local/tomcat/webapps
      - ${DATA_PATH}/tomcat-wx/logs:/usr/local/tomcat/logs
      - /etc/localtime:/etc/localtime:ro
    environment:  
      TZ: 'Asia/Shanghai'  
    #启动依赖  
    depends_on:
      - mysql
      - redis
      - rabbitmq

  #mysql
  mysql:
    hostname: mysql
    image: mysql:5.7
    restart: always
    volumes:
      - ${DATA_PATH}/mysql/conf:/etc/mysql/conf.d
      - ${DATA_PATH}/mysql/data:/var/lib/mysql
      - /etc/localtime:/etc/localtime:ro
    environment:
      MYSQL_ROOT_PASSWORD: mysql@root
      
  #定时备份业务：将要备份的数据库传入，同时要设置日期，不然时间不对。    
  backup:
    hostname: backup
    build: ./backup
    restart: always
    tty: true
    environment:
      MYSQL_ROOT_PASSWORD: mysql@root
      MYSQL_HOST: mysql
      MYSQL_DB: sys
    volumes:
      - ${DATA_PATH}/backup/mysql:/myapp/mysqladm/files
      - /etc/localtime:/etc/localtime:ro
    #启动依赖  
    depends_on:
      - mysql      
      
  #redis
  redis:
    hostname: redis
    image: redis:5.0.6-alpine
    restart: always
    # 开启持久化，并设置密码
    command: redis-server --appendonly yes --requirepass "redis123"
    volumes:
      - ${DATA_PATH}/redis/data:/data
      - /etc/localtime:/etc/localtime:ro
      
  #rabbitmq
  rabbitmq:
    hostname: rabbitmq
    build: ./rabbitmq
    restart: always
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: fanhualei 
    volumes:
      - /etc/localtime:/etc/localtime:ro
      - ${DATA_PATH}/rabbitmq/data:/var/lib/rabbitmq
    ports:
      - "15672:15672"
      
      
  #mosquitto 主要是为了测试 rabbitmq的客户端
  mosquitto:
    hostname: mosquitto
    image: eclipse-mosquitto:1.6.7
    restart: always      
      
```



## 3.4 生成容器

```shell
docker-compose up --build -d
```





## 3.5 单元测试

①②③④⑤⑥⑦⑧⑨

### 3.5.1 常用命令

```shell
#停止运行并移除容器
docker-compose down

#启动单个服务
docker-compose up -d 服务名

#查看当前运行的服务
docker-compose ps

#构建镜像，--no-cache表示不用缓存，否则在重新编辑Dockerfile后再build可能会直接使用缓存而导致新编辑内容不生效
docker-compose build --no-cache

#查看镜像
docker-compose images

#查看日志
docker-compose logs

#启动/停止服务
docker-compose start/stop 服务名

#拉取镜像
docker-compose pull 镜像名
```



### 3.5.2 测试Tomcat

#### ① 添加index.html

```shell
echo hello James.    $(date +%F%n%T) > /data/myapp/tomcat-wx/webapps/index.html
```



#### ② 浏览器打开首页

由于tomcat-wx外挂了`21080`，所以可以用宿主机的IP地址来访问。

在浏览器中输入：http://192.168.1.179:21080/



#### ③ 测试日志是否正确

日志的日期，以及日志是否持久化

```shell
ls /data/myapp/tomcat-wx/logs
more /data/myapp/tomcat-wx/logs/localhost_access_log.2019-10-09.txt
```



```shell
catalina.2019-10-09.log  # tomcat自身的日志
host-manager.2019-10-09.log   # 管理相关日志
localhost.2019-10-09.log  
localhost_access_log.2019-10-09.txt  #访问日志文件
manager.2019-10-09.log # 管理相关日志
```



#### ④ 登录到容器中

看看日期是否正确

```shell
docker-compose exec  tomcat-wx  /bin/sh
#看与宿主机是否一致
date
```



### 3.5.3 测试Mysql



#### ① 登录到容器中

看看日期是否正确

```shell
docker-compose exec  mysql  /bin/bash
#看与宿主机是否一致
date

#登录到mysql 
mysql -uroot -pmysql@root

#看看mysql的 now()函数日期与服务器是否一致
mysql>select now();

#看看mysql的 时区是否取自服务器
mysql>show variables like '%time_zone%';
```



#### ② mysql数据持续化

登录到mysql中添加数据，在今后的过程中，再重新Up时候，看看数据是否保存下来

```shell
docker-compose exec  mysql  mysql -uroot -pmysql@root

#登录到mysql 

> show databases;
> create database wk;
> use wk;
> show tables;
> create table user (id int,name varchar(50));
> insert into user values(1,'james');
> insert into user values(2,'sophia');
> select * from wk.user;
```



#### ③ 备份数据库

见 3.2.3章节的backup容器。

#### ④ 批量导入数据

见 3.2.2章节的backup容器。



### 3.5.4 测试Redis



#### ① 登录到redis中

```shell
# -a redis密码
docker-compose exec redis redis-cli -a redis123
```



#### ② 进行一些基本操作

```shell
keys *
set key1 "hello"
get key1
set key2 1
INCR key2
get key2
```



```
登录redis即获得帮助
    redis-cli
    help    
基本使用命令
    查看所有的key列表  keys *
    增加一条记录key1  set key1 "hello"
    得到数据         get key1
    增加一条数字记录  set key2 1
    让数字自增       INCR key2
    删除一个        del key1   
    删除所有数据     flushall
```



#### ③ 测试持久化

删除容器后重启，发现以前的数据都还在

```shell
docker-compose down
docker-compose up -d
docker-compose exec redis redis-cli -a redis123
127.0.0.1:6379>  get key1
```





### 3.5.5 测试rabbitmq

[参考文档](https://github.com/fanhualei/wukong-framework/blob/master/reference/mq.md)

#### ①  Web是否可以访问

在浏览器中输入`http://192.168.1.179:15672/  `，访问到rabbitmq，用户名：guest  密码：fanhualei



#### ②  rabbitmq基本操作

```shell
docker-compose exec rabbitmq /bin/ash

#查看状态
rabbitmqctl status

#查看可用插件及已安装插件
rabbitmq-plugins list

#查看用户
rabbitmqctl list_users

#添加管理用户
rabbitmqctl add_user admin yourpassword
rabbitmqctl set_user_tags admin administrator
```



#### ③ 测试mosquitto服务

mosquitto是一个mqtt服务，docker镜像才3M，所以拿过来当客户端用。

[Mosquitto-pub地址](https://mosquitto.org/man/mosquitto_pub-1.html)  [Mosquitto-sub地址](https://mosquitto.org/man/mosquitto_sub-1.html)



> 打开一个窗口，用来监听

```
docker-compose exec mosquitto mosquitto_sub -t topic1 
```

想结束了，就用`ctrl+c`来结束



> 打开一个窗口，用来发送

```
docker-compose exec mosquitto mosquitto_pub -t topic1 -m 'hello world1'
```





#### ④ 测试rabbitmq-mqtt

* -h rabbitmq 用来将服务器指向rabbitmq
* -u guest 用户名
* -p fanhualei 密码



> 打开一个窗口，用来监听

```
docker-compose exec mosquitto mosquitto_sub -t topic1  -h rabbitmq -u guest -P fanhualei
```

想结束了，就用`ctrl+c`来结束



> 打开一个窗口，用来发送

```
docker-compose exec mosquitto mosquitto_pub -t topic1 -m 'hello world1'  -h rabbitmq -u guest -P fanhualei
```



![alt](imgs/docker-compose-rabbitmq-mqtt.png)



#### ⑤ 添加一些数据

添加exchange

![alt](imgs/docker-compose-rabbitmq-exchange.png)



#### ⑥ 删除容器后看数据

做了数据持久化，删除容器后，容器中的数据应该在。









> mqtt客户端

* [MQTT入门（4）- 客户端工具](https://www.iteye.com/blog/rensanning-2406598)

* 推荐：MQTTfx  或 Mosquitto 





## 3.6 集成测试

> 测试的主要内容

* 内部集成
  * 内部 Tomcat Redis Mysql rabbitMq联通
* Nginx集成
  * 方向代理Tomcat
    * Https解析
    * WebSocket
  * 反向代理RabbitMq

### ①  容器内部集成

上面已经测试过，`backup` 可以连通`mysql` 。 `mosquitto` 可以连通`rabbitmq`

下面就不进行实际的测试了。



### ② Nginx 反向代理tomcat



### ③ Nginx 反向Https





## 3.7 压力测试



> 启动一个窗口进行压力测试

`ab`是一个压力测试工具

```shell
yum install httpd-tools
ab -c 5000 -n 500000 http://192.168.1.186:31524/
```







# 参考文档

* 基本使用
  * [docker和docker-compose的前后端项目部署（含MySQL，Redis和RabbitMQ）](https://www.jianshu.com/p/528fa4d62ace)
  * [docker-compose.yml部署redis，mysql，tomcat，jenkins，activemq](https://blog.csdn.net/qq_40460909/article/details/84672492)
  * [Docker Compose 搭建Mysql主从复制集群](https://blog.csdn.net/u012562943/article/details/86589834)
* Tomcat使用
  * [tomcat常用配置详解和优化方法](https://www.cnblogs.com/xuwc/p/8523681.html)