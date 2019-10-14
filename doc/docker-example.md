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
      - "31883:1883"
      
      
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

内部 Tomcat Redis Mysql rabbitMq联通

上面已经测试过，`backup` 可以连通`mysql` 。 `mosquitto` 可以连通`rabbitmq`





## 3.7 Nginx反向代理Tomcat

Nginx集成

- 方向代理Tomcat
  - Https解析
  - WebSocket
- 反向代理RabbitMq



### 3.7.0 常见问题

方向代理不成功

1：没有将nginx设置成host模式

2：没有将80 443 端口的权限给开放。



```shell
# 添加指定需要开放的端口：
firewall-cmd --add-port=80/tcp --permanent
# 重载入添加的端口：
firewall-cmd --reload
# 查询指定端口是否开启成功：
firewall-cmd --query-port=80/tcp
```





### 3.7.1 新建Nginx工程



#### ① 建立工作目录

```shell
mkdir -p /opt/my-nginx
cd /opt/my-nginx
```



#### ②  创建Dockerfile文件

```
mdir nginx
cd ./nginx
```



> 创建一个`createConf.sh` 文件

```shell
# 运行一个环境，来复制默认的conf文件,然后删除
docker run --name my-nginx-temp  -d nginx:alpine 
docker cp my-nginx-temp:/etc/nginx/nginx.conf ./ 
docker rm -f my-nginx-temp ;

lineNum=$(grep -nr 'include /etc/nginx/conf.d/\*.conf;'  ./nginx.conf  | awk -F ':' '{print $1}') 
numi=${lineNum}i 
sed -i ${numi}"include /etc/nginx/myconf/*.conf;" ./nginx.conf 
sed -i ${numi}"server_tokens off;" ./nginx.conf 
```



```shell
# 执行这个脚本生成一个 nginx.conf，去掉nginx的版本信息，为了安全
chmod +x createConf.sh
./createConf.sh
```



> 创建Dockerfile文件

```dockerfile
#备份镜像
FROM nginx:alpine

#替换脚本
COPY nginx.conf    /etc/nginx/nginx.conf
```



#### ③ 编写comfose文件



```shell
vi docker-compose.yml
```



```yml
version: '3'
services:

  nginx:
    hostname: nginx
    build: ./nginx
    restart: always
    # 此处一定要使用host，不然反向代理不通
    network_mode: host   
    volumes:
      - /data/my-nginx/nginx/www/:/usr/share/nginx/html/
      - /data/my-nginx/nginx/logs/:/var/log/nginx/
      # 自己可以添加nginx的配置文件
      - /data/my-nginx/nginx/myconf/:/etc/nginx/myconf/
      - /etc/localtime:/etc/localtime:ro
```



#### ④  生成容器

```shell
docker-compose up -d
```



#### ⑤ 测试

生成一个测试文件

```
echo hello world $(date "+%Y-%m-%d %H:%M:%S") >/data/my-nginx/nginx/www/index.html
```



- 在浏览器中访问
  - http://192.168.1.179/



### 3.7.2 反向代理80端口



#### ① 确认tomcat可以访问

确认tomcat的端口映射到了服务器。

> 打开这个网址，看看能不能访问到

http://192.168.1.179:21080





#### ②  撰写反向代理文件

新撰写的文件应该放到`myconf`目录中 。

```shell
vi /data/my-nginx/nginx/myconf/my-tomcat.conf
```



> my-tomcat.conf 文件

```xml
server {
  listen 80;
  #这个需要修改
  server_name my-tomcat;
  server_tokens off;
  ## Don't show the nginx version number, a security best practice

  location / {
    proxy_set_header   X-Real-IP $remote_addr;
    proxy_set_header   Host      $http_host;
    #这个需要修改
    proxy_pass  http://192.168.1.179:21080;
  }
}
```



#### ③  重启nginx

重启nginx配置

```shell
# 一般要执行下面文件，检查以下
docker-compose exec nginx nginx -t

# 然后再执行配置文件
docker-compose exec nginx nginx -s reload
```



#### ④  查看是否可以访问

配置本地的`hosts`文件，将my-tomcat指向ip地址`192.168.1.179`



> 打开这个网址，看看能不能访问到

http://my-tomcat/



### 3.7.3 反向代理443端口



#### ① 得到Https证书

在阿里云得到证书文件，并放到指定文件夹`ss-cert`中



#### ②  撰写反向代理文件

新撰写的文件应该放到`myconf`目录中 。

```shell
vi /data/my-nginx/nginx/myconf/my-tomcat-https.conf
```



> my-tomcat-https.conf 文件

```xml
server {
  listen 443 ssl;

  server_name ss.runzhichina.com;
  server_tokens off;

  # ssl on;
  root html;
  index index.html index.htm;
  ssl_certificate   /etc/nginx/myconf/ss-cert/1893036_ss.runzhichina.com.pem;
  ssl_certificate_key  /etc/nginx/myconf/ss-cert/1893036_ss.runzhichina.com.key;
  ssl_session_timeout 5m;
  ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
  ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
  ssl_prefer_server_ciphers on;

  location / {

    # 下面代码是用来通过80端口访问的 21080
     proxy_set_header   X-Real-IP $remote_addr;
     proxy_set_header   Host      $http_host;
     proxy_pass  http://127.0.0.1:21080;

    # 下面是websocket配置
     proxy_http_version 1.1;
     proxy_set_header Upgrade $http_upgrade;
     proxy_set_header Connection "upgrade";
   
   }

}

```



#### ③  重启nginx



重启nginx配置

```shell
# 一般要执行下面文件，检查以下
docker-compose exec nginx nginx -t

# 然后再执行配置文件
docker-compose exec nginx nginx -s reload
```



#### ④  查看是否可以访问

确认tomcat的端口映射到了服务器。

配置本地的`hosts`文件，将`ss.runzhichina.com`指向ip地址`192.168.1.179`



> 打开这个网址，看看能不能访问到

https://ss.runzhichina.com/

![alt](imgs/docker-compose-nginx-https.png)



**如果不能访问**，请看看`443`端口是否打开



## 3.8 Nginx反向代理RabbitMq

Nginx反向代理tomcat的好处有两个：

* 是可以简化SSL配置，tomcat不用配置SSL
* 使用域名来解析到不同的tomcat端口上。



但是不推荐使用Nignx反向代理RabbitMq，原因：

* 使用了Docker后，可以直接将端口开发到服务器上的端口中。
* 还没有找到通过Nginx把SSL简化掉，直接代理到RabbitMq非SSL接口。



> 下面简单说明以下如何进行代理。



### ①  将Mqtt端口映射到宿主机

`1883 `映射到宿主机 `31883`



### ②  在nginx.conf配置stream代理

添加了`stream`这一章节

```xml
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;


events {
    worker_connections  1024;
}

stream {
    server {
        listen 1883;
        proxy_connect_timeout 3s;
        proxy_timeout 525600m;    
        proxy_pass 192.168.1.179:31883;
        }                          
}       


http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    keepalive_timeout  65;

    #gzip  on;

server_tokens off;
include /etc/nginx/myconf/*.conf;
    include /etc/nginx/conf.d/*.conf;
}

```





### ③  开放1833防火墙端口

一定要重启防火墙，并看到这个端口开发了。

```shell
# 添加指定需要开放的端口：
firewall-cmd --add-port=1883/tcp --permanent
# 重载入添加的端口：
firewall-cmd --reload
# 查询指定端口是否开启成功：
firewall-cmd --query-port=1883/tcp
```





### ④  使用MQTTfx进行测试

按照正常的配置方法进行测试就可以了。







## 3.9 RabbitMq启动SSL

主要想反向代理RabbitMq的Https的Mqtt服务



### 3.9.1 基本概念



以下是与TLS相关的基本配置设置：

| 配置键                           | 描述                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| listeners.ssl                    | 侦听TLS连接的端口列表。RabbitMQ可以侦听[单个接口或多个接口](https://www.rabbitmq.com/networking.html)。 |
| ssl_options.cacertfile           | 证书颁发机构（CA）捆绑包文件路径                             |
| ssl_options.certfile             | 服务器证书文件路径                                           |
| ssl_options.keyfile              | 服务器私钥文件路径                                           |
| ssl_options.verify               | 是否应该启用[对等验证](https://www.rabbitmq.com/ssl.html#peer-verification)？ |
| ssl_options.fail_if_no_peer_cert | 设置为true时，如果客户端无法提供证书，则TLS连接将被拒绝      |





### 3.9.2 安装MQTTfx  

> 参考了:[阿里-使用MQTT.fx接入物联网平台](https://www.alibabacloud.com/help/zh/doc-detail/86706.htm)



下载并安装MQTT.fx软件。请访问[MQTT.fx官网](https://mqttfx.jensd.de/index.php/download)。





### 3.9.3 测试直连Mqtt服务



#### ①  将Mqtt端口映射到宿主机

`1883 8883`映射到宿足机



#### ② 防火墙开放端口

```shell
# 添加指定需要开放的端口：
firewall-cmd --add-port=1883/tcp --permanent
# 重载入添加的端口：
firewall-cmd --reload
# 查询指定端口是否开启成功：
firewall-cmd --query-port=1883/tcp
```



#### ③  进行测试

在MQTTfx中配置，并进行测试，这里一定要设置`用户名与密码`

![alt](imgs/docker-compose-mqtt-connect1.png)



### 3.9.4 配置SSL单向认证服务



#### ① 得到证书

有两种方法：

* 手动生成CA，证书和私钥
* 自动生成CA，证书和私钥
  * 参考这个文档[RabbitMQ指南（七） SSL\TLS通信](https://blog.csdn.net/weixin_43533358/article/details/83792038)



最终需要4个文件：

| rabbitmq的Key          | 文件名                 | 说明                       |
| ---------------------- | ---------------------- | -------------------------- |
| ssl_options.cacertfile | ca_certificate.pem     | CA证书文件                 |
| ssl_options.certfile   | server_certificate.pem | 服务端证书文件             |
| ssl_options.keyfile    | server_key.pem         | 服务端私钥文件             |
| 无                     | client_key.p12         | 客户端证书文件，用于客户端 |



#### ②  配置Dockerfile

地址rabbitmq的Dockerfile文件

```dockerfile
#带有管理界面的rabbitmq
FROM rabbitmq:3.8.0-management-alpine
# 启动mqtt插件
RUN rabbitmq-plugins enable --offline rabbitmq_mqtt

COPY ./manually/testca/ca_certificate.pem /cert/ca_certificate.pem
COPY ./manually/server/server_certificate.pem /cert/server_certificate.pem
COPY ./manually/server/private_key.pem /cert/server_key.pem
```





#### ③  配置Compose文件

地址rabbitmq的Dockerfile文件

```yaml
  #rabbitmq
  rabbitmq:
    hostname: rabbitmq
    build: ./rabbitmq
    restart: always
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: fanhualei

      RABBITMQ_SSL_CACERTFILE: /cert/ca_certificate.pem
      RABBITMQ_SSL_CERTFILE: /cert/server_certificate.pem
      RABBITMQ_SSL_KEYFILE: /cert/server_key.pem
      #客户端不需要带证书
      RABBITMQ_SSL_FAIL_IF_NO_PEER_CERT: 'false'
      RABBITMQ_SSL_VERIFY: 'verify_none'

    volumes:
      - /etc/localtime:/etc/localtime:ro
      - ${DATA_PATH}/rabbitmq/data:/var/lib/rabbitmq
    ports:
      - "15672:15672"
      - "15671:15671"
      - "1883:1883"
      - "8883:8883"
```



#### ④  生成镜像并再次配置

这个镜像不包含Mqtt的SSL功能，所以要配置后重新启动。

```shell
docker-compose up -d

# 登录到容器中，追加配置
docker-compose exec rabbitmq ash
>cd /etc/rabbitmq/
# 追加mqtt.listeners.ssl.default
>vi rabbitmq.conf

# 重启容器
docker-compose restart rabbitmq
```



> rabbitmq.conf

```ini
loopback_users.guest = false
listeners.ssl.default = 5671
ssl_options.cacertfile = /cert/ca_certificate.pem
ssl_options.certfile = /cert/server_certificate.pem
ssl_options.fail_if_no_peer_cert = false
ssl_options.keyfile = /cert/server_key.pem
ssl_options.verify = verify_none
default_pass = fanhualei
default_user = guest
management.ssl.port = 15671
management.ssl.cacertfile = /cert/ca_certificate.pem
management.ssl.certfile = /cert/server_certificate.pem
management.ssl.fail_if_no_peer_cert = false
management.ssl.keyfile = /cert/server_key.pem
management.ssl.verify = verify_none

# 下面这一行是追加的。
mqtt.listeners.ssl.default = 8883

```



#### ⑤ mqttfx使用证书连接

通过这个配置连接服务器，并且发布一些数据

![alt](imgs/rabbit-mqttfx-ssl-ok-setting.png)



#### ⑥ 访问管理界面

这次一定要使用https . 我认为管理界面的证书，可以单独使用，与mqtt的证书不同。

地址rabbitmq的Dockerfile文件

https://192.168.1.179:15671/

![alt](imgs/rabbitmq-web-ssl-connected.png)







### 参考资料

* 网友的文档
  * [安装Nginx,配置反向代理,打开微信小程序测试MQTT连接](https://www.bilibili.com/video/av70119734/)
  * [RabbitMQ+Erlang+MQTT安装及配置](https://www.jianshu.com/p/9db463ab0ab0)
* 官网
  * [http://www.rabbitmq.com/mqtt.html](http://www.rabbitmq.com/mqtt.html)
  * [http://www.rabbitmq.com/web-mqtt.html](http://www.rabbitmq.com/web-mqtt.html)





### 3.9.5 配置SSL双向认证服务



### 3.9.6 配置SSL双向认证服务











## 3.10 证书制作过程

[参考网址](https://www.rabbitmq.com/ssl.html#manual-certificate-generation):本指南的这一部分说明了如何生成证书颁发机构，并使用它来生成和签名两个证书/密钥对，一个用于服务器，一个用于客户端库。请注意，可以[使用](https://www.rabbitmq.com/ssl.html#automated-certificate-generation)推荐的[现有工具](https://www.rabbitmq.com/ssl.html#automated-certificate-generation)使该过程[自动化](https://www.rabbitmq.com/ssl.html#automated-certificate-generation)。本部分适用于希望提高其对过程，OpenSSL命令行工具以及一些重要方面OpenSSL配置的了解的人员。



![alt](imgs/rabbitmq-ca-create.png)



### 3.10.1 证书颁发机构

有时候，使用SSL协议是自己内部服务器使用的，这时可以不必去找第三方权威的CA机构做证书，可以做自签证书（自己创建root CA（非权威））主要有以下三个步骤。



#### ①  创建工作目录

为证书颁发机构创建一个目录

```shell
mkdir testca
cd testca
mkdir certs private
chmod 700 private
echo 01 > serial
touch index.txt
```



#### ②  配置openssl.cnf文件

现在，在新创建的`testca` 目录中添加以下OpenSSL配置文件`openssl.cnf`：

[06.Openssl基本概念](https://www.cnblogs.com/aixiaoxiaoyu/p/8400036.html)



```ini
[ ca ]
default_ca = testca

[ testca ]

dir = .
#存放CA证书文件
certificate = $dir/ca_certificate.pem

#Openssl定义的用于以签发证书的文本数据库文件
database = $dir/index.txt

#存放CA指令签发生成新证书的目录
new_certs_dir = $dir/certs

#存放CA私钥的文件
private_key = $dir/private/ca_private_key.pem


serial = $dir/serial

#从当前CRL到下次CRL发布以天为单位的时间间隔 CRL证书作废
default_crl_days = 7
#签发证书的有效期，以天为单位
default_days = 365
default_md = sha256

#该字段的策略决定CA要求和处理证书请求提供的DN域各个参数值的规则
policy = testca_policy
#指定了生成自签名证书时要使用的证书扩展项字段
x509_extensions = certificate_extensions


[ testca_policy ]
commonName = supplied
stateOrProvinceName = optional
countryName = optional
emailAddress = optional
organizationName = optional
organizationalUnitName = optional
domainComponent = optional

[ certificate_extensions ]
basicConstraints = CA:false

[ req ]
default_bits = 2048
default_keyfile = ./private/ca_private_key.pem
default_md = sha256
prompt = yes
distinguished_name = root_ca_distinguished_name
x509_extensions = root_ca_extensions

[ root_ca_distinguished_name ]
commonName = hostname

[ root_ca_extensions ]
basicConstraints = CA:true
keyUsage = keyCertSign, cRLSign

[ client_ca_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature,keyEncipherment
extendedKeyUsage = 1.3.6.1.5.5.7.3.2

[ server_ca_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature,keyEncipherment
extendedKeyUsage = 1.3.6.1.5.5.7.3.1
```



#### ③  生成密钥和证书

接下来，我们需要生成测试证书颁发机构将使用的密钥和证书。仍在testca 目录中：

```shell
openssl req -x509 -config openssl.cnf -newkey rsa:2048 -days 365 \
    -out ca_certificate.pem -outform PEM -subj /CN=MyTestCA/ -nodes

#DER类型的格式，两者是一样的
openssl x509 -in ca_certificate.pem -out ca_certificate.cer -outform DER
```

这是生成测试证书颁发机构所需的全部。根证书位于`ca_certificate.pem中` ，也位于`testca / ca_certificate.cer`中。这两个文件包含相同的信息，但格式不同，PEM和DER。大多数软件使用前者，但是某些工具需要后者。

设置了证书颁发机构之后，我们现在需要为客户端和服务器生成私钥和证书。RabbitMQ代理使用PEM格式的证书和私钥。一些客户端库使用PEM格式，另一些则需要转换为其他格式（例如PKCS＃12）。

Java和.NET客户端使用称为PKCS＃12的证书格式和自定义证书存储。证书库包含客户端的证书和密钥。PKCS存储区通常受密码保护，因此必须提供密码。



### 3.10.2 服务器端

获取权威机构颁发的证书，

* 需要先得到私钥的key文件（.key）
* 然后使用私钥的key文件生成sign req 文件（.csr）或 req.pem文件
* 最后把csr文件发给权威机构，等待权威机构认证，认证成功后，会返回证书文件（.crt）。 

```shell
cd ..
ls
# => testca
mkdir server
cd server
#首先：生成私钥
openssl genrsa -out private_key.pem 2048
#然后使用私钥的key文件生成sign req 文件（.csr）或 req.pem文件
openssl req -new -key private_key.pem -out req.pem -outform PEM \
    -subj /CN=$(hostname)/O=server/ -nodes

#最后把csr文件(req.pem)发给权威机构，等待权威机构认证，认证成功后，会返回证书文件（.crt） 
cd ../testca
openssl ca -config openssl.cnf -in ../server/req.pem -out \
    ../server/server_certificate.pem -notext -batch -extensions server_ca_extensions

#Java和.NET客户端使用称为PKCS＃12的证书格式，这里进行转换
cd ../server
openssl pkcs12 -export -out server_certificate.p12 -in server_certificate.pem -inkey private_key.pem \
    -passout pass:MySecretPassword
```

### 3.10.3 客户端

原理同服务器端，因为要做双向认证，所以这里也需要生成

```shell
cd ..
ls
# => server testca
mkdir client
cd client
#首先：生成私钥
openssl genrsa -out private_key.pem 2048
#然后使用私钥的key文件生成sign req 文件（.csr）或 req.pem文件
openssl req -new -key private_key.pem -out req.pem -outform PEM \
    -subj /CN=$(hostname)/O=client/ -nodes
    
#最后把csr文件(req.pem)发给权威机构，等待权威机构认证，认证成功后，会返回证书文件（.crt）     
cd ../testca
openssl ca -config openssl.cnf -in ../client/req.pem -out \
    ../client/client_certificate.pem -notext -batch -extensions client_ca_extensions

#Java和.NET客户端使用称为PKCS＃12的证书格式，这里进行转换
cd ../client
openssl pkcs12 -export -out client_certificate.p12 -in client_certificate.pem -inkey private_key.pem \
    -passout pass:MySecretPassword
```









## 3.11 压力测试



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