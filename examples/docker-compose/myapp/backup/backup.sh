


#!/bin/sh

#database info
DB_USER="root"
DB_PASS=$MYSQL_ROOT_PASSWORD
DB_HOST=$MYSQL_HOST
DB_NAME=$MYSQL_DB

# 定义在容器内部的路径
ADM_DIR="/myapp/mysqladm/"            #the backup.sh path
BCK_DIR="/myapp/mysqladm/files"    #the backup file directory

if [ ! -d $ADM_DIR  ];then
  mkdir $ADM_DIR
fi

if [ ! -d $BCK_DIR  ];then
  mkdir $BCK_DIR
fi


DATE=`date +%F`
#备份数据库的文件名
OUT_SQL="${DB_NAME}-${DATE}.sql"

#最终保存的数据库备份文件名
TAR_SQL="${DB_NAME}-${DATE}.tar"

mysqldump --opt -u$DB_USER -p$DB_PASS -h$DB_HOST $DB_NAME > $BCK_DIR/$OUT_SQL

#========================================
#DAYS=15代表删除15天前的备份，即只保留最近15天的备份
DAYS=15

#进入备份存放目录
cd $BCK_DIR
tar -czf $TAR_SQL ./$OUT_SQL

#删除.sql格式的备份文件
rm $OUT_SQL


#删除15天前的备份文件(注意：{} \;中间有空格)
find $BCK_DIR -name "${DB_NAME}-*" -type f -mtime +$DAYS -exec rm {} \;














