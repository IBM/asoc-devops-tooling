package fnc.sync.utils;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;

public class DynamicProxyRoutePlanner implements HttpRoutePlanner {

    private DefaultProxyRoutePlanner defaultProxyRoutePlanner = null;

    public DynamicProxyRoutePlanner(HttpHost host){
        defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(host);
    }

    public void setProxy(HttpHost host){
        defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(host);
    }

    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        return defaultProxyRoutePlanner.determineRoute(target,request,context); 
    }

}