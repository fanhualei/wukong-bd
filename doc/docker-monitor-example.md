# 数据监控之具体案例

[参考网址](https://github.com/stefanprodan/dockprom)



# 1. 部署架构



![alt](imgs/monitor-pro-architect.png)



按照监控的对象不同，分为三部分：

* 主机监控层
* 服务监控层
  * 服务组件监控：mysql redis rabbitmq
  * 自有业务监控:  需要在java程序中写监控代码 
* Nginx网关监控层





# 2. 主机监控层



## 2.0 常见问题

一些关键问题记录在这里



## 2.1 准备工作目录

### ① 定义工作目录

定义compose工作目录

```shell
mkdir /opt/my-monitor-pro 

cd /opt/my-monitor-pro
```



### ② 定义存储空间

如果是使用本地目录，docker-compose会自动建立。

如果使用nfs网络存储，需要重新挂载网络存储。





## 2.2 定义变量



```shell
cd /opt/my-monitor-pro
vi .env
```

> 文件内容

```properties
# 基础路径
DATA_PATH=/data/my-monitor-pro
```



## 2.3 组件配置

 

### 2.3.1 配置Prometheus

Prometheus的[Docker 网址](https://hub.docker.com/r/prom/prometheus) 说明很少，而[官方安装说明](https://prometheus.io/docs/prometheus/latest/installation/)功能描述也不完整。所以本次设置参考了[github上网友的方案](https://github.com/stefanprodan/dockprom)。

①②③④⑤⑥⑦⑧⑨



建立`prometheus`配置文件，今后用些文件生成`prometheus`容器。

#### ① 建立promethues目录

这个目录未来会被挂载到promethues系统中

```shell
mkdir prometheus
```



#### ② 配置prometheus.yml

```
vi prometheus/prometheus.yml
```



可以添加很多rules：

```
rule_files:  - /etc/prometheus/rules/*.rules
```



```yml
global:
  scrape_interval:     1m
  evaluation_interval: 1m

  # Attach these labels to any time series or alerts when communicating with
  # external systems (federation, remote storage, Alertmanager).
  external_labels:
      monitor: 'docker-host-alpha'

# Load and evaluate rules in this file every 'evaluation_interval' seconds.
rule_files:
  - "*.rules"

# A scrape configuration containing exactly one endpoint to scrape.
scrape_configs:
  - job_name: 'nodeexporter'
    scrape_interval: 15s
    static_configs:
      - targets: ['nodeexporter:9100']

  - job_name: 'cadvisor'
    scrape_interval: 15s
    static_configs:
      - targets: ['cadvisor:8080']

  - job_name: 'prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:9090']


alerting:
  alertmanagers:
  - scheme: http
    static_configs:
    - targets: 
      - 'alertmanager:9093'

```



#### ③ 配置报警规则

`*.rules` 是报警规则文件



> 服务监控规则

```
vi prometheus/targets.rules
```



```yaml
groups:
- name: targets
  rules:
  # 监控服务宕机
  - alert: monitor_service_down
    expr: up == 0
    for: 30s
    labels:
      severity: critical
    annotations:
      summary: "监控服务停止监控"
      description: "监控服务 {{ $labels.instance }} 没有响应."
```



> 监控主机

```
vi prometheus/host.rules
```



```yaml
groups:
# 宿主机
- name: host
  rules:
  #CPU负载过高
  - alert: high_cpu_load
    expr: node_load1 > 1.5
    for: 30s
    labels:
      severity: warning
    annotations:
      summary: "{{ $labels.instance }}负载过高"
      description: "Docker宿主机当前负载过高, 每秒的平均负载是 {{ $value}}. 当前实例： {{ $labels.instance }} ，具体任务： {{ $labels.job }}."

  #这个是用来测试的
  - alert: hostCpuUsageAlert
    expr: sum(avg without (cpu)(irate(node_cpu_seconds_total{mode!='idle'}[5m]))) by (instance) > 0.3
    for: 1m
    labels:
      severity: warning
    annotations:
      summary: "{{ $labels.instance }} CPU负载过高"
      description: "{{ $labels.instance }} CPU负载过高 (当前值: {{ $value }})"


  # 内存使用过高
  - alert: high_memory_load
    expr: (sum(node_memory_MemTotal_bytes) - sum(node_memory_MemFree_bytes + node_memory_Buffers_bytes + node_memory_Cached_bytes) ) / sum(node_memory_MemTotal_bytes) * 100 > 85
    for: 30s
    labels:
      severity: warning
    annotations:
      summary: "{{ $labels.instance }} 内存快用完了"
      description: "{{ $labels.instance }} 使用了 {{ humanize $value}}%. 当前任务{{ $labels.job }}."
  
  # 判断磁盘空间 rootfs=本地磁盘路径  aufs=挂载磁盘空间
  - alert: high_storage_load
    expr: (node_filesystem_size_bytes{fstype="rootfs"} - node_filesystem_free_bytes{fstype="rootfs"}) / node_filesystem_size_bytes{fstype="rootfs"}  * 100 > 85
    for: 30s
    labels:
      severity: warning
    annotations:
      summary: "{{ $labels.instance }}存储空间不足"
      description: "{{ $labels.instance }}存储空间是 {{ humanize $value}}%. 当前任务 {{ $labels.job }}."
```



```
vi prometheus/containers.rules
```



```yaml
groups:
# 监控容器
- name: containers
  rules:
  # 某个容器当掉了
  - alert: jenkins_down
    expr: absent(container_memory_usage_bytes{name="jenkins"})
    for: 30s
    labels:
      severity: critical
    annotations:
      summary: "Jenkins宕机了"
      description: "Jenkins超过30秒没有响应了."

  # 某个容器的CPU过高	
  - alert: jenkins_high_cpu
    expr: sum(rate(container_cpu_usage_seconds_total{name="jenkins"}[1m])) / count(node_cpu_seconds_total{mode="system"}) * 100 > 10
    for: 30s
    labels:
      severity: warning
    annotations:
      summary: "Jenkins CPU使用率过高"
      description: "Jenkins CPU 当前使用率是 {{ humanize $value}}%."

  # 某个容器的内储存过高
  - alert: jenkins_high_memory
    expr: sum(container_memory_usage_bytes{name="jenkins"}) > 1200000000
    for: 30s
    labels:
      severity: warning
    annotations:
      summary: "Jenkins memory使用率过高"
      description: "Jenkins memory 使用率是 {{ humanize $value}}."
```

 

#### ④ 报警模板的测试

使用`promtool check rules`命令的来进行测试。

```shell
docker-compose exec prometheus ls /etc/prometheus/

docker-compose exec prometheus promtool check rules  /etc/prometheus/containers.rules

docker-compose exec prometheus promtool check rules  /etc/prometheus/host.rules

docker-compose exec prometheus promtool check rules  /etc/prometheus/targets.rules
```







### 2.3.2 配置alertmanager





#### ① 建立alertmanager目录

这个目录未来会被挂载到`alertmanager`系统中

```shell
mkdir alertmanager
```



#### ②  配置企业微信



从[企业微信官网](https://work.weixin.qq.com/)得到 `企业Id Agentid  Secret`



![alt](imgs/premetheus-alter-to-weixin.png)



#### ③ 配置config.yml



```
vi alertmanager/config.yml
```



> config.yml

  

* 解决回复
  * `resolve_timeout`  默认值为`5m` , 分钟(`minute`)未接收到告警后标记，那么可能解决或者没有解决。
  * 如果收到的数据值小于报警值，那么会马上发送消息。
  * `send_resolved=true` 才返回解决信息。
* 路由匹配
  * 配置最深的匹配route截至，除非该节点**continue**为true
  * `match` `match_re` 分别`字符串验证` 与 `正则表达式`
* 告警分组
  * **group_wait**参数设置等待时间
  * **group_by**来定义分组规则
  * **group_interval**用于定义相同的Group之间发送告警通知的时间间隔
  * **repeat_interval** 如果警报已经成功发送通知, 想设置发送告警通知之前要等待时间



```yaml
#config.yml

 # 全局配置项
global:
  resolve_timeout: 5m
  wechat_api_corp_id: "ww61d97edebf566d2a"
  wechat_api_secret: "BeQdkY193DtldiP-3CAHG8Dpa2OdGDyDgG1fG3q9QWo"

  smtp_smarthost: 'smtp.21cn.com:25'
  smtp_from: 'runzhi_share@21cn.com'
  smtp_auth_username: 'runzhi_share@21cn.com'
  smtp_auth_password: '123'
  smtp_hello: '21cn.com'


templates:
- '*.msg'


route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 3m
  receiver: 'wechat'
  #子路由
  routes:
  - receiver: 'email'
    match:
      severity: warning


receivers:
- name: 'wechat'
  wechat_configs:
  - agent_id: '1000003'
    message: '{{ template "wechat.msg" . }}'
    to_party: '2'
    send_resolved: true

- name: 'email'
  email_configs:
  - to: 'runzhi_share@21cn.com'
    html: '{{ template "email.msg" . }}'
    headers: { Subject: " {{ .CommonLabels.instance }} {{ .CommonAnnotations.summary }}" }

```





#### ④ 配置消息模板

这个模板语言不是那么好用，建议使用默认的，也就是不指定模板消息。默认的唯一的确定是英文的提示。



> 邮件模板

不建议用，因为邮箱经常被当作垃圾邮件。

```shell
vi alertmanager/email.msg
```



```
{{ define "email.msg" }}
{{ range .Alerts }}
 <pre>
实例: {{ .Labels.instance }}
信息: {{ .Annotations.summary }}
详情: {{ .Annotations.description }}
时间: {{ .StartsAt.Format "2006-01-02 15:04:05" }}
 </pre>
{{ end }}
{{ end }}
```



> 微信模板

```shell
vi wecaht.msg
```



```yaml
{{ define "wechat.msg" }}{{- if gt (len .Alerts.Firing) 0 -}}{{ range .Alerts }}
@警报
状态:{{ .Status }}
级别:{{ .Labels.severity }}
类型:{{ .Labels.alertname }}
标题:{{ .Annotations.summary }}
详情:{{ .Annotations.description }}
触发时间:{{ .StartsAt.Format "2006-01-02 15:04:05" }}
{{ end }}{{ end -}}{{- if gt (len .Alerts.Resolved) 0 -}}{{ range .Alerts }}
@恢复
状态:{{ .Status }}
级别:{{ .Labels.severity }}
类型:{{ .Labels.alertname }}
标题:{{ .Annotations.summary }}
详情:{{ .Annotations.description }}
触发时间:{{ .StartsAt.Format "2006-01-02 15:04:05" }}
恢复时间: {{ .EndsAt.Format "2006-01-02 15:04:05" }}
{{ end }}{{ end -}}{{- end }}
```







#### ⑤ 企业微信调试方法

如果收不到信息，可以先看看日志

```
docker-compose logs alertmanager
```

然后看看微信能不能收到信息

- [参考文档](https://blog.csdn.net/qq_36937234/article/details/96306900)
- [微信提供的调试地址](https://work.weixin.qq.com/api/devtools/devtool.php)



#### ⑥ 参考文档

* [关于微信与email写的比较全的文档](https://www.e-learn.cn/content/qita/2638665)
* [Prometheus官方关于微信的配置说明](https://prometheus.io/docs/alerting/configuration/#wechat_config)
* [一个关于Prometheus全面介绍的文档](https://songjiayang.gitbooks.io/prometheus/content/alertmanager/wechat.html)
* [Prometheus好多的文档](https://blog.csdn.net/coffin_monkey/article/category/8967165)
* [Prometheus官方文档中文版](https://github.com/Alrights/prometheus)

①②③④⑤⑥⑦⑧⑨



### 2.3.3 配置node-exporter

[hubDocker地址](https://hub.docker.com/r/prom/node-exporter/tags)

直接在compose文件中安装。



> 收集如下监控指标：

- node_boot_time：系统启动时间
- node_cpu：系统CPU使用量
- node*disk**：磁盘IO
- node*filesystem**：文件系统用量
- node_load1：系统负载
- node*memeory**：内存使用量
- node*network**：网络带宽
- node_time：当前系统时间
- go_*：node exporter中go相关指标
- process_*：node exporter自身进程相关运行指标





### 2.3.4 配置cadvisor

[cadvisor Hub-Docker 地址](https://hub.docker.com/r/google/cadvisor)



### 2.3.5 配置grafana

[grafanaHub-Docker 地址](https://hub.docker.com/r/grafana/grafana)

#### ① 配置Datasources

> 建立目录

```shell
mkdir -p grafana/datasources

```



> 配置**Prometheus.json**

```shell
vi grafana/datasources/Prometheus.json
```



```json
{
    "name":"Prometheus",
    "type":"prometheus",
    "url":"http://prometheus:9090",
    "access":"proxy",
    "basicAuth":false
}
```



#### ② 配置**Dashboards**

| 名称              | 说明                              |
| ----------------- | --------------------------------- |
| docker_containers | Docker整体性能检测                |
| docker_host       | 监控主机性能，CPU 内存 磁盘 网络  |
| monitor_services  | Prometheus服务的监控              |
| nginx_container   | Nginx监控，请求数 连接数 连接速率 |



> 建立目录

```shell
mkdir -p grafana/dashboards

```



> 下载或修改相关文件

```json
cd grafana/dashboards
wget https://raw.githubusercontent.com/stefanprodan/dockprom/master/grafana/dashboards/docker_containers.json

wget https://raw.githubusercontent.com/stefanprodan/dockprom/master/grafana/dashboards/docker_host.json

wget https://raw.githubusercontent.com/stefanprodan/dockprom/master/grafana/dashboards/monitor_services.json

wget https://raw.githubusercontent.com/stefanprodan/dockprom/master/grafana/dashboards/nginx_container.json
```



#### ③ 配置setup.sh

```
cd /opt/my-monitor-pro/grafana
wget https://raw.githubusercontent.com/stefanprodan/dockprom/master/grafana/setup.sh
```



#### ④ 改变权限

```
chmod 755 ./grafana/setup.sh
```





## 2.4 撰写Compose文件

[参考了这个网址](https://github.com/stefanprodan/dockprom)



```shell
cd /opt/my-monitor-pro
vi docker-compose.yml
```



```yml
version: '3'

networks:
  monitor-net:
    driver: bridge

services:

  # prometheus
  prometheus:
    image: prom/prometheus:v2.13.1
    hostname: prometheus
    volumes:
      - ./prometheus/:/etc/prometheus/
      #- ${DATA_PATH}/prometheus/data/:/prometheus/
      - /etc/localtime:/etc/localtime:ro
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'      
    restart: unless-stopped
    expose:
      - 9090
    ports:
      - "9090:9090"  
    networks:
      - monitor-net
    labels:
      org.label-schema.group: "monitoring"


  # alertmanager 这个没有设置数据持久化
  alertmanager:
    image: prom/alertmanager:v0.19.0
    hostname: alertmanager
    volumes:
      - ./alertmanager/:/etc/alertmanager/
      - /etc/localtime:/etc/localtime:ro
    command:
      - '--config.file=/etc/alertmanager/config.yml'
      - '--storage.path=/alertmanager'
    restart: unless-stopped
    expose:
      - 9093
    ports:
      - "9093:9093"        
    networks:
      - monitor-net
    labels:
      org.label-schema.group: "monitoring"


  # nodeexporter
  nodeexporter:
    image: prom/node-exporter:v0.18.1
    hostname: nodeexporter
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
      - /etc/localtime:/etc/localtime:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.rootfs=/rootfs'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.ignored-mount-points=^/(sys|proc|dev|host|etc)($$|/)'
    restart: unless-stopped
    expose:
      - 9100
    networks:
      - monitor-net
    labels:
      org.label-schema.group: "monitoring"
      
  # cadvisor    
  cadvisor:
    image: google/cadvisor:v0.33.0
    hostname: cadvisor
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
      - /etc/localtime:/etc/localtime:ro
    restart: unless-stopped
    expose:
      - 8080
    networks:
      - monitor-net
    labels:
      org.label-schema.group: "monitoring" 
      
      
  # grafana    
  grafana:
    #6.4.3 没有curl 所以执行setup.sh 会出现错误
    image: grafana/grafana:6.3.6
    hostname: grafana
    volumes:
      #- ${DATA_PATH}/grafana/data/:/var/lib/grafana/
      - ./grafana/datasources:/etc/grafana/datasources
      - ./grafana/dashboards:/etc/grafana/dashboards
      - ./grafana/setup.sh:/setup.sh
      - /etc/localtime:/etc/localtime:ro
    entrypoint: /setup.sh
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    restart: unless-stopped
    expose:
      - 3000
    ports:
      - "3000:3000"        
    networks:
      - monitor-net
    labels:
      org.label-schema.group: "monitoring"
  
```

## 2.5 生成容器



```shell
docker-compose up -d
```





## 2.6 单元测试





### 2.6.1 测试Prometheus



#### ① 访问管理界面

访问9090端口，看看能不能打开

```
http://192.168.1.179:9090/graph
```



#### ② 数据持久化测试(未通过)

还不知道怎么做呢



### 2.6.2 测试alertmanager



#### ① 访问管理界面

访问9093端口，看看能不能打开

```
http://192.168.1.179:9093
```



#### ② 测试警报流程

手动拉高系统的CPU使用率

```shell
cat /dev/zero>/dev/null
```

分别测试报警与报警取消的状态。

![alt](imgs/prometheus-weixin-alert-message.jpg)







### 2.6.3 测试grafana

#### ① 访问管理界面

访问3000端口，看看能不能打开

```
http://192.168.1.179:3000
```



#### ② 下载Dashboard

>  点击导入菜单



![alt](imgs/grafana-import-step1.png)



> 进行简单配置

![alt](imgs/grafana-import-step2.png)



#### ③ 常用模板介绍

* Node Exproter
  * [Node Exporter模板-外语很全-*Node Exporter Full*](https://grafana.com/grafana/dashboards/1860)
  * [Node Exporter模板-外语-*Node Exporter Server Metrics*-一般虽然评分高](https://grafana.com/grafana/dashboards/405)

* Docker

  * [Docker监控模板-*Docker and system monitoring*](https://grafana.com/grafana/dashboards/893)

  * [Docker监控模板-简洁型-*Docker and Host Monitoring w/ Prometheus*](https://grafana.com/grafana/dashboards/179)

  * [Docker监控模板-刚更新-检索条件做的不错-**Docker Container & Host Metrics**](https://grafana.com/grafana/dashboards/10619)

    

①②③④⑤⑥⑦⑧⑨



# 参考文档



* 官方文档
  * [Exporter列表](https://prometheus.io/docs/instrumenting/exporters/)
  * [官方Docker地址](https://hub.docker.com/u/prom)
* 网友
  * [中文文档](https://yunlzheng.gitbook.io/prometheus-book/)







安装 Nginx

https://hub.docker.com/r/nginx/nginx-prometheus-exporter

https://github.com/nginxinc/nginx-prometheus-exporter

[使用Nginx对访问权限的控制](https://www.cnblogs.com/alterem/p/11495904.html)

nginx-vts prometheus-lua

OpenResty   tengine

https://hub.docker.com/r/openresty/openresty

**ngx_http_reqstat_module**

Tengine  ngx_http_reqstat_module



* OpenResty 

Nginx,OpenResty HTTP API performance monitoring based on Prometheus LUA library

Last updated: 4 months ago

https://github.com/zrbcool/prometheus-lua-nginx





# Prometheus监控Nginx

https://www.jianshu.com/p/3341db428978

# Nginx添加Lua扩展

https://www.jianshu.com/p/d72c8f06684a

