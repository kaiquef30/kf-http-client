package org.httpclient.kf.curlConverter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class KfCurlRequest {

    public String method;
    public String url;
    public Map<String, String> headers;
    public String body;
    public boolean isJson;
    public boolean isFormUrlEncoded;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("=== CURL PARSED ===\n");
        builder.append("Method: ").append(method).append("\n");
        builder.append("URL: ").append(url).append("\n");
        builder.append("Headers:\n");

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        } else {
            builder.append("  (none)\n");
        }

        builder.append("Body:\n");
        if (body != null && isFormUrlEncoded) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                builder.append("  ").append(key).append(" = ").append(value).append("\n");
            }
        } else if (body != null) {
            builder.append("  ").append(body).append("\n");
        } else {
            builder.append("  (none)\n");
        }

        builder.append("isJson: ").append(isJson).append("\n");
        builder.append("isFormUrlEncoded: ").append(isFormUrlEncoded).append("\n");

        return builder.toString();
    }

}
