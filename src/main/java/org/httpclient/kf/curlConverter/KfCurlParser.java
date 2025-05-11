package org.httpclient.kf.curlConverter;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KfCurlParser {

    public static KfCurlRequest parse(String curl) {
        KfCurlRequest req = new KfCurlRequest();
        req.headers = new LinkedHashMap<>();

        curl = curl.replaceAll("\\\\\n", " ")
                .replaceAll("\\\\r\\\\n", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        Matcher urlMatcher = Pattern.compile("curl ['\"]?(https?://[^\\s'\"]+)['\"]?").matcher(curl);
        if (urlMatcher.find()) req.url = urlMatcher.group(1);

        if (curl.contains("-X POST")) req.method = "POST";
        else if (curl.contains("-X PUT")) req.method = "PUT";
        else if (curl.contains("-X PATCH")) req.method = "PATCH";
        else if (curl.contains("-X DELETE")) req.method = "DELETE";
        else if (curl.contains("--data") || curl.contains("--data-raw") || curl.contains("--data-binary")) req.method = "POST";
        else req.method = "GET";

        Matcher headerMatcher = Pattern.compile("-H ['\"](.*?):\\s*(.+?)['\"](?=\\s+-[HbIX] |$)").matcher(curl);
        while (headerMatcher.find()) {
            String name = headerMatcher.group(1).trim();
            String value = headerMatcher.group(2).trim();
            req.headers.put(name, value);
        }

        Matcher cookieMatcher = Pattern.compile("-b ['\"](.+?)['\"]").matcher(curl);
        if (cookieMatcher.find()) {
            req.headers.put("Cookie", cookieMatcher.group(1).trim());
        }

        Matcher dataMatcher = Pattern.compile("--data(?:-raw)? ['\"](.*?)['\"]").matcher(curl);
        if (dataMatcher.find()) {
            req.body = dataMatcher.group(1).trim();
        }

        String contentType = req.headers.getOrDefault("content-type", "").toLowerCase(Locale.ROOT);
        req.isJson = contentType.contains("application/json");
        req.isFormUrlEncoded = contentType.contains("application/x-www-form-urlencoded");

        return req;
    }
}
