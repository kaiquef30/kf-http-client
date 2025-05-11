package org.httpclient.kf.http;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface KfInterceptor {

    void beforeSend(HttpRequest request);

    void afterReceive(HttpResponse<?> response);

}
