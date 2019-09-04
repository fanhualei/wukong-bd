# Kettle

Kettle是一款国外开源的ETL工具，纯java编写，可以在Windows、Linux、Unix上运行，[数据抽取](https://baike.baidu.com/item/数据抽取/218221)高效稳定。

Kettle这个ETL工具集，它允许你管理来自不同数据库的数据，通过提供一个图形化的用户环境来描述你想做什么，而不是你想怎么做。

Kettle中有两种[脚本文件](https://baike.baidu.com/item/脚本文件)，transformation和[job](https://baike.baidu.com/item/job)，transformation完成针对数据的基础转换，job则完成整个工作流的控制。

作为[Pentaho](https://baike.baidu.com/item/Pentaho)的一个重要组成部分，现在在国内项目应用上逐渐增多。

Kettle家族目前包括4个产品：Spoon、Pan、CHEF、Kitchen。

**SPOON** 允许你通过图形界面来设计ETL转换过程（Transformation）。

**PAN** 允许你批量运行由Spoon设计的ETL转换 (例如使用一个时间调度器)。Pan是一个后台执行的程序，没有图形界面。

**CHEF** 允许你创建任务（Job）。 任务通过允许每个转换，任务，脚本等等，更有利于自动化更新数据仓库的复杂工作。任务通过允许每个转换，任务，脚本等等。任务将会被检查，看看是否正确地运行了。

**KITCHEN** 允许你批量使用由Chef设计的任务 (例如使用一个时间调度器)。KITCHEN也是一个后台运行的程序。





可以使用分页，进行数据清洗