package org.httpclient.kf.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    public Optional<String> getHeader(String name) {
        if (name == null || headers == null || headers.isEmpty()) {
            return Optional.empty();
        }
        return headers.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(name))
                .findFirst()
                .flatMap(e -> e.getValue().stream().findFirst());
    }
    
    public String getBody() {
        return body;
    }
    
    public boolean is2xxSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    public boolean is4xxClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
    
    public boolean is5xxServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
}
