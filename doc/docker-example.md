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



## 2.2. 定义数据目录(忽略)

如果是使用本地目录，docker-compose会自动建立，**可以不执行下面的代码**。

如果使用nfs网络存储，需要重新挂载网络存储。

```shell
#如果是使用本地目录，docker-compose会自动建立，可以不执行下面的代码。
mkdir /data/myapp
cd /data/myapp
mkdir rabbitmq mysql redis tomcat-hz tomcat-wx
```





# 3. 具体实现

①②③④⑤⑥⑦⑧⑨

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



### 3.2.1 Tomcat

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
#关于修改配置，这里省略
COPY server.xml /usr/local/tomcat/conf
```





### 3.2.2 Rabbitmq

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



#### ② 参考资料

RabbitMQ 已经有一些自带管理插件的镜像。用这些镜像创建的容器实例可以直接使用默认的 15672 端口访问，默认账号密码是`guest/guest`：



映射端口

- `15672` 是rabbitmq management管理界面默认访问端口
- `5672` 是amqp默认端口
- `1883` 是mqtt tcp协议默认端口
- `15675` 是web_mqtt ws协议默认端口



> 相关文档

* [RabbitMQ手册之rabbitmq-plugins](https://www.jianshu.com/p/0ff7c2e5c7cb)
* [Docker安装RabbitMQ配置MQTT](https://blog.csdn.net/hololens/article/details/80059991)



#### ③ 遗留问题

* 如何进行持久化



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
    image: tomcat:9.0.20-jre8-alpine
    #容器的映射端口
    ports:
      - 8080:20180    
    #定义挂载点
    volumes:
      - ${DATA_PATH}/tomcat-wx/webapps:/usr/local/tomcat/webapps
      - ${DATA_PATH}/tomcat-wx/logs:/usr/local/tomcat/logs
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
    environment:
      MYSQL_ROOT_PASSWORD: mysql@root
      
  #redis
  redis:
    hostname: redis
    image: redis:5.0.6-alpine
    restart: always
    # 开启持久化，并设置密码
    command: redis-server --appendonly yes --requirepass "redis123"
    volumes:
      - ${DATA_PATH}/redis/data:/data
      
  #rabbitmq
  rabbitmq:
    hostname: rabbitmq
    build: ./rabbitmq
    restart: always  
```



## 3.4 执行Compose

```shell
docker-compose up -d
```







## 3.5 进行测试



### ① 常用命令

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











# 参考文档

* 基本使用
  * [docker和docker-compose的前后端项目部署（含MySQL，Redis和RabbitMQ）](https://www.jianshu.com/p/528fa4d62ace)
  * [docker-compose.yml部署redis，mysql，tomcat，jenkins，activemq](https://blog.csdn.net/qq_40460909/article/details/84672492)
  * [Docker Compose 搭建Mysql主从复制集群](https://blog.csdn.net/u012562943/article/details/86589834)
* Tomcat使用
  * [tomcat常用配置详解和优化方法](https://www.cnblogs.com/xuwc/p/8523681.html)