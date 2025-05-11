package org.httpclient.kf.http;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class HttpResponseWrapper {

    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String body;

    public HttpResponseWrapper(int statusCode, Map<String, List<String>> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

}
