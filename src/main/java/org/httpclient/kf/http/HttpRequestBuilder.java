package org.httpclient.kf.http;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public class HttpRequestBuilder {

    public static HttpRequest buildGet(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();
        headers.forEach(builder::header);
        return builder.build();
    }

    public static HttpRequest buildPostJson(String url, String body, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json");
        headers.forEach(builder::header);
        return builder.build();
    }

    public static HttpRequest buildPostForm(String url, Map<String, String> formData, Map<String, String> headers) {
        StringJoiner sj = new StringJoiner("&");
        formData.forEach((k, v) -> sj.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8)));
        String formBody = sj.toString();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .header("Content-Type", "application/x-www-form-urlencoded");
        headers.forEach(builder::header);
        return builder.build();
    }

    public static HttpRequest buildPut(String url, String body, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(builder::header);
        return builder.build();
    }

    public static HttpRequest buildDelete(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();
        headers.forEach(builder::header);
        return builder.build();
    }

}
