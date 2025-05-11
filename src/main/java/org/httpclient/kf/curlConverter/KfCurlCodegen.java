package org.httpclient.kf.curlConverter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class KfCurlCodegen {

    public static String toJava(KfCurlRequest req, boolean pretty) {
        String indent = pretty ? "    " : "";
        String newline = pretty ? "\n" : "";

        StringBuilder code = new StringBuilder();

        code.append("import org.httpclient.kf.http.*;").append(newline);
        code.append("import org.httpclient.kf.trace.*;").append(newline);
        code.append("import java.util.Map;").append(newline).append(newline);

        code.append("KfHttpTracer tracer = new KfDebugTracer();").append(newline);
        code.append("KfConfig config = KfConfig.defaultConfig().withTracer(tracer);").append(newline);
        code.append("KfHttpClient client = new KfHttpClient(config);").append(newline).append(newline);

        code.append("Map<String, String> headers = Map.ofEntries(").append(newline);
        for (Map.Entry<String, String> entry : req.headers.entrySet()) {
            code.append(indent).append("Map.entry(\"")
                    .append(entry.getKey().replace("\"", "\\\""))
                    .append("\", \"")
                    .append(entry.getValue().replace("\"", "\\\""))
                    .append("\"),").append(newline);
        }
        if (!req.headers.isEmpty()) {
            code.setLength(code.length() - (newline.length() + 1));
        }
        code.append(newline).append(");").append(newline).append(newline);

        if (req.isJson) {
            code.append("String jsonBody = \"\"\"").append(newline)
                    .append(req.body).append(newline)
                    .append("\"\"\";").append(newline).append(newline);

            code.append("HttpResponseWrapper response = client.postJson(").append(newline)
                    .append(indent).append("\"").append(req.url).append("\",").append(newline)
                    .append(indent).append("jsonBody,").append(newline)
                    .append(indent).append("headers").append(newline)
                    .append(");").append(newline);

        } else if (req.isFormUrlEncoded) {
            code.append("Map<String, String> formData = Map.of(").append(newline);
            String[] pairs = req.body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                code.append(indent).append("\"").append(key).append("\", \"").append(value).append("\",").append(newline);
            }
            code.setLength(code.length() - (newline.length() + 1));
            code.append(newline).append(");").append(newline).append(newline);

            code.append("HttpResponseWrapper response = client.postForm(").append(newline)
                    .append(indent).append("\"").append(req.url).append("\",").append(newline)
                    .append(indent).append("formData,").append(newline)
                    .append(indent).append("headers").append(newline)
                    .append(");").append(newline);

        } else if ("GET".equalsIgnoreCase(req.method)) {
            code.append("HttpResponseWrapper response = client.get(").append(newline)
                    .append(indent).append("\"").append(req.url).append("\",").append(newline)
                    .append(indent).append("headers").append(newline)
                    .append(");").append(newline);
        } else {
            String rawBody = req.body != null ? req.body : "";
            code.append("String body = \"\"\"").append(newline)
                    .append(rawBody).append(newline)
                    .append("\"\"\";").append(newline).append(newline);

            code.append("HttpResponseWrapper response = client.")
                    .append(req.method.toLowerCase()).append("(").append(newline)
                    .append(indent).append("\"").append(req.url).append("\",").append(newline)
                    .append(indent).append("body,").append(newline)
                    .append(indent).append("headers").append(newline)
                    .append(");").append(newline);
        }

        return code.toString();
    }
}
