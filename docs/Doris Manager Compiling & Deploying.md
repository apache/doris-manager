# Doris Manager 编译部署文档

## 一、代码编译和运行
### 一）编译
直接运行build.sh脚本，会在manager路径下生成安装运行包——output，包中包括:

1、agent目录：Doris manager agent的安装包，是拷贝到部署doris服务的节点的安装包；

   1)bin,运行脚本.

       agent_start.sh agent启动脚本;
       agent_stop.sh agent停止脚本;
       download_doris.sh 部署doris时下载安装包的脚本;
   2)lib,运行的可执行jar包;

2、server目录：Doris manager server的安装包；
   
    1)bin,运行脚本.
       start_manager.sh manager server启动脚本;
       stop_manager.sh manager server停止脚本;

    2)conf,启动manager server的配置文件. manager.conf
    3)lib, 运行的可执行jar包;

### 二）运行
#### 1 配置
进入生成的安装运行包，查看配置文件server/conf路径，打开路径中的配置文件manager.conf，重点关注的配置项内容如下：
```$xslt
服务的启动http端口
STUDIO_PORT=8080

后端数据存放的数据库的类型，包括mysql/h2/postgresql.默认是支持mysql
MB_DB_TYPE=mysql

数据库连接信息
如果是配置的h2类型数据库，就不需要配置这些信息，会把数据以本地文件存放在本地
h2数据文件存放路径，默认直接存放在当前路径
H2_FILE_PATH=

如果是mysql/postgresql就需要配置如下连接信息
数据库地址
MB_DB_HOST=

数据库端口
MB_DB_PORT=3306

数据库访问端口
MB_DB_USER=

数据库访问密码
MB_DB_PASS=

数据库的database名称
MB_DB_DBNAME=

服务运行的路径，默认直接存放在当前运行路径的log文件夹中
LOG_PATH=

web容器的等待队列长度，默认100。队列也做缓冲池用，但也不能无限长，不但消耗内存，而且出队入队也消耗CPU
WEB_ACCEPT_COUNT=100

Web容器的最大工作线程数，默认200。（一般是CPU核数*200）
WEB_MAX_THREADS=200

Web容器的最小工作空闲线程数，默认10。（适当增大一些，以便应对突然增长的访问量）
WEB_MIN_SPARE_THREADS=10

Web容器的最大连接数，默认10000。（适当增大一些，以便应对突然增长的访问量）
WEB_MAX_CONNECTIONS=10000

访问数据库连接池最大连接数量，默认为10
DB_MAX_POOL_SIZE=20

访问数据库连接池最小空闲连接数，默认为10
DB_MIN_IDLE=10
```

#### 2 启动和停止
配置修改完成后，回到安装运行包server路径下，直接运行如下命令
```$xslt
启动命令：
sh bin/start_manager.sh
停止命令：
sh bin/stop_manager.sh
```
查看server目录logs中的日志即可判断程序是否启动成功

### 三） 接口文档

启动server后，直接查看http://serverHost:serverPort/swagger-ui.html# ,就可以查看全部的server接口。