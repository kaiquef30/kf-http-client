package org.httpclient.kf.chain;

import org.httpclient.kf.http.HttpResponseWrapper;
import org.httpclient.kf.http.KfHttpClient;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KfRequestChain {

    private final KfHttpClient client;
    private final List<KfRequestStep> steps = new ArrayList<>();
    private final Map<String, String> context = new HashMap<>();

    public KfRequestChain(KfHttpClient client) {
        this.client = client;
    }

    public KfRequestChain get(String url, Consumer<Map<String, String>> headerModifier) {
        steps.add(new KfRequestStep("GET", url, null, headerModifier));
        return this;
    }

    public KfRequestChain postJson(String url, String body, Consumer<Map<String, String>> headerModifier) {
        steps.add(new KfRequestStep("POST_JSON", url, body, headerModifier));
        return this;
    }

    public KfRequestChain postForm(String url, Map<String, String> formData, Consumer<Map<String, String>> headerModifier) {
        steps.add(new KfRequestStep("POST_FORM", url, formData, headerModifier));
        return this;
    }

    public KfRequestChain extract(String key, String regex) {
        steps.get(steps.size() - 1).setExtractor(key, regex);
        return this;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void run() throws IOException, InterruptedException {
        for (KfRequestStep step : steps) {
            Map<String, String> headers = new HashMap<>();
            step.applyHeaderModifier(headers);

            headers.replaceAll((k, v) -> replaceTokens(v));

            HttpResponseWrapper resp;
            switch (step.method) {
                case "GET":
                    resp = client.get(replaceTokens(step.url), headers);
                    break;
                case "POST_JSON":
                    resp = client.postJson(replaceTokens(step.url), replaceTokens(step.body.toString()), headers);
                    break;
                case "POST_FORM":
                    @SuppressWarnings("unchecked")
                    Map<String, String> formData = (Map<String, String>) step.body;
                    Map<String, String> resolvedForm = new HashMap<>();
                    for (Map.Entry<String, String> entry : formData.entrySet()) {
                        resolvedForm.put(entry.getKey(), replaceTokens(entry.getValue()));
                    }
                    resp = client.postForm(replaceTokens(step.url), resolvedForm, headers);
                    break;
                default:
                    throw new UnsupportedOperationException("Método não suportado: " + step.method);
            }

            if (step.extractKey != null && step.extractRegex != null) {
                Pattern pattern = Pattern.compile(step.extractRegex);
                Matcher matcher = pattern.matcher(resp.getBody());
                if (matcher.find()) {
                    context.put(step.extractKey, matcher.group(1));
                }
            }
        }
    }

    private String replaceTokens(String input) {
        if (input == null) return null;
        for (Map.Entry<String, String> entry : context.entrySet()) {
            input = input.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return input;
    }
}