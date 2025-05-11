package org.httpclient.kf.http;

import org.httpclient.kf.retry.KfRetryPolicy;
import org.httpclient.kf.trace.KfHttpTracer;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KfHttpClient {

    private final HttpClient client;
    private final KfInterceptor interceptor;
    private final KfConfig config;
    private final KfHttpTracer tracer;

    public KfHttpClient(KfConfig config) {
        this.config = config;
        this.tracer = config.getTracer();;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(config.getTimeout())
                .cookieHandler(config.getCookieManager())
                .followRedirects(config.getRedirectPolicy());

        if (config.getProxy() != null) {
            builder.proxy(config.getProxy().toProxySelector());
        }
        if (config.getSslContext() != null) {
            builder.sslContext(config.getSslContext());
        }

        this.client = builder.build();
        this.interceptor = config.getInterceptor();
    }

    public HttpResponseWrapper get(String url, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestBuilder.buildGet(url, headers);
        return send(request);
    }

    public HttpResponseWrapper postJson(String url, String jsonBody, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestBuilder.buildPostJson(url, jsonBody, headers);
        return send(request);
    }

    public HttpResponseWrapper postForm(String url, Map<String, String> formData, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestBuilder.buildPostForm(url, formData, headers);
        return send(request);
    }

    public HttpResponseWrapper put(String url, String jsonBody, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestBuilder.buildPut(url, jsonBody, headers);
        return send(request);
    }

    public HttpResponseWrapper delete(String url, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest request = HttpRequestBuilder.buildDelete(url, headers);
        return send(request);
    }

    private HttpResponseWrapper send(HttpRequest request) throws IOException, InterruptedException {
        if (tracer != null) tracer.onRequest(request);
        Instant start = Instant.now();

        if (interceptor != null) interceptor.beforeSend(request);

        if (config.getProxy() != null && config.getProxy().hasAuth()) {
            String authHeader = config.getProxy().getAuthHeader();

            HttpRequest.Builder rebuilt = HttpRequest.newBuilder(request.uri())
                    .method(request.method(), request.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));

            request.headers().map().forEach((k, v) -> v.forEach(val -> rebuilt.header(k, val)));

            rebuilt.setHeader("Proxy-Authorization", authHeader);
            request = rebuilt.build();
        }

        HttpResponse<byte[]> response = null;
        int attempt = 1;

        KfRetryPolicy retryPolicy = config.getRetryPolicy();
        boolean shouldRetry;
        IOException lastException = null;

        do {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                shouldRetry = retryPolicy != null &&
                        attempt < retryPolicy.getMaxAttempts() &&
                        retryPolicy.shouldRetry(response.statusCode());
                if (!shouldRetry) break;

                long delay = retryPolicy.getDelayForAttempt(attempt);
                Thread.sleep(delay);
                attempt++;
            } catch (IOException | InterruptedException e) {
                lastException = e instanceof IOException ? (IOException) e : null;
                if (retryPolicy == null || attempt >=  (retryPolicy.getMaxAttempts())) {
                    if (lastException != null) throw lastException;
                    throw e;
                }
                long delay = retryPolicy.getDelayForAttempt(attempt);
                Thread.sleep(delay);
                attempt++;
            }
        } while (true);

        String encoding = response.headers().firstValue("Content-Encoding").orElse("").toLowerCase(Locale.ROOT);
        String decodedBody;

        if ("gzip".equals(encoding)) {
            try (var gzipStream = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(response.body()))) {
                decodedBody = new String(gzipStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                decodedBody = "[ERROR] Falha ao descompactar GZIP: " + e.getMessage();
            }
        } else {
            decodedBody = new String(response.body(), StandardCharsets.UTF_8);
        }

        if (interceptor != null) interceptor.afterReceive(response);

        if (tracer != null) {
            Duration duration = Duration.between(start, Instant.now());
            tracer.onResponse(request, response, duration, decodedBody);
        }

        return new HttpResponseWrapper(response.statusCode(), response.headers().map(), decodedBody);
    }

    public String extractFieldFromJson(String body, String fieldName) {
        if (body == null || fieldName == null) return null;

        String regex = "\""+Pattern.quote(fieldName)+"\"\\s*:\\s*\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
