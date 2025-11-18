package mz.co.mozbuy.common.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String username = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-User-Roles"); // ex: "ADMIN,USER"
        String token = request.getHeader("X-User-Auth"); // ex: "Bearer eyJ..."

        List<String> roles = null;
        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            roles = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        if (username != null) {
            UserContext.set(new UserContext(username, roles, token));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
