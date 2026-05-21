package mz.co.mozbuy.e_ticket.event.core.exceptions;



public class SystemException extends RuntimeException {

    private String code;
    private String platform;

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public SystemException(String message, Throwable cause, String platform) {
        super(message, cause);
        this.platform = platform;
    }

    public SystemException(String code, String message, Throwable cause, String platform) {
        super(message, cause);
        this.code = code;
        this.platform = platform;
    }

    public String getCode() {
        return code;
    }

    public String getPlatform() {
        return platform;
    }
}