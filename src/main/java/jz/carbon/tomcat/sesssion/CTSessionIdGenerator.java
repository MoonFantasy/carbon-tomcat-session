package jz.carbon.tomcat.sesssion;

import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created by jack on 2016/12/12.
 */
public class CTSessionIdGenerator extends SessionIdGeneratorBase {
    private static final Log log = LogFactory.getLog(CTSessionIdGenerator.class);
    public static final int MIN_SESSION_ID_LENGTH = 50;
    public CTSessionIdGenerator() {

    }

    @Override
    public void setSessionIdLength(int sessionIdLength) {
        super.setSessionIdLength(sessionIdLength > MIN_SESSION_ID_LENGTH ? sessionIdLength : MIN_SESSION_ID_LENGTH);
    }

    public String generateSessionId(String route) {
        int sessionIdLength = this.getSessionIdLength();
        int maxByteLength = sessionIdLength / 4 * 3;
        byte[] randomAppended = new byte[maxByteLength];
        new Random().nextBytes(randomAppended);
        byte[] routeBytes = null;
        String result = null;

        if (route == null || route.length() == 0) {
            route = this.getJvmRoute();
        }

        if (route != null && route.length() > 0) {
            //noinspection Since15
            routeBytes = route.getBytes(Charset.forName("UTF-8"));
        }

        if (routeBytes != null && routeBytes.length > 0) {
            byte[] resultBytes = new byte[randomAppended.length + routeBytes.length];
            System.arraycopy(routeBytes, 0, resultBytes, 0, routeBytes.length);
            System.arraycopy(randomAppended, 0, resultBytes, routeBytes.length, randomAppended.length);
            result = new String(Base64.encodeBase64(resultBytes));
            log.debug("Generate SessionId " + result);
            return result;
        }

        result = new String(Base64.encodeBase64(randomAppended));
        log.debug("Generate SessionId " + result);
        return result;
    }
}
