package com.sribalafashion.config;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import com.sribalafashion.user.User;
import com.sribalafashion.user.UserRepository;

import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Value("${supabase.jwks-url}")
  private String jwksUrl;

  @Autowired
  private UserRepository userRepository;

  private volatile ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

  private ConfigurableJWTProcessor<SecurityContext> getJwtProcessor() {
    if (jwtProcessor == null) {
      synchronized (this) {
        if (jwtProcessor == null) {
          try {
            JWKSource<SecurityContext> keySource = JWKSourceBuilder.create(new URI(jwksUrl).toURL()).build();
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, keySource));
            this.jwtProcessor = processor;
          } catch (Exception e) {
            logger.error("Failed to initialize JWKS processor: {}", e.getMessage());
            throw new RuntimeException("Cannot init JWKS processor", e);
          }
        }
      }
    }
    return jwtProcessor;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if (jwt != null) {
        // Verify token using Supabase JWKS public keys
        JWTClaimsSet claims = getJwtProcessor().process(jwt, null);

        String email = claims.getStringClaim("email");
        if (email == null) {
          // Fallback: some Supabase tokens put email in sub
          email = claims.getSubject();
        }

        if (email != null) {
          // Look up user in our database to get their role
          Optional<User> userOpt = userRepository.findByEmail(email);
          String role = "CUSTOMER";
          if (userOpt.isPresent()) {
            role = userOpt.get().getRole().name();
          }

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              email,
              null,
              Collections.singletonList(
                  new SimpleGrantedAuthority("ROLE_" + role)));

          authentication.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");

    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }

    return null;
  }
}
