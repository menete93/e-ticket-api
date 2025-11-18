package mz.co.mozbuy.common.security;


import java.util.List;

public class UserContext {

    private static final ThreadLocal<UserContext> currentUserContext = new ThreadLocal<>();

    private String username;
    private List<String> roles;
    private String token;

    public UserContext(String username, List<String> roles, String token) {
        this.username = username;
        this.roles = roles;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getToken() {
        return token;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public static void set(UserContext context) {
        currentUserContext.set(context);
    }

    public static UserContext get() {
        return currentUserContext.get();
    }

    public static void clear() {
        currentUserContext.remove();
    }
}
