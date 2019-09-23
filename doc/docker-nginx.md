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







> 参考文档

* [docker官方文档](https://hub.docker.com/_/nginx)
  * gihub上的文档：https://github.com/docker-library/docs
* [菜鸟Docker 安装 Nginx](https://www.runoob.com/docker/docker-install-nginx.html)

* [Docker nginx安装与配置挂载](https://blog.csdn.net/qq_26641781/article/details/80883192)