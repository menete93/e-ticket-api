package mz.co.mozbuy.common.api;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class APIResponse {

    private Integer statusCode;
    private String result;
    private String errorMessage;
    private Map<String, String> headers = new HashMap<>();
    private Long durationMs;
    private boolean success;

    public APIResponse(Integer statusCode, String result) {
        this.statusCode = statusCode;
        this.result = result;
        this.success = statusCode != null && (statusCode >= 200 && statusCode < 300);
    }

    public APIResponse(Integer statusCode, String result, String errorMessage) {
        this.statusCode = statusCode;
        this.result = result;
        this.errorMessage = errorMessage;
        this.success = statusCode != null && (statusCode >= 200 && statusCode < 300);
    }

    public boolean isSuccessful() {
        return success;
    }
}