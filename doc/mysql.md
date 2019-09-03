#### 安装MySql

在CentOS7中默认安装有MariaDB，这个是MySQL的分支，尽量安装官方的mysql

> 安装

```shell
# 查看是否有msql 如果有就删除
$ rpm -qa | grep mysql
$ wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
$ yum install mysql80-community-release-el7-3.noarch.rpm
$ yum -y install mysql-community-server
```

> 启动

```shell
$ service mysqld status
$ service mysqld start
$ service mysqld stop

# 得到默认的密码 qg0hBFwD56-v
$ grep 'temporary password' /var/log/mysqld.log

# 一定要立即修改密码
$ mysql -uroot -p

# 设置新密码
$mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'Root@mysql-1';

# 重新登录
$ mysql -uroot -pRoot@mysql-1

$mysql>set global validate_password.policy=0;
$mysql>set global validate_password.length=3;
$mysql>show global variables like '%validate_password%';
# 退出mysql后

# 设置mysql 启动后自动启动
$ systemctl enable mysql
```

- [CentOS7安装MySQL](https://www.cnblogs.com/nicknailo/articles/8563737.html)



> mysql安装的位置

```shell
# 执行命令的路径
 /usr/sbin/mysqld
 
# 文件存储路径 
  /var/lib/mysql
  
# 配置文件
 /etc/my.cnf
```



