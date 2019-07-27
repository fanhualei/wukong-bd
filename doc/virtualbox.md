# VirtualBox虚拟机安装



我是在win10机器上安装centos的。



## 1：程序安装

`virtualbox`安装起来步复杂，建议安装到`d`盘。

`centos` 安装起来也不复杂，按照向导安装就可以了。



> 下载地址

* [VirtualBox下载地址](https://www.virtualbox.org/)
* [centos下载](https://www.centos.org/)



> 参考文档

* [win10安装oracle vm virtualbox，并安装centos7详细安装记录](https://blog.csdn.net/qq_37316272/article/details/87691835)



## 2：配置网络

目标是`虚拟机`可以访问网络，并且`主机`可以访问虚拟机。网上的教程写的太乱，实际很简单。



### 2.1：默认安装后就能访问外网

按照默认的安装，`虚拟机`可以访问外网，但是`主机`访问虚拟机很麻烦。



> centos01机器安装后，默认的就是：网络地址转换(NAT)

![alt](imgs/net-ant-01.png)



> 用虚拟机ping外网

ping 百度，ping 我自己的win10系统，都可以ping通。

![alt](imgs/net-ant-02-ping.png)



> 查看当前的网卡信息

使用ifconfig，如果找不到这个命令，网上搜如何安装ifconfig.

这里生成两个网卡配置`enp0s3`与`lo`

![alt](imgs/net-ant-02-ifconfig.png)



> 主机ping 不同虚拟机

ping 上面的ip是不行了。那么使用ssh登录页是不行的。

![alt](imgs/net-ant-03-ping-no.png)



> 怎么使用ssh登录呢？

使用端口转发功能

![alt](imgs/net-ant-04-relay-01.png)



新追加一条规则，由于我里面只有一个虚拟机，所以只用转发一条就可以了。



![alt](imgs/net-ant-04-relay-02.png)



使用putty来访问

![alt](imgs/net-ant-05-putty-01.png)



![alt](imgs/net-ant-05-putty-02.png)





### 2.2：优化网络配置

如果你感觉转发不爽，也可以使用直连的方式，当时刚才的`ant`模式不能删除，不然访问不了外网。

一个服务器可以有多个网卡，当然也可以虚拟出来多个网卡。



> 设置host only 模式

选中起用第二个网卡

![alt](imgs/net-hostonly-01-seting.png)



> 登录虚拟主机看看本地IP

这时候，看到多了一个网卡，`enp0s8`，这个就是刚才配出来的网卡

![alt](imgs/net-hostonly-02-ifconfig.png)



> 不用转发登录虚拟主机

在主机上`ping`虚拟主机，可以ping通，那么也可以通过`putty`来远程登录到虚拟主机

![alt](imgs/net-hostonly-03-ping.png)



> 固定IP 

现在都是动态IP，有可能重启后，就找不到这个IP了，这样就不好登录了，所以可以将`192.168.56.102`这个IP固定了。 可以这么来设置

我发现没有找到那个网卡配置文件，所以就没有去配置，网上有人说新建立一个文件就行，我也没有新建立文件。

参考：[Centos系统如何设置固定IP](https://jingyan.baidu.com/article/6c67b1d65127692787bb1ef3.html)



## 3：使用VirtualBox





