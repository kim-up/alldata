# Dinky

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/DataLinkDC/dlink?category=lines)](https://github.com/DataLinkDC/dlink)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README.md)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)

[![Stargazers over time](https://starchart.cc/DataLinkDC/dlink.svg)](https://starchart.cc/DataLinkDC/dlink)

## 简介

实时即未来，Dlink 为 Apache Flink 而生，让 Flink SQL 纵享丝滑，并致力于实时计算平台建设。

Dinky 基于 Apache Flink 实现 Dlink ，增强 Flink 的应用与体验，探索流式数仓。即站在巨人肩膀上创新与实践，Dinky 在未来批流一体的发展趋势下潜力无限。

最后，Dinky 的发展皆归功于 Apache Flink 等其他优秀的开源项目的指导与成果。

## 特点

一个 `开箱即用` 、`易扩展` ，以 `Apache Flink` 为基础，连接 `OLAP` 和 `数据湖` 等众多框架的 `一站式` 实时计算平台，致力于 `流批一体` 和 `湖仓一体` 的建设与实践。

其主要目标如下：

- 可视化交互式 FlinkSQL 和 SQL 的数据开发平台：自动提示补全、语法高亮、调试执行、语法校验、语句美化、全局变量等
- 支持全面的多版本的 FlinkSQL 作业提交方式：Local、Standalone、Yarn Session、Yarn Per-Job、Yarn Application、Kubernetes Session、Kubernetes Application
- 支持 Apache Flink 所有的 Connector、UDF、CDC等
- 支持 FlinkSQL 语法增强：兼容 Apache Flink SQL、表值聚合函数、全局变量、CDC多源合并、执行环境、语句合并、共享会话等
- 支持易扩展的 SQL 作业提交方式：ClickHouse、Doris、Hive、Mysql、Oracle、Phoenix、PostgreSql、SqlServer 等
- 支持 FlinkCDC （Source 合并）整库实时入仓入湖
- 支持实时调试预览 Table 和 ChangeLog 数据及图形展示
- 支持语法逻辑检查、作业执行计划、字段级血缘分析等
- 支持 Flink 元数据、数据源元数据查询及管理
- 支持实时任务运维：作业上线下线、作业信息、集群信息、作业快照、异常信息、作业日志、数据地图、即席查询、历史版本、报警记录等
- 支持作为多版本 FlinkSQL Server 的能力以及 OpenApi
- 支持易扩展的实时作业报警及报警组：钉钉、微信企业号等
- 支持完全托管的 SavePoint 启动机制：最近一次、最早一次、指定一次等
- 支持多种资源管理：集群实例、集群配置、Jar、数据源、报警组、报警实例、文档、用户、系统配置等
- 更多隐藏功能等待小伙伴们探索

## 原理

![dinky_principle](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/main/dinky_principle.png)

## 精彩瞬间

> FlinkSQL Studio

![flinksqlstudio](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/flinksqlstudio.png)

> 实时调试预览

![selectpreview](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/selectpreview.png)

> 语法和逻辑检查

![checksql](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/checksql.png)

> JobPlan

![jobplan](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/jobplan.png)

> 字段级血缘分析

![lineage](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/lineage.png)

> BI 展示

![charts](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/charts.png)

> 元数据查询

![metadata](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/metadata.png)

> 实时任务监控

![monitor](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/monitor.png)

> 实时作业信息

![jobinfo](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/jobinfo.png)

> 数据地图

![datamap](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/datamap.png)

> 数据源注册

![datasource](https://raw.githubusercontent.com/DataLinkDC/dlink/main/dlink-doc/images/060/datasource.png)

## 功能

详见 [功能](https://github.com/DataLinkDC/dlink/blob/dev/docs/zh-CN/feature.md)

## 近期计划

- [ ]  多租户及命名空间
- [ ]  全局血缘与影响分析
- [ ]  统一元数据管理
- [ ]  Flink 元数据持久化
- [ ]  多版本 Flink-Client Server
- [ ]  整库千表同步

## 参与贡献

欢迎您为社区贡献自己的力量，共建共赢，贡献流程请参考： [[参与贡献](https://github.com/DataLinkDC/dlink/blob/dev/docs/zh-CN/developer_guide/how_contribute.md)]

## 如何部署

详见 [编译](https://github.com/DataLinkDC/dlink/blob/dev/docs/zh-CN/quick_start/build.md) 和 [安装](https://github.com/DataLinkDC/dlink/blob/dev/docs/zh-CN/quick_start/deploy.md) 。

## 如何升级到最新

由于功能较多，所以 bug 及优化点较多，强烈建议你使用或升级到最新版本。
替换最新 Dinky 所有依赖包 ，执行 sql 目录下的 dlink_history.sql 中的部分升级语句，依据是通过版本号与日期来判断从何处开始执行，请不要直接执行全部 sql。

## Dinky社区项目是Github开源实时计算平台

### 社区链接
>
>https://github.com/DataLinkDC/dlink

## DINKY SUPPORT RUNNING HIVEQL ON FLINK

## 1、新增hive2flink元数据

## 2、新增hive2flink任务类型

## 3、引入flink-sql-parser-hive解析HiveSQL

## 4、启用Flink natively support REST Endpoint and HiveServer2 Endpoint

### 4.1 ./bin/sql-gateway.sh start -Dsql-gateway.endpoint.type=hiveserver2

### 4.2 Flink CONF配置：sql-gateway.endpoint.type: hiveserver2


## 项目官网地址

https://github.com/DataLinkDC/dinky