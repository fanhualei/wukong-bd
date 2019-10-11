

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


