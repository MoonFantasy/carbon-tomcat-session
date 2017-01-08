# carbon-tomcat-session
WSO2 carbon tomcat session
## Buid jar

``` mvn package ```

Then drop ```carbon-tomcat-session-dep-<version>.jar``` into ```<CARBON_HOME>/repository/repository/components/lib```

## configuration 
 For web applications edit ```<CARBON_HOME>/repository/conf/tomcat/context.xml```
 
 For carbon console edit ```<CARBON_HOME>/repository/conf/tomcat/carbon/META-INF/context.xml``` 
``` xml
    <Manager className='jz.carbon.tomcat.sesssion.CTSessionPersistentManager'
             sessionIdGeneratorClassName="jz.carbon.tomcat.sesssion.CTSessionIdGenerator"
             sessionIdLength="200">
        <Store className='jz.carbon.tomcat.sesssion.store.SpyMemcachedStore' nodes='hostName1:port,hostName2:port.....'/>
    </Manager>
```


