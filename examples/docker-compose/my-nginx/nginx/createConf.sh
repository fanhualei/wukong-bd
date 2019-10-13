#!/bin/bash

# 运行一个环境，来复制默认的conf文件,然后删除
docker run --name my-nginx-temp  -d nginx:alpine 
docker cp my-nginx-temp:/etc/nginx/nginx.conf ./ 
docker rm -f my-nginx-temp ;

lineNum=$(grep -nr 'include /etc/nginx/conf.d/\*.conf;'  ./nginx.conf  | awk -F ':' '{print $1}') 
numi=${lineNum}i 
sed -i ${numi}"   include /etc/nginx/myconf/*.conf;" ./nginx.conf 
sed -i ${numi}"   server_tokens off;" ./nginx.conf 



