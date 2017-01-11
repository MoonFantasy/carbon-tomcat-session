package session;

import jz.carbon.tomcat.sesssion.CTSessionIdGenerator;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by jack on 2017/1/10.
 */
public class TestCTSessionIdGenerator {
    @Test
    public void testGenerateSessionId() throws Exception {
        CTSessionIdGenerator idGenerator = new  CTSessionIdGenerator();
        //idGenerator.setJvmRoute("abc");
        String jvmRoute = "123_Asdf";
        String route = "asdf41234";
        int jvmRouteByteLen = jvmRoute.getBytes(Charset.forName("UTF-8")).length;
        int routeByteLen = route.getBytes(Charset.forName("UTF-8")).length;
        int sessionIdLength = 234;
        int veryLen = sessionIdLength - (sessionIdLength % 4);
        int jvmRouteVaryLen = ((jvmRouteByteLen + (sessionIdLength/ 4 * 3)) / 3) * 4
                + (((jvmRouteByteLen + (sessionIdLength/ 4 * 3)) % 3) > 0 ? 4 : 0);

        int routeVaryLen = ((routeByteLen + (sessionIdLength/ 4 * 3)) / 3) * 4
                + (((routeByteLen + (sessionIdLength/ 4 * 3)) % 3) > 0 ? 4 : 0);

        idGenerator.setSessionIdLength(sessionIdLength);
        String sessionId = idGenerator.generateSessionId(null);
        String sessionId2 = idGenerator.generateSessionId(null);

        assertNotEquals(sessionId, sessionId2);
        assertEquals(sessionId.length(), sessionId2.length());
        assertEquals(veryLen, sessionId.length());

        idGenerator.setJvmRoute(jvmRoute);
        sessionId = idGenerator.generateSessionId(null);
        assertEquals(jvmRouteVaryLen, sessionId.length());

        sessionId = idGenerator.generateSessionId(route);
        assertEquals(routeVaryLen, sessionId.length());

    }
}
