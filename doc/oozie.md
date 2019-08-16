# oozie使用



## 1. 工作流调度框架

* crontab 
* azkaban
  * 轻量化，比较灵活，界面好看
* oozie
  * 比较笨重，与hadoop结合比较好
* zeus
  * 阿里的





## 2. oozie基本概念

控制节点：开始、结束、合并、分开

动作节点：mr、email、等执行命令







### 2.1. 初识oozie

[Oozie 介绍](https://www.jianshu.com/p/97893bfc94a4)

wordcount的例子

![img](http://oozie.apache.org/docs/5.1.0/DG_Overview.png)

```xml
<workflow-app name='wordcount-wf' xmlns="uri:oozie:workflow:0.1">
    <start to='wordcount'/>
    <action name='wordcount'>
        <map-reduce>
            <job-tracker>${jobTracker}</job-tracker>
            <name-node>${nameNode}</name-node>
            <configuration>
                <property>
                    <name>mapred.mapper.class</name>
                    <value>org.myorg.WordCount.Map</value>
                </property>
                <property>
                    <name>mapred.reducer.class</name>
                    <value>org.myorg.WordCount.Reduce</value>
                </property>
                <property>
                    <name>mapred.input.dir</name>
                    <value>${inputDir}</value>
                </property>
                <property>
                    <name>mapred.output.dir</name>
                    <value>${outputDir}</value>
                </property>
            </configuration>
        </map-reduce>
        <ok to='end'/>
        <error to='end'/>
    </action>
    <kill name='kill'>
        <message>Something went wrong: ${wf:errorCode('wordcount')}</message>
    </kill/>
    <end name='end'/>
</workflow-app>
```



### 2.2. 三种执行方式



#### 2.2.1. 工作流



![alt](https://upload-images.jianshu.io/upload_images/4176128-62782054ae91bb8a.png?imageMogr2/auto-orient/)



#### 2.2.2. 定时任务

![alt](https://upload-images.jianshu.io/upload_images/4176128-ef061004ca302cfc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



#### 2.2.3. 捆绑型

不常用

![alt](https://upload-images.jianshu.io/upload_images/4176128-e4fa06121ee68c95.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/689/format/webp)



### 2.3. oozie架构图

![alt](https://upload-images.jianshu.io/upload_images/4176128-f66c6110e531e11d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)



### 2.4. oozie的组件

![alt](https://upload-images.jianshu.io/upload_images/4176128-cbe9e9980d533bd4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000/format/webp)





## 3. 编译与安装

* [官方安装指导](http://oozie.apache.org/docs/5.1.0/DG_QuickStart.html)
* [oozie 5.1.0编译安装](https://www.jianshu.com/p/6e2f6b4517f0)
* [oozie5.1 编译安装](https://blog.csdn.net/zphyy1988/article/details/88994298)

官方文档，看起来比较难以理解，可以先看下面的两个中文文档。



### 3.1. 编译





### 3.2. 安装

先配置各种参数，然后使用`oozie-setup.sh`自动化脚本来进行初始化。

#### 

### 









## 参考文档



> 参考文档

* [官网地址](http://oozie.apache.org/)
* [任务调度框架Oozie学习笔记](https://blog.csdn.net/qq_24326765/article/details/81060058)
* [从头开始 手把手 oozie 安装配置 with an example](https://blog.csdn.net/lucylove3943/article/details/80673962)
* [Oozie:如何定义Oozie的工作流](https://www.jianshu.com/p/f2b4f8c1ffe1)

