//package mz.co.mozbuy.e_ticket.event.auth.config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcOperations;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//
//import javax.sql.DataSource;
//import java.util.UUID;
//
//@Configuration
//public class AuthorizationServerConfig {
//
//    @Bean
//    public JdbcOperations jdbcOperations(DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//
//    @Bean
//    public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations, PasswordEncoder passwordEncoder) {
//        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcOperations);
//
//        // Cria um cliente se ele ainda não existir
//        if (repository.findByClientId("meu-servico") == null) {
//            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                    .clientId("meu-servico")
//                    .clientSecret(passwordEncoder.encode("segredo123"))
//                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                    .scope("ticket:write")
//                    .build();
//
//            repository.save(registeredClient);
//        }
//
//        return repository;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }
//}
