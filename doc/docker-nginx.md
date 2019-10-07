# Docker Nginx的使用



# 1. 快速使用

有时候要练习一个docker命令或者k8s时，就简单的使用一下。但是在正式环境中，应该安装nginx的正式版

```shell
# 下载镜像
docker pull nginx:alpine
docker images

# 运行一个
docker run --name my-nginx  -p 18080:80 -v /test/content:/usr/share/nginx/html -d nginx:alpine


# 查看容器ip
docker inspect -f {{.NetworkSettings.IPAddress}} my-nginx

# 写一个数据进去
echo hello nginx,I am james  >> /test/content/index.html

# 查看的网页
curl $(docker inspect -f {{.NetworkSettings.IPAddress}} my-nginx)

#登录到容器
docker exec -it my-nginx /bin/sh
> exit

# 清空内容
docker rm -f my-nginx
rm -rf /test/content
```







# 2. 生产环境中使用

参考文档：[传统Ngins的安装](https://github.com/fanhualei/wukong-framework/blob/master/reference/gitlab_apache.md)



```shell
# 创建要保存的目录
rm -rf /data/my-nginx
mkdir -p /data/my-nginx/www /data/my-nginx/logs /data/my-nginx/conf

# 运行一个环境，来复制默认的conf文件,然后删除
docker run --name my-nginx-temp  -d nginx:alpine
docker cp my-nginx-temp:/etc/nginx/ /data/my-nginx/conf
docker rm -f my-nginx-temp

# 安全配置
mkdir /data/my-nginx/conf/nginx/myconf

lineNum=$(grep -nr 'include /etc/nginx/conf.d/\*.conf;'  /data/my-nginx/conf/nginx/nginx.conf  | awk -F ':' '{print $1}')
numi=${lineNum}i
sed -i ${numi}"include /etc/nginx/myconf/*.conf;" /data/my-nginx/conf/nginx/nginx.conf
sed -i ${numi}"server_tokens off;" /data/my-nginx/conf/nginx/nginx.conf


# 运行一个正式的文件
docker run -d -p 80:80 --name my-nginx -v /data/my-nginx/www:/usr/share/nginx/html -v /data/my-nginx/conf/nginx:/etc/nginx -v /data/my-nginx/logs:/var/log/nginx nginx:alpine




docker ps -a
# 得到一个测试文件
echo hello world $(date "+%Y-%m-%d %H:%M:%S") >/data/my-nginx/www/index.html
curl 127.0.0.1
```



> 也可以使用下面的脚本来执行

下面的脚本是生成了一个简单的nginx应用

```shell
#!/bin/bash

#create nginx sh

echo -e  "command example: create-nginx my-nginx 80 \n"
 

APPNAME=my-nginx
PORT=80

if [ $# -eq 0 ];
then
    echo "please input appname"
    exit
fi

if [ $# -eq 1 ];
then
    echo $1
fi

if [ $# -ge 2 ];
then
    APPNAME=$1  
    PORT=$2
fi

#echo $APPNAME  $PORT

# 判断端口是否被使用
hasPort=$(netstat -tulpn | grep :${PORT})
if [ -n "$hasPort"  ];
then
	echo "error:port ${PORT} has used,please set other port"
    netstat -tulpn | grep :${PORT}
    exit
fi

# 判断目录是否存在
if [  -d "/data/${APPNAME}" ];then
	echo "error:/data/${APPNAME} is exist"
	echo "you will check the dir ,then can delete for run: rm -rf /data/${APPNAME}"
	exit
fi

# 判断docker 是否存在
hasContainer=$(docker ps -f name=${APPNAME} | grep ${APPNAME})
if [ -n "$hasContainer"  ];
then
	echo "error:container ${APPNAME} has exist,please set other appname"
    docker ps -f name=${APPNAME}
    exit
fi


# 创建要保存的目录
rm -rf /data/${APPNAME}
mkdir -p /data/${APPNAME}/www /data/${APPNAME}/logs /data/${APPNAME}/conf

# 运行一个环境，来复制默认的conf文件,然后删除
docker run --name ${APPNAME}-temp  -d nginx:alpine  > /dev/null
docker cp ${APPNAME}-temp:/etc/nginx/ /data/${APPNAME}/conf
docker rm -f ${APPNAME}-temp > /dev/null

# 安全配置
mkdir /data/${APPNAME}/conf/nginx/myconf


lineNum=$(grep -nr 'include /etc/nginx/conf.d/\*.conf;'  /data/${APPNAME}/conf/nginx/nginx.conf  | awk -F ':' '{print $1}')
numi=${lineNum}i
sed -i ${numi}"include /etc/nginx/myconf/*.conf;" /data/${APPNAME}/conf/nginx/nginx.conf
sed -i ${numi}"server_tokens off;" /data/${APPNAME}/conf/nginx/nginx.conf


# 运行一个正式的文件
docker run -d -p ${PORT}:80 --name ${APPNAME} -v /data/${APPNAME}/www:/usr/share/nginx/html -v /data/${APPNAME}/conf/nginx:/etc/nginx -v /data/${APPNAME}/logs:/var/log/nginx nginx:alpine


docker ps -a
# 得到一个测试文件
echo run access $(date "+%Y-%m-%d %H:%M:%S") >/data/${APPNAME}/www/index.html
curl 127.0.0.1:${PORT}
```











## 2.1 要解决的问题



* 数据持久化
  * www目录
  * logs目录
  * conf文件
* 端口映射
* https配置
* Nginx集群管理



## 2.2 安装

nginx官方docker地址：https://hub.docker.com/_/nginx

我选择安装`alpine`版本













# 参考文档

* [docker官方文档](https://hub.docker.com/_/nginx)
  * gihub上的文档：https://github.com/docker-library/docs
* [菜鸟Docker 安装 Nginx](https://www.runoob.com/docker/docker-install-nginx.html)

* [Docker nginx安装与配置挂载](https://blog.csdn.net/qq_26641781/article/details/80883192)