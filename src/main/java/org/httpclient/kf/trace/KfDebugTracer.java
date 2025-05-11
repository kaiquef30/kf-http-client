package org.httpclient.kf.trace;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class KfDebugTracer implements KfHttpTracer {

    @Override
    public void onRequest(HttpRequest request) {
        System.out.println("🔍 [REQUEST] " + request.method() + " " + request.uri());
        printHeaders(request.headers().map());
    }

    @Override
    public void onResponse(HttpRequest request, HttpResponse<?> response, Duration duration, String body) {

        System.out.println("✅ [RESPONSE] " + request.method() + " " + request.uri());
        System.out.println("⏱  Tempo: " + duration.toMillis() + "ms");
        System.out.println("📦 Status: " + response.statusCode());
        printHeaders(response.headers().map());

        if (body != null && body.length() < 1000) {
            System.out.println("📄 Body:");
            System.out.println(body);
        } else if (body != null) {
            System.out.println("📄 Body (truncado): " + body.substring(0, 1000) + "...");
        }

    }

    private void printHeaders(Map<String, List<String>> headers) {
        if (headers.isEmpty()) {
            System.out.println(" (sem headers)");
            return;
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = String.join(", ", entry.getValue());
            System.out.println("  " + key + ": " + value);
        }
    }

}
