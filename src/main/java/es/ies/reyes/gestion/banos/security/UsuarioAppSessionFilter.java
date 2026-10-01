package es.ies.reyes.gestion.banos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UsuarioAppSessionFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    public UsuarioAppSessionFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            actualizarSesion(authentication, request);
        }
        filterChain.doFilter(request, response);
    }

    private void actualizarSesion(Authentication authentication, HttpServletRequest request) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(authentication.getName());
            UsernamePasswordAuthenticationToken actualizada = UsernamePasswordAuthenticationToken.authenticated(
                    user,
                    null,
                    user.getAuthorities()
            );
            actualizada.setDetails(authentication.getDetails());
            SecurityContextHolder.getContext().setAuthentication(actualizada);
        } catch (UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
        }
    }
}
