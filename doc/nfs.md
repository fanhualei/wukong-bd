# NFS共享目录使用

假设服务器：192.168.1.185 .  客服端是192.168.1.186

为了方便，可以提前把防火墙给关闭了。



# 1. 服务器配置



## 第一步、安装 NFS 软件包

```shell
yum install -y nfs-utils
```



## 第二步、创建目录，并分配权限

```shell
mkdir -p /home/nfs
#将nfsnobody分配给这个目录，不然客户端只能看，不能写
chown nfsnobody.nfsnobody /home/nfs
```



## 第三步、编辑exports文件

编辑exports文件，添加那些客户端可以访问

```
vim /etc/exports
/home/nfs/ 192.168.1.0/24(rw,sync,fsid=0)
```

同192.168.1.0/24一个网络号的主机可以挂载NFS服务器上的/home/nfs/目录到自己的文件系统中
rw表示可读写；sync表示同步写，fsid=0表示将/data找个目录包装成根目录



> 参数说明

```
ro：目录只读
rw：目录读写
sync：将数据同步写入内存缓冲区与磁盘中，效率低，但可以保证数据的一致性
async：将数据先保存在内存缓冲区中，必要时才写入磁盘
all_squash：将远程访问的所有普通用户及所属组都映射为匿名用户或用户组(nfsnobody)
no_all_squash：与all_squash取反(默认设置)
root_squash：将root用户及所属组都映射为匿名用户或用户组(默认设置)
no_root_squash：与rootsquash取反
anonuid=xxx：将远程访问的所有用户都映射为匿名用户，并指定该用户为本地用户(UID=xxx)
anongid=xxx：将远程访问的所有用户组都映射为匿名用户组账户
```



## 第四步、确保nfs服务启动

```shell
#先为rpcbind和nfs做开机启动：(必须先启动rpcbind服务)
systemctl enable rpcbind.service
systemctl enable nfs-server.service

#然后分别启动rpcbind和nfs服务：
systemctl start rpcbind.service
systemctl start nfs-server.service

#确认NFS服务器启动成功：
rpcinfo -p

```



## 第五步、使配置生效

```shell
#检查 NFS 服务器是否挂载我们想共享的目录 /home/nfs/：
exportfs -r

#使配置生效
exportfs
```





# 2.客户端配置



## 第一步、安装 NFS 软件包

```shell
yum install -y nfs-utils
```



## 第二步、确保rpcbind服务启动

centos7默认是启动的。

```shell
systemctl status rpcbind.service
systemctl enable rpcbind.service
systemctl start rpcbind.service
```

注意：客户端不需要启动nfs服务



## 第三步、测试是否联通

```shell
showmount -e 192.168.1.185
```



## 第四步、挂载

```shell
mkdir /home/nfs
mount -t nfs 192.168.1.185:/home/nfs /home/nfs
```



## 第五步、测试

从客户端添加一个软件，看看服务器端能不能看到

```shell
cd /home/nfs
touch c.txt
```

然后跳转到服务器端看



> 参考网址

* [Centos7安装配置NFS服务和挂载教程(推荐)](https://www.jb51.net/article/126091.htm)