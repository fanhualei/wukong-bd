

FROM python:3.7.4-alpine3.10

#安装make openssl  libssl-dev 下载代码，并且解压到根目录下
RUN apk add --update --no-cache make openssl unzip \
    && wget https://github.com/michaelklishin/tls-gen/archive/master.zip \
    && unzip -d / master.zip


#  守护进程，不然程序自动退出
CMD ["/bin/sh","-c","while true; do sleep 3; done"]




