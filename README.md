# ik-mysql
## 利用mysql实时更新ElasticSearch热词
   
   
### 1、先下载相应ik分词器版本
地址：https://github.com/medcl/elasticsearch-analysis-ik/releases  
  
  
### 2、修改相应代码
* `config`添加`jdbc-reload.properties`文件  
* 在`Dictionary.java`中添加   
    + `loadMySQLExtDict` 和 `loadMySQLStopwordDict` 方法  
    + `initial` 方法中添加 `new Thread(new HotDicReloadThread()).start(); `   
    
    ```  
    
    if (singleton == null) {   
        singleton = new Dictionary(cfg);
        singleton.loadMainDict();
        singleton.loadSurnameDict();
        singleton.loadQuantifierDict();
        singleton.loadSuffixDict();
        singleton.loadPrepDict();
        singleton.loadStopWordDict();

        new Thread(new HotDicReloadThread()).start(); 
        
        if (cfg.isEnableRemoteDict()) {
            // 建立监控线程
            for (String location : singleton.getRemoteExtDictionarys()) {
                // 10 秒是初始延迟可以修改的 60是间隔时间 单位秒
                pool.scheduleAtFixedRate(new Monitor(location), 10, 60, TimeUnit.SECONDS);
            }
            for (String location : singleton.getRemoteExtStopWordDictionarys()) {
                pool.scheduleAtFixedRate(new Monitor(location), 10, 60, TimeUnit.SECONDS);
            }
        }
        return singleton;
    }
    
    ```  
    
  + `loadMainDict`方法的最后添加 `this.loadMySQLExtDict();`    
  + `loadStopWordDict`方法的最后添加 `this.loadMySQLStopwordDict();`  
      
* `dic` 目录下添加 `HotDicReloadThread.ava` 文件
* `plugin.xml` 文件中添加
  
      <dependencySet>  
          <outputDirectory>/</outputDirectory>  
          <useProjectArtifact>true</useProjectArtifact>  
          <useTransitiveFiltering>true</useTransitiveFiltering>  
          <includes>  
              <include>mysql:mysql-connector-java</include>  
          </includes>  
      </dependencySet>  
      
* `pom.xml`添加  

        <dependency>  
            <groupId>mysql</groupId>  
            <artifactId>mysql-connector-java</artifactId>  
            <version>5.1.46</version>  
        </dependency>  

### 3、打包上传  
* `mvn package`打包之后在`target`目录之下会有一个`releases`目录  
* 将该目录下的压缩包上传至es的`plugins`目录之下进行解压
* 解压之后会有会生成`elasticsearch`和`mysql-connector-java-5.1.46.jar` 再将`mysql`的`jar`包移入`elasticsearch`之下重启es即可  
## 注意：
* **所下载的版本是对应elasticSearch的版本<br >但是pom中所写的`<elasticsearch.version>5.6.16</elasticsearch.version>`版本不一定是你所下的版本所以修改版本即可**  

## **异常处理:**
*  `java.sql.SQLException: No suitable driver found for jdbc`    
    `loadMySQLExtDict和loadMySQLStopwordDict`方法中的 `Connection、Statement和ResultSet`所引的包不对不是mysql的  
    
*  `java.security.AccessControlException: access denied ("java.lang.RuntimePermission" "setContextClassLoader")`  
    修改 java 目录 `java/jre/lib/security` 中 `java.policy`  
    在文件最后添加  
    >`permission java.security.AllPermission`  
