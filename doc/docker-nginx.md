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



### ①  手工安装

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



### ②  自动安装



下面的脚本是生成了一个简单的nginx应用

```shell
# 这是一个自动生成的脚本
curl -sSL https://raw.githubusercontent.com/fanhualei/wukong-bd/master/examples/docker/script/create-nginx.sh | bash /dev/stdin my-nginx 80
```











# 参考文档

* [docker官方文档](https://hub.docker.com/_/nginx)
  * gihub上的文档：https://github.com/docker-library/docs
* [菜鸟Docker 安装 Nginx](https://www.runoob.com/docker/docker-install-nginx.html)

* [Docker nginx安装与配置挂载](https://blog.csdn.net/qq_26641781/article/details/80883192)