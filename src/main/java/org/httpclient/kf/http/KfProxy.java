package org.httpclient.kf.http;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.util.Base64;

public class KfProxy {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public KfProxy(String host, int port) {
        this(host, port, null, null);
    }

    public KfProxy(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public ProxySelector toProxySelector() {
        return ProxySelector.of(new InetSocketAddress(host, port));
    }

    public boolean hasAuth() {
        return username != null && password != null;
    }

    public String getAuthHeader() {
        if (!hasAuth()) return null;
        String creds = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes());
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
