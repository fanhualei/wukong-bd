FROM alpine

MAINTAINER fanhualei


#使用阿里镜像,安装 openjdk
RUN set -x;\
        sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories;\
        apk add openjdk8;
        

#安装tomcat
ADD apache-tomcat-9.0.24.tar.gz /opt
RUN set -x;\
        ln -s /opt/apache-tomcat-9.0.24/  /opt/tomcat; \
        ln -s /opt/apache-tomcat-9.0.24/bin/catalina.sh /bin/tomcat; \
        mkdir /opt/webapp;
    

#初始化文件
COPY index.jsp /opt/webapp/ 
COPY server.xml /opt/tomcat/conf/

EXPOSE 8080

CMD ["tomcat", "run"]


