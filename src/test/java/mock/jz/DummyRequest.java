package mock.jz;

import org.apache.catalina.connector.Request;

/**
 * Created by jack on 2017/1/11.
 */
public class DummyRequest extends Request {
    private String requestStr = null;
    public DummyRequest(String requestStr) {
        this.requestStr = requestStr;
    }
    public String getRequestURI() {
        return this.requestStr;
    }
}