# 使用su

```shell
# 清理一下镜像
docker rmi my-tomcat-alpine:v3

# 生成镜像
docker build -t my-tomcat-alpine:v3 .

# 运行容器
docker run -dit --name my-app-v3 my-tomcat-alpine:v3

# 测试容器
curl $(docker inspect -f '{{.NetworkSettings.IPAddress }}' my-app-v3):8080

```


