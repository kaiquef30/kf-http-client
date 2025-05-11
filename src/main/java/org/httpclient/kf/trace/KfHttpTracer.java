package org.httpclient.kf.trace;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public interface KfHttpTracer {

    void onRequest(HttpRequest request);

    void onResponse(HttpRequest request, HttpResponse<?> response, Duration duration, String body);

}
