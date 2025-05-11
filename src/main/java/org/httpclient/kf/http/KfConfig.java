package org.httpclient.kf.http;

import org.httpclient.kf.retry.KfRetryPolicy;
import org.httpclient.kf.trace.KfDebugTracer;
import org.httpclient.kf.trace.KfHttpTracer;

import javax.net.ssl.SSLContext;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

public class KfConfig {

    private final Duration timeout;
    private final KfProxy proxy;
    private final SSLContext sslContext;
    private final CookieManager cookieManager;
    private final HttpClient.Redirect redirectPolicy;
    private final KfInterceptor interceptor;
    private final KfHttpTracer tracer;
    private final KfRetryPolicy retryPolicy;

    public KfConfig(Duration timeout, KfProxy proxy, SSLContext sslContext,
                    KfInterceptor interceptor, KfHttpTracer tracer, KfRetryPolicy retryPolicy) {

        this.timeout = timeout;
        this.proxy = proxy;
        this.sslContext = sslContext;
        this.tracer = tracer;
        this.retryPolicy = retryPolicy;
        this.cookieManager = new CookieManager();
        this.redirectPolicy = HttpClient.Redirect.NORMAL;
        this.interceptor = interceptor;
    }

    public static KfConfig defaultConfig() {
        return new KfConfig(
                Duration.ofSeconds(10),
                null,
                null,
                null,
                new KfDebugTracer(),
                null
        );
    }

    public KfConfig withTracer(KfHttpTracer tracer) {
        return new KfConfig(timeout, proxy, sslContext, interceptor, tracer, retryPolicy);
    }

    public KfConfig withRetryPolicy(KfRetryPolicy retryPolicy) {
        return new KfConfig(timeout, proxy, sslContext, interceptor, tracer, retryPolicy);
    }

    public Duration getTimeout() {
        return timeout;
    }

    public KfProxy getProxy() {
        return proxy;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public HttpClient.Redirect getRedirectPolicy() {
        return redirectPolicy;
    }

    public KfInterceptor getInterceptor() {
        return interceptor;
    }

    public KfHttpTracer getTracer() {
        return tracer;
    }

    public KfRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
}
