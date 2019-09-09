# Docker

如果你就想简单的使用，那么花10分钟看这个文档。[10分钟学会使用docker](https://github.com/fanhualei/wukong-framework/blob/master/reference/docker.md)

如果你想深入了解原理，那么看下面的文档。

[TOC]



## 介绍



### Docker优点

* 快速打包
* 控制多个版本
* 移植性高
* 标准化
* 隔离性与安全性





### 虚拟机与容器的区别

> Docker的缺点：

由于共享宿主机内核，只是进程级隔离，因此隔离性和稳定性不如虚拟机，容器具有一定权限访问宿主机内核，存在一定安全 隐患。 



>  Docker好处：

速度快，性能好，占用空间好



### 应用场景

* 标准化运行环境
  * 避免环境冲突，解决生产环境的标准化
* 自动化测试与持续集成
* 快速弹性扩展
* 微服务





## 安装



安装很简单，一行命令就能安装完毕了

```shell
## 安装
$ curl -sSL https://get.daocloud.io/docker | sh
$ docker -v
Docker version 19.03.2, build 6a30dfc

##修改docker镜像地址，官方的镜像库连接太慢，这里转到daocloud镜像库。 
$ curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://91c0cc1e.m.daocloud.io 

## 启动docker服务，并设置开机启动 
$ systemctl enable docker.service && service docker start
```



如果想知道原理，可以参考一下文档：

在百度搜索:`docker一键安装`

* [下面是我以前的安装步骤](https://github.com/fanhualei/wukong-framework/blob/master/reference/docker.md)
* [daocloud官方网址](http://get.daocloud.io/)
* [菜鸟docker安装](https://www.runoob.com/docker/centos-docker-install.html)
* [Docker - 加速镜像下载（使用DaoCloud镜像服务）](https://www.hangge.com/blog/cache/detail_2401.html)
* [Docker的学习--命令使用详解](https://www.cnblogs.com/CraryPrimitiveMan/p/4657835.html)

docker 被安装到``



## 镜像管理



### 什么是镜像？

简单说，Docker镜像是一个不包含Linux内核而又精简的Linux操作系统。



### 镜像从哪里来？

Docker Hub是由Docker公司负责维护的公共注册中心，包含大量的容器镜像，Docker工具默认从这个公共镜像库下载镜像。
`https://hub.docker.com/explore`
默认是国外的源，下载会慢，可以国内的源提供下载速度：
`curl -sSL https://get.daocloud.io/daotools/set_mirror.sh | sh -s http://91c0cc1e.m.daocloud.io` 



### 镜像工作原理？

[10张图带你深入理解Docker容器和镜像](http://dockone.io/article/783)

容器=镜像+可读文件层

当我们启动一个新的容器时，Docker会加载只读镜像，并在其之上添加一个读写层，并将镜像中的目录复制一份到
`/var/lib/docker/aufs/mnt/`容器ID为目录下，我们可以使用chroot进入此目录。如果运行中的容器修改一个已经存在的文件，
那么会将该文件从下面的只读层复制到读写层，只读层的这个文件就会覆盖，但还存在，这就实现了文件系统隔离，当删除容
器后，读写层的数据将会删除，只读镜像不变。



### 镜像文件存储结构？

docker相关文件存放在：`/var/lib/docker`目录下
![alt](imgs/docker-dir.png)



### 镜像常用命令

`docker --help | grep imge`

```
search 从服务器上查找镜像
pull   下载一个镜像
push   将镜像放到自己的私有仓库或者公共仓库
history 查看镜像的历史
images 查看本地镜像
commit 保存镜像
build  设置镜像创建时的变量
rmi    删除镜像
       https://www.runoob.com/docker/docker-build-command.html
export 将一个【容器】保存成tar文件
import 将一个tar文件生成镜像
save   将一个【镜像】保存成tar文件
load   将一个tar文件生成镜像
```



### 示例

下面例子是连续的，必须一步一步的操作。



#### 下载一个镜像，并启动容器:search+pull

```shell
# 查找镜像
$ docker search ubuntu
# 把镜像拉到本地
$ docker pull ubuntu
# 执行一个容器
$ docker run -itd --name test01 ubuntu
# 查看容器
$ docker ps
```



#### 删除容器，文件也被删除:rm

在容器中创建两个文件，删除容器，两个文件也消失了

```shell
# 进入一个容器
$ docker attach test01
$root@1e25fff09c94:/# cd home
$root@1e25fff09c94:/# touch a.txt
$root@1e25fff09c94:/# touoch b.txt

##docker退出容器，而不关闭容器： ctrl+q+p；

# 查看这个容器的某个目录,应该有a.txt与b.txt
$ docker exec test01 ls /home

# 删除docker
$ docker stop test01
$ docker rm test01

# 再运行，发现已经被删除了。
$ docker run -itd --name test01 ubuntu
$ docker exec test01 ls /home
```



#### 将容器保存成镜像:images+commit

这样就能保存容器的文件

用到：`images` ，`commit`

```shell
# 查看当前有几个镜像
$ docker images

# 修改容器的内容
$ docker exec test01 touch /home/a.txt
$ docker exec test01 touch /home/b.txt
$ docker exec test01 ls /home

# 将容器的内容保存成镜像
$ docker commit test01 ubuntu:self

# 查看当前有几个镜像
$ docker images

# 运行一个镜像
$ docker run -itd --name test01_self ubuntu:self

# 查看容器中是否包含了那两个文件
$ docker exec test01_self ls /home
```

![alt](imgs/docker-images.png)



#### 删除镜像:rmi

`rmi`：删除镜像

`rm`：删除容器

`-f` 表示强行删除



```shell
# 如果有容器在运行，那么删除镜像报错
$ docker rmi ubuntu:self

# 停止容器,还是不能删除
$ docker stop test01_self

# 只能先删除容器了
$ docker rm test01_self

# 再次删除
$ docker rmi ubuntu:self

# 查看当前有几个镜像
$ docker images
```



#### 导出导入:export+import

使用`export`将一个容器导出成tar文件

使用`import`将一个tar文件生成镜像文件

```shell
# 导出一个容器
$ docker export test01 > test01.tar

# 查看导出的文件，以及文件大小
$ ls 
$ du -sh test01.tar

# 导入成镜像
$ docker import  test01.tar ubuntu:self

# 查看当前有几个镜像
$ docker images

# 运行一个镜像,这样会报错
$ docker run -itd --name test01_self ubuntu:self

# 这个不报错
$ docker run -itd --name test01_self ubuntu:self /bin/bash

# 查看容器中是否包含了那两个文件
$ docker exec test01_self ls /home
```



#### 保存镜像到tar文件:save+load



```shell
$ docker save ubuntu:self > ubuntu_self.tar
$ docker load -i ubuntu_self.tar
```



## 容器管理



### 创建容器

使用 `docker run --help` 来看相关命令的使用方法

![alt](imgs/dcoker-command-option.png)



#### 提供标准输入：-i

* 可以使用 attach 进入容器
* --interactive                    Keep STDIN open even if not attached

#### 给一个伪终端：-t

* 有一个伪终端
* --tty                            Allocate a pseudo-TTY

#### 后台执行：-d

* 将容器放到后台运行
* --detach                         Run container in background and print container ID



#### 修改hosts：--add-host list

--add-host list                  Add a custom host-to-IP mapping (host:ip)

```shell
$ docker exec  teset01_self cat /etc/hosts
```

查看一个容器的IP地址

![alt](imgs/docker-exec-hosts.png)

```shell
docker run -itd --name test01_self01 --add-host abc:192.168.12.123 ubuntu:self /bin/bash
docker exec  test01_self01 cat /etc/hosts

```

![alt](imgs/docker-add-host.png)



#### 提供访问linux内核：--cap-add



####  将容器ID保存到文件中：--cidfile 



#### 添加一个设备到容器中：--device



#### 设置容器DNS：--dns

```shell
# 查看DNS
$ docker exec test01_self cat /etc/resolv.conf
$ docker run -itd --dns 6.6.6.6 ubuntu
```



#### 传入一个系统变量：-e

在容器中，可以通过echo来得到变量

```shell
$ docker run -itd -e java_version:123456 ubuntu
```



#### 暴露一个端口：--expose

可以是一个范围

```shell
$ docker run -itd --expose 80 ubuntu
```



#### 设定容器的主机名：-h

```shell
$ docker run -h myComputer ubuntu
```



#### 指定容器的IP地址：--net

如果一台机器上有很多容器，同时并不想让这些容器相互访问，可以通过划分IP端来隔离

这个使用了docker的网络分配方法，详细内容见后面

```shell
# 创建一个自网段，并且指定IP地址
$ docker network create --subnet=10.0.0.0/16 network_10
$ docker network ls
$ docker run -itd --net=network_10 --ip 10.0.0123 utuntu
# 查看这个容器的IP
$ docker inspect 容器名
```



#### 通过机器名关联不同容器：--link



#### 日志收集：--log-driver

docker默认将日志保存在本地。

通过设置，可以将日志发送到统一的日志收集系统便于分析。

--log-opt日志收集选项



#### 挂载分区：--mount



#### 可以执行大内存程序：--oom-kell-disable

例如执行spark程序



#### 将宿主机器上端口转发到容器：-p

与大写P与小写p，功能不一样

```shell
# 创建一个自网段，并且指定IP地址

$ docker run -itd -p 8888:80 utuntu

```

大写P会将所有端口，都映射到宿主机器的随机端口



#### 自动重启容器：--restart

如果容器挂掉，可以自动重启3次，如果还没有启动，就不启动了。

```shell


$ docker run -itd --restart on-failure:3 utuntu

```



#### 去掉操作系统的最大限制：--ulimit

例如spark要打开很多文件，开启很多线程，所以要突破linux的限制

```shell
$ docker run -itd --ulimit nproc=10240 --ulimit nofile=12400 utuntu
# ulimit -a 查看当前系统设置
```

![alt](imgs/docker-unlimited.png)



#### 将容器的目录挂载到宿主机：-v



#### 共享目录：--volumes-from



#### 进入容器后的默认目录：-w



#### CPU限制：--cpu-period

--cpu-period：周期

--cpu-quota :多长时间

-c：各个容器分配cpu的权重值

-cpuset-cpus : 指定几核



#### 限制磁盘读取速度：--device--read-bps

--device--read-bps：字节数

--device--write-bps

--device--read-iops：

--device--write-iops



#### 限制内存：-m



```shell
$ docker run -itd -m 10240000 utuntu
```

--memory-swap

--memory-swappiness

限制内存交换区



#### 限制使用磁盘空间：--storag-opt

指定容器可以使用的最大空间数



### 容器常用管理命令

```shell
# 删除所有容器,如果你做测试，里面有很多容器，可以全部删除，这个命令慎用
docker rm -f $(docker ps -q -a)
```

![alt](imgs/docker-command-content.png)



#### 基本命令

##### 显示列表：ps

* -a 显示没有启动的
* -l  显示最后一条
* -q 只显示编号
* -s  显示容器大小

![alt](imgs/docker-ps-options.png)



##### 进入一个容器：attach



##### 删除容器：rm



##### 启动容器：start



##### 停止容器：stop



##### 重新命令容器：rename

以前随机的容器名，可以从新命令一个



##### 暂停容器：kill pause unpase



### 容器数据持久化

一个容器被删除后，数据也会被删除，那么怎么才能将数据保存下来呢？



### 搭建一个平台例子