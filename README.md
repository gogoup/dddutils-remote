#dddutils-remote

A java-implemented lightweight web server.


## Package Description

* objectsegment: Abstract classes and interfaces used for persistence layer

* pool: Object pooling data structure and implemenations.

* appsession: An application session library provides API for access session paramters and session replication.

* remote: Http server and Http/Socket clients.

* misc: Miscellaneous

## MySQL Session Persistence Setup

The following script is used to create a database for session persistence.

<pre>
CREATE DATABASE  IF NOT EXISTS `appsession`;
USE `appsession`;

DROP TABLE IF EXISTS `session`;
CREATE TABLE `session` (
  `_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ApplicationId` varchar(255) NOT NULL,
  `SessionKey` varchar(255) NOT NULL,
  `LastUpdateTime` bigint(20) unsigned NOT NULL,
  `States` text NOT NULL,
  PRIMARY KEY (`_id`),
  UNIQUE KEY `Key_UNIQUE` (`ApplicationId`,`SessionKey`),
  KEY `LastUpdateTime` (`LastUpdateTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
</pre>

Create c3p0-config.xml file with the following content.


```xml
<?xml version="1.0" encoding="UTF-8"?>
 
<c3p0-config>

  <named-config name="APP_SESSION_DB">
  	<property name="driverClass">org.gjt.mm.mysql.Driver</property>
	<property name="jdbcUrl">jdbc:mysql://localhost/appsession</property>  
	<property name="user">root</property>  
	<property name="password">123456</property>
    <property name="automaticTestTable">con_test</property>
    <property name="checkoutTimeout">3000</property>
    <property name="idleConnectionTestPeriod">30</property>
    <property name="initialPoolSize">10</property>
    <property name="maxIdleTime">3000</property>
    <property name="maxPoolSize">100</property>
    <property name="minPoolSize">10</property>
    <property name="maxStatements">200</property>    
    <property name="acquireIncrement">20</property>
  </named-config>
  
</c3p0-config>
```