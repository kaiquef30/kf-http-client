
# KfHttpClient

`KfHttpClient` é uma biblioteca Java leve, extensível e poderosa construída sobre `java.net.http.HttpClient`. Inspirada pelo Apache HttpClient e Postman, ela permite executar requisições HTTP complexas, encadeadas, com suporte a proxy, autenticação, retry, tracing, conversão de cURL e muito mais.

---

## ✨ Funcionalidades

- Requisições: `GET`, `POST (JSON/FORM)`, `PUT`, `DELETE`
- Encadeamento com contexto (`KfRequestChain`)
- Interceptadores (`KfInterceptor`)
- Proxy com autenticação (`KfProxy`)
- Retry automático com backoff (`KfRetryPolicy`)
- Debug/Tracing avançado (`KfHttpTracer`)
- Conversão de cURL (`KfCurlParser` + `KfCurlCodegen`)
- Extração leve de campos de JSON (`extractFieldFromJson`)

---

## ⚙️ Instalação

Inclua a lib no seu projeto Maven (em breve no Maven Central):

```xml
<dependency>
  <groupId>org.httpclient</groupId>
  <artifactId>kf-httpclient</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## ✅ Exemplo Simples (GET)

```java
KfHttpClient client = new KfHttpClient(KfConfig.defaultConfig());

HttpResponseWrapper response = client.get(
    "https://httpbin.org/get",
    Map.of("User-Agent", "KfHttpClient")
);

System.out.println(response.getBody());
```

---

## 📮 POST JSON

```java
String json = "{ "name": "kaique" }";

HttpResponseWrapper response = client.postJson(
    "https://httpbin.org/post",
    json,
    Map.of("Content-Type", "application/json")
);
```

---

## 📑 POST Formulário

```java
HttpResponseWrapper response = client.postForm(
    "https://httpbin.org/post",
    Map.of("username", "kaique", "password", "123"),
    Map.of("Content-Type", "application/x-www-form-urlencoded")
);
```

---

## 🔁 Retry Policy

```java
KfRetryPolicy retry = KfRetryPolicy.builder()
    .maxAttempts(5)
    .baseDelay(Duration.ofMillis(300))
    .strategy(KfRetryPolicy.Strategy.EXPONENTIAL)
    .retryOnStatus(429, 500)
    .build();

KfHttpClient client = new KfHttpClient(
    KfConfig.defaultConfig().withRetryPolicy(retry)
);
```

---

## 🔗 Encadeamento com `KfRequestChain`

```java
new KfRequestChain(client)
    .postJson("https://example.com/login", "{ "user": "x", "pass": "y" }",
        headers -> headers.put("Content-Type", "application/json"))
    .extract("token", ""token"\s*:\s*"([^"]+)"")
    .get("https://example.com/data",
        headers -> headers.put("Authorization", "Bearer {{token}}"))
    .run();
```

---

## 🛰️ Tracing com `KfDebugTracer`

```java
KfHttpClient client = new KfHttpClient(
    KfConfig.defaultConfig().withTracer(new KfDebugTracer())
);
```

Saída:
```
[DEBUG] POST https://example.com/login (145ms)
Status: 200
Headers: {...}
Body: {...}
```

---

## 🌐 Proxy com autenticação

```java
KfProxy proxy = new KfProxy("177.91.115.220", 8080, "user", "pass");

KfHttpClient client = new KfHttpClient(
    KfConfig.defaultConfig().withProxy(proxy)
);
```

---

## 🔎 Converter cURL para Java

```java
String curl = "curl -X POST 'https://example.com' -H 'Content-Type: application/json' --data '{"x":1}'";

KfCurlRequest parsed = KfCurlParser.parse(curl);
String javaCode = KfCurlCodegen.toJava(parsed, true);

System.out.println(javaCode);
```

---

## 🔍 Extrair campo JSON

```java
String token = client.extractFieldFromJson(response.getBody(), "token");
```

---

## 🧩 Interceptor Global

```java
client = new KfHttpClient(
    KfConfig.defaultConfig().withInterceptor(new KfInterceptor() {
        public void beforeSend(HttpRequest req) {
            System.out.println("Interceptando request: " + req.uri());
        }
        public void afterReceive(HttpResponse<?> res) {
            System.out.println("Status: " + res.statusCode());
        }
    })
);
```

---

## 📂 Estrutura de Pacotes

```
org.httpclient
├── kf.http
│   ├── KfHttpClient
│   ├── KfConfig
│   ├── KfInterceptor
│   ├── KfProxy
│   └── HttpResponseWrapper
├── kf.trace
│   ├── KfHttpTracer
│   └── KfDebugTracer
├── kf.retry
│   └── KfRetryPolicy
├── kf.chain
│   ├── KfRequestChain
│   └── KfRequestStep
└── kf.curlConverter
    ├── KfCurlParser
    ├── KfCurlCodegen
    └── KfCurlRequest
```

---

## © Autor

Desenvolvido com agressividade e eficiência por **Kaique Fernando**.
