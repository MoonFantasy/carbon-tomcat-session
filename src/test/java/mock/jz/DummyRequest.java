package mock.jz;

import org.apache.catalina.connector.Request;

/**
 * Created by jack on 2017/1/11.
 */
public class DummyRequest extends Request {
    private String requestStr = null;
    private String method = "GET";
    public DummyRequest(String requestStr) {
        this(requestStr, "GET");
    }

    public DummyRequest(String requestStr, String method) {
        this.requestStr = requestStr;
        this.method = method;
    }

    public String getRequestURI() {
        return this.requestStr;
    }

    public String getMethod() {
        return method;
    }
}