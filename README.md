# carbon-tomcat-session

WSO2 carbon tomcat session manager implementation that stores sessions in Memcache or Redis for easy to clustering of wso2 carbon and others. Session are implementation as non-session-sticky.
It Support wso2 carbon 4.4.X
## Buiding package
### requirement
* Apache Maven 3.X
* jdk 1.7 or higher 

``` shell
git clone https://github.com/MoonFantasy/carbon-tomcat-session.git
cd carbon-tomcat-session
mvn package
```

All dependencies library will pack in one jar

Then drop ```carbon-tomcat-session-dep-<version>.jar``` into ```<CARBON_HOME>/repository/repository/components/lib```

## configuration
### Session manager configuration

 For web applications edit ```<CARBON_HOME>/repository/conf/tomcat/context.xml```
 
 For carbon console edit ```<CARBON_HOME>/repository/conf/tomcat/carbon/META-INF/context.xml```
 
 Replace ```<Manager>``` arttribute ```className``` to ```jz.carbon.tomcat.sesssion.CTSessionPersistentManager```
 Add ```<Store>``` in ```<Manager>```
 for example 
``` xml
<Manager className='jz.carbon.tomcat.sesssion.CTSessionPersistentManager'
        sessionIdGeneratorClassName="jz.carbon.tomcat.sesssion.CTSessionIdGenerator"
        sessionIdLength="200">
    <Store className='jz.carbon.tomcat.sesssion.store.SpyMemcachedStore' nodes='hostName1:port,hostName2:port.....'/>
</Manager>
```

### Valve configuration

edit ```<CARBON_HOME>/repository/conf/tomcat/catalina-server.xml```

Add ```<Valve className="jz.carbon.tomcat.sesssion.CTSessionHandlerValve"/>``` to ```<Host>``` at first Valve

for example
``` xml
<Host name="localhost" unpackWARs="true" deployOnStartup="false" autoDeploy="false"
        appBase="${carbon.home}/repository/deployment/server/webapps/">
    <Valve className="jz.carbon.tomcat.sesssion.CTSessionHandlerValve"/>
    <Valve className="org.wso2.carbon.tomcat.ext.valves.CarbonContextCreatorValve"/>
    <Valve className="org.apache.catalina.valves.AccessLogValve" directory="${carbon.home}/repository/logs"
            prefix="http_access_" suffix=".log"
            pattern="combined"/>
    <Valve className="org.wso2.carbon.tomcat.ext.valves.CarbonStuckThreadDetectionValve" threshold="600"/>
    <Valve className="org.wso2.carbon.tomcat.ext.valves.CompositeValve"/>
</Host>
```
