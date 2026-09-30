package es.ies.reyes.gestion.banos.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.catalog-username}") String catalogUsername,
            @Value("${app.auth.catalog-password}") String catalogPassword,
            @Value("${app.auth.permissions-username}") String permissionsUsername,
            @Value("${app.auth.permissions-password}") String permissionsPassword) {
        if (catalogUsername.isBlank() || permissionsUsername.isBlank()
                || catalogPassword.length() < 12 || permissionsPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "Los usuarios no pueden estar vacíos y las contraseñas deben tener al menos 12 caracteres");
        }
        if (catalogUsername.equals(permissionsUsername)) {
            throw new IllegalArgumentException("Los nombres de usuario de cada perfil deben ser distintos");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(catalogUsername)
                        .password(passwordEncoder.encode(catalogPassword))
                        .roles("GESTION_CATALOGOS")
                        .build(),
                User.withUsername(permissionsUsername)
                        .password(passwordEncoder.encode(permissionsPassword))
                        .roles("GESTION_PERMISOS")
                        .build());
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/alumnos/**", "/api/profesores/**",
                                "/api/grupos/**", "/api/franjas-horarias/**").hasRole("GESTION_CATALOGOS")
                        .requestMatchers(HttpMethod.PUT, "/api/alumnos/**", "/api/profesores/**",
                                "/api/grupos/**", "/api/franjas-horarias/**").hasRole("GESTION_CATALOGOS")
                        .requestMatchers(HttpMethod.DELETE, "/api/alumnos/**", "/api/profesores/**",
                                "/api/grupos/**", "/api/franjas-horarias/**").hasRole("GESTION_CATALOGOS")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, cause) ->
                                response.sendError(401, "Autenticación requerida"))
                        .accessDeniedHandler((request, response, cause) ->
                                response.sendError(403, "No tienes permiso para realizar esta operación")))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }
}
