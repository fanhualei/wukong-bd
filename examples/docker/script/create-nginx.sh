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
hasContainer=$(docker ps -a -f name=${APPNAME} | grep ${APPNAME})
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

# 让这个容器可以随着服务器启动自动启动
docker update --restart=always  ${APPNAME}  > /dev/null


docker ps -a
# 得到一个测试文件
echo run access $(date "+%Y-%m-%d %H:%M:%S") >/data/${APPNAME}/www/index.html
curl 127.0.0.1:${PORT}