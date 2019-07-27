# wukong-bd



# Linux 基本使用

以centerOS为准



### 网络配置

​	
```
 	1、ip配置
		-》ifconfig命令，查看网卡信息（ip）
		-》修改ip配置文件
			- vi /etc/sysconfig/network-scripts/ifcfg-eth0
			- IPADDR=192.168.109.51
			- NETMASK=255.255.255.0
			- GATEWAY=192.168.109.2
			- DNS=192.168.109.2
	2、配置域名
		-》hostname 查看域名信息
		-》临时修改 hostname bigdata22.ibeifeng.com
		-》永久修改 vi /etc/sysconfig/network
	3、配置网络映射
		-》vi /etc/hosts
		-》192.168.109.51 bigdata22.ibeifeng.com   格式：ip + 主机名
```

### 命令
```
	1、格式
		-》命令 [-选项] [参数]
	2、pwd
		-》作用：可以打印出当前所在的绝对路径
	

	3、clear 
		-》清屏
		-》快捷键：ctrl + l
	4、ls
		-》列出目录结构，全称list
		-》ls /
		-》ls -l /  
			- l代表以列表（详细信息）形式查看， 全屏long
		-》ls -lh /
			- h代表human，以kb的方式展示文件大小
		-》ls -ld /dev
			- d代表只查看指定的目录信息
	5、cd 
		-》移动到指定目录
		-》cd /etc/abrt/
		-》cd 没有跟任何参数，那就默认移动到家目录
		-》cd .. 反回上一级目录
	6、mkdir
		-》创建目录
		-》mkdir -p dir/dir2
			- p选项可以创建dir2的同时创建dir目录（前提，dir目录不存在）
	7、touch
		-》创建文件
		-》touch somefile.txt
	8、rmdir
		-》删除空文件夹（注意，只能删除空目录）
		-》rmdir test_dir/
	9、rm - remove
		-》删除目录或文件
		-》rm -r dir/
			- r代表删除的是目录，如果删除的是目录，必须要加
		-》rm -rf package/
			- f代表确认删除
	10、cp - copy
		-》拷贝目录或文件
		-》 cp /etc/passwd .
			- .代表的当前目录
	11、mv - move
		-》剪切
		-》mv passwd package/
		-》还可以对文件进行重命名
		-》mv passwd passwd2
	12、查看文件命令
		-》cat /etc/passwd
		-》more /etc/services 查看多行文件，支持翻页，逐行查看，但不支持向上翻页回看
		-》less /etc/services 查看多行文件，支持翻页，逐行查看，向上或向下翻页查看
		-》head -n /etc/services 查看文件头n行，默认是10
		-》tail -n /etc/services 查看文件后n行，默认是10
		-》tail -f 加-f选项可以查看滚动的日志文件
```
### 用户和用户组管理
```
	1、用户配置文件位置
		-》cat /etc/passwd
	2、root:x:0:0:root:/root:/bin/bash
		-》root 代表用户名
		-》x 密码标识，代表有密码
		-》0 UID user id
		-》0 GID group id
		-》root 别名
		-》/root 家目录
		-》/bin/bash 用户使用的shell
	3、用户组配置文件位置
		-》cat /etc/group
	4、添加用户
		-》useradd 用户名
	5、删除用户
		-》userdel -r 用户名
			- r代表删除用户的同时，也删除home目录下的对应用户目录
	6、用户切换
		-》su - 用户名  切记，一定不要忘记写 - 横线
	7、添加和删除用户组
		-》添加 groupadd 组名
		-》删除 groupdel 组名
```

### 权限管理
```
	1、drwxr-xr-x.  3 root root   4096 Apr 14  2018 abrt
		-》第一列代表用户对文件的操作权限，第2列文件访问次数，第3列文件所有者，
		   第4列所属组，第5列大小，第6列文件的创建日期，第7列文件名
		-》d代表目录，-代表文件，l代表链接文件
		-》r代表读，w代表写，x代表执行，-没有这个权限
		-》rwx r-x r-x 每三个字符为一组，一共分为三组
			- 第一组是这个文件的所有者
			- 第二组是这个文件的所属组
			- 第三组代表其他人
	2、修改文件权限
		-》chmod命令只有root用户或文件的所有者才可以使用
		-》chmod u-x package/ 
			- u（user）代表所有者
			- g（group）代表所属组
			- o（other）代表其他人
			- a（all）代表所有用户
			- 减号（-）是去掉权限
			- 加好（+）增加权限
	3、数字表示
		-》r=4，w=2， x=1
		-》rwxr-xr-x   用数字表示 755
	4、读写执行权限对目录和文件的意义
		-》目录
			- 读 ls
			- 写 touch mkdir rm rmdir
			- 执行 cd
		-》文件
			- 读 cat more less head tail
			- 写 vi、vim
			- 执行 脚本，shell
	5、修改文件的所有者和所属组
		-》chown和chgrp只有root用户才有执行权限
		-》chown 用户名 要修改的文件 
		-》chgrp 用户名 要修改的文件
```

### vi编辑器
```
	1、命令模式操作
		-》删除：在命令模式按dd删除一行，dd还有剪切功能
		-》撤销：在命令模式按u
		-》复制：在命令模式按yy
			- 多行复制，先按下要复制的行数，然后按yy
		-》粘贴：在命令模式按p
		-》shift+a移动到行的末尾，并进入插入模式
		-》按大写的G移动到文件末尾
		-》按小写的gg移动到文件的开头
		-》大写的ZZ保存并退出
	2、插入（编辑）模式操作
		-》按i进入编辑模式
	3、最后行模式操作
		-》q 退出
		-》wq 保存并退出
		-》q！或wq！，！强制退出
		-》set nu 或 set number显示行号
		-》1,$s/nologin/666/g
			- 1起始行号
			- $最后一行，这里指定行号
			- s替换
			- g全局替换，把所有出现的nologin全部替换
```
### find命令
```
	-》格式：find 所搜范围 -name init 所搜名为init的文件
			 find 所搜范围 -iname init 搜索关键字可以是大写，也可以是小写
	-》linux文件大小计量单位：块=512bytes 1kb=1024bytes 块=0.5kb
		- 100MB 1mb=1024kb 100mb=102400kb 204800
	-》find / -size +204800   根据大小查找文件
	-》根据文件类型查找 -type  -d目录 -f文件 
		- find / -name init* -a -type d
		- a是and 连接符
	-》find /root -name test*
	   find: paths must precede expression: testfile
	   Usage: find [-H] [-L] [-P] [-Olevel] [-D help|tree|search|stat|rates|opt|exec] [path...] [expression]
	   搜索的时候报这个错误，关键字就添加‘’引号
	   正确命令：find /root -name 'test*'
```

### man帮助命令
```
	1、查看命令
	2、查看配置文件的帮助信息
```

