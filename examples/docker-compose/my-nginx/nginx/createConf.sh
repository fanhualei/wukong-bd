




#!/bin/bash

rm nginx.conf
# 运行一个环境，来复制默认的conf文件,然后删除
docker run --name my-nginx-temp  -d nginx:alpine 
docker cp my-nginx-temp:/etc/nginx/nginx.conf ./ 
docker cp my-nginx-temp:/etc/nginx/conf.d/default.conf ./conf.d 
docker rm -f my-nginx-temp ;

# 去掉版本，添加html配置目录
lineNum=$(grep -nr 'include /etc/nginx/conf.d/\*.conf;'  ./nginx.conf  | awk -F ':' '{print $1}') 
numi=${lineNum}i 
sed -i ${numi}"server_tokens off;" ./nginx.conf 


# 添加stream配置目录
lineNum=$(grep -nr 'http {'  ./nginx.conf  | awk -F ':' '{print $1}') 
numi=${lineNum}i 

sed -i ${numi}"#-------------- " ./nginx.conf 
sed -i ${numi}"}" ./nginx.conf 
sed -i ${numi}"include /etc/nginx/conf-stream.d/*.conf;" ./nginx.conf 
sed -i ${numi}"stream {" ./nginx.conf 
sed -i ${numi}"# fanhladd " ./nginx.conf 


