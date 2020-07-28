##利用mysql实时更新ElasticSearch热词
   
   
###1、先下载相应ik分词器版本
地址：https://github.com/medcl/elasticsearch-analysis-ik/releases  
  
  
###2、修改相应代码
* config添加jdbc-reload.properties文件  
* 在Dictionary.java 中添加   
    + loadMySQLExtDict和loadMySQLStopwordDict方法  
    + initial方法中添加 ```new Thread(new HotDicReloadThread()).start(); ``` 
    <br >
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
    + loadMainDict方法的最后添加 ```this.loadMySQLExtDict();```
    + loadStopWordDict方法的最后添加 ```this.loadMySQLStopwordDict();```
        
* HotDicReloadThread.ava  
* plugin.xml 文件中添加
  
      <dependencySet>  
          <outputDirectory>/</outputDirectory>  
          <useProjectArtifact>true</useProjectArtifact>  
          <useTransitiveFiltering>true</useTransitiveFiltering>  
          <includes>  
              <include>mysql:mysql-connector-java</include>  
          </includes>  
      </dependencySet>  
      
* pom.xml添加  

        <dependency>  
            <groupId>mysql</groupId>  
            <artifactId>mysql-connector-java</artifactId>  
            <version>5.1.46</version>  
        </dependency>  

##注意：
* **所下载的版本是对应elasticSearch的版本<br >但是pom中所写的`<elasticsearch.version>5.6.16</elasticsearch.version>`版本不一定是你所下的版本所以修改版本即可**  

### **异常处理**
*  `java.sql.SQLException: No suitable driver found for jdbc`    
    `loadMySQLExtDict和loadMySQLStopwordDict`方法中的 `Connection、Statement和ResultSet`所引的包不对不是mysql的  
    
*  `java.security.AccessControlException: access denied ("java.lang.RuntimePermission" "setContextClassLoader")`  
    修改 java 目录 `java/jre/lib/security` 中 `java.policy`  
    在文件最后添加  
    >`permission java.security.AllPermission`  
