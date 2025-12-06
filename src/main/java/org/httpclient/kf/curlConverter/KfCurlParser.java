package org.httpclient.kf.curlConverter;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KfCurlParser {

    public static KfCurlRequest parse(String curl) {
        Map<String, String> headers = new LinkedHashMap<>();

        curl = curl.replaceAll("\\\\\n", " ")
                .replaceAll("\\\\r\\\\n", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        Matcher urlMatcher = Pattern.compile("curl ['\"]?(https?://[^\\s'\"]+)['\"]?")
                .matcher(curl);
        String url = null;
        if (urlMatcher.find()) {
            url = urlMatcher.group(1);
        }
        
        String method;
        if (curl.contains("-X POST")) method = "POST";
        else if (curl.contains("-X PUT")) method = "PUT";
        else if (curl.contains("-X PATCH")) method = "PATCH";
        else if (curl.contains("-X DELETE")) method = "DELETE";
        else if (curl.contains("--data")
                || curl.contains("--data-raw")
                || curl.contains("--data-binary")) method = "POST";
        else method = "GET";

        Matcher headerMatcher = Pattern.compile(
                        "-H ['\"](.*?):\\s*(.+?)['\"](?=\\s+-[HbIX] |$)")
                .matcher(curl);
        while (headerMatcher.find()) {
            String name = headerMatcher.group(1).trim();
            String value = headerMatcher.group(2).trim();
            headers.put(name.toLowerCase(Locale.ROOT), value);
        }

        Matcher cookieMatcher = Pattern.compile("-b ['\"](.+?)['\"]")
                .matcher(curl);
        if (cookieMatcher.find()) {
            headers.put("cookie", cookieMatcher.group(1).trim());
        }

        Matcher dataMatcher = Pattern.compile("--data(?:-raw)? ['\"](.*?)['\"]")
                .matcher(curl);
        String body = null;
        if (dataMatcher.find()) {
            body = dataMatcher.group(1).trim();
        }

        String contentType = headers
                .getOrDefault("content-type", "")
                .toLowerCase(Locale.ROOT);
        boolean isJson = contentType.contains("application/json");
        boolean isFormUrlEncoded = contentType.contains("application/x-www-form-urlencoded");
        return new KfCurlRequest(method, url, headers, body, isJson, isFormUrlEncoded);
    }
}
