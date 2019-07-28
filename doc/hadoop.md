# Hadoop



> 参考网址

* [官方网址](http://hadoop.apache.org/)
* [官方推荐版本](http://hadoop.apache.org/docs/stable/)





## 1：开始安装

当前官方推荐的是`2.9.2`

[官方参考文档](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html)



## 1.1：必要软件

* 必须安装Java™。

* 必须安装ssh并且必须运行sshd才能使用管理远程Hadoop守护进程的Hadoop脚本

> java安装





> ssh安装

```shell
# 安装ssh于rsync ,其中rsync是用来数据同步的
$ yum install ssh
$ yum install rsync


# 补充信息，如何查询是否安装过软件了

$ rpm -qa | grep ssh 
$ ps -ef | grep ssh
$ service sshd status 

```







