package mz.co.mozbuy.common.api;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import mz.co.mozbuy.common.enums.APIMethodType;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIContext {

    // Configurações de autenticação
    private String apiKey;
    private String publicKey;
    private String bearerToken;
    private String username;
    private String password;

    // Configurações de conexão
    private Boolean ssl = true;
    private String address;
    private Integer port;
    private String path;
    private Integer connectionTimeout = 30000; // 30 segundos
    private Integer readTimeout = 30000;       // 30 segundos

    // Configurações da requisição
    private APIMethodType methodType = APIMethodType.POST;
    private String contentType = "application/json";
    private String accept = "application/json";

    // Headers e parâmetros
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private Object body;

    // Configurações de proxy
    private String proxyHost;
    private Integer proxyPort;

    public void addHeader(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
    }

    public void addParameter(String key, String value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }

    public void addHeaders(Map<String, String> headers) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.putAll(headers);
    }

    public void addParameters(Map<String, String> parameters) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.putAll(parameters);
    }

    public String getFullUrl() {
        String protocol = Boolean.TRUE.equals(ssl) ? "https://" : "http://";
        String baseUrl = address;
        if (port != null && port > 0) {
            baseUrl = address + ":" + port;
        }
        return protocol + baseUrl + (path != null ? path : "");
    }
}