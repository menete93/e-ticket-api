package mz.co.mozbuy.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String HEADER_USER = "X-User-Id"; // o nome do header que o gateway envia

    @Override
    public Optional<String> getCurrentAuditor() {
        // Pega o Request atual
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return Optional.of("system"); // fallback, por exemplo para jobs ou testes
        }

        HttpServletRequest request = attrs.getRequest();
        String username = request.getHeader(HEADER_USER);

        return Optional.of(username != null ? username : "system");
    }
}
