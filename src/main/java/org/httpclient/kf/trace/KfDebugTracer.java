package org.httpclient.kf.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KfDebugTracer implements KfHttpTracer {
    
    private static final Logger log = LoggerFactory.getLogger(KfDebugTracer.class);
    private static final int MAX_BODY_LOG_LENGTH = 2000;
    
    @Override
    public void onRequest(HttpRequest request) {
        log.debug("🔍 [REQUEST] {} {}", request.method(), request.uri());
        printHeaders(request.headers().map());
    }

    @Override
    public void onResponse(HttpRequest request,
                           HttpResponse<?> response,
                           Duration duration,
                           String body) {
        
        log.debug("✅ [RESPONSE] {} {}", request.method(), request.uri());
        log.debug("⏱  Duration: {}ms", duration.toMillis());
        log.debug("📦 Status: {}", response.statusCode());
        printHeaders(response.headers().map());

        if (body == null || body.isEmpty()) {
            return;
        }
        
        if (body.length() <= MAX_BODY_LOG_LENGTH) {
            log.debug("Body: {}", body);
        } else {
            log.debug("Body (truncated, {} chars, showing first {}): {}",
                    body.length(),
                    MAX_BODY_LOG_LENGTH,
                    body.substring(0, MAX_BODY_LOG_LENGTH));
        }
    }

    private void printHeaders(Map<String, List<String>> headers) {
        if (headers.isEmpty()) {
            log.debug(" (no headers)");
            return;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = String.join(", ", entry.getValue());
            log.debug("  {}: {}", key, maskIfSensitive(key, value));
        }
    }
    
    private String maskIfSensitive(String name, String value) {
        if (name == null || value == null) {
            return value;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("authorization")
                || lower.contains("cookie")
                || lower.contains("token")
                || lower.contains("secret")) {
            return "***";
        }
        return value;
    }
}
