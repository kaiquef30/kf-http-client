package org.httpclient.kf.chain;

import java.util.Map;
import java.util.function.Consumer;

class KfRequestStep {

    enum Method {
        GET,
        POST_JSON,
        POST_FORM
    }
    
    final Method method;
    final String url;
    final Object body;
    final Consumer<Map<String, String>> headerModifier;
    String extractKey;
    String extractRegex;
    
    KfRequestStep(Method method,
                  String url,
                  Object body,
                  Consumer<Map<String, String>> headerModifier) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.headerModifier = headerModifier;
    }

    public void setExtractor(String key, String regex) {
        this.extractKey = key;
        this.extractRegex = regex;
    }

    public void applyHeaderModifier(Map<String, String> headers) {
        if (headerModifier != null) {
            headerModifier.accept(headers);
        }
    }
}
