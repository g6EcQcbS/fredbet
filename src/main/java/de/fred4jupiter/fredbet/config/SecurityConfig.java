package de.fred4jupiter.fredbet.config;

import de.fred4jupiter.fredbet.security.FredBetPermission;
import de.fred4jupiter.fredbet.security.KeycloakOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.h2console.autoconfigure.H2ConsoleProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final int REMEMBER_ME_TOKEN_VALIDITY_SECONDS = 24 * 60 * 60; // 24 hours

    private final Optional<H2ConsoleProperties> h2ConsoleProperties;

    private final Optional<KeycloakOAuth2UserService> keycloakOAuth2UserService;

    public SecurityConfig(Optional<H2ConsoleProperties> h2ConsoleProperties, Optional<KeycloakOAuth2UserService> keycloakOAuth2UserService) {
        this.h2ConsoleProperties = h2ConsoleProperties;
        this.keycloakOAuth2UserService = keycloakOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PersistentTokenRepository persistentTokenRepository) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/actuator/**", "/blueimpgallery/**", "/lightbox/**", "/flag-icons*/**",
                    "/club-wm-icons*/**", "/fonts/**", "/login/**", "/logout", "/registration",
                    "/webjars/**", "/css/**", "/js/**").permitAll()
                .requestMatchers("/user/**").hasAnyAuthority(FredBetPermission.PERM_USER_ADMINISTRATION)
                .requestMatchers("/admin/**", "/administration/**").hasAnyAuthority(FredBetPermission.PERM_ADMINISTRATION)
                .anyRequest().authenticated()
            )
            .rememberMe(remember -> remember.tokenRepository(persistentTokenRepository).tokenValiditySeconds(REMEMBER_ME_TOKEN_VALIDITY_SECONDS))
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/matches/upcoming")
                .failureUrl("/login/error"));

        keycloakOAuth2UserService.ifPresent(userService ->  {
            try {
                http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/matches/upcoming")
                    .failureUrl("/login/error")
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(userService)));
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure OAuth2 login", e);
            }
        });

        http.logout(logout -> logout.logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me"))
            .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable));

        if (h2ConsoleProperties.isPresent() && h2ConsoleProperties.get().isEnabled()) {
            // this is for the embedded h2 console
            http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
            http.csrf(csrf -> csrf.ignoringRequestMatchers("/console/**"));
        }

        return http.build();
    }

    /**
     * Register HttpSessionEventPublisher only when Spring Session Redis is NOT on the classpath.
     * When Redis session is active, Spring Session registers its own publisher automatically.
     */
    @Bean
    @ConditionalOnMissingClass("org.springframework.session.data.redis.RedisIndexedSessionRepository")
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new JdbcTokenRepositoryImpl();
        jdbcTokenRepositoryImpl.setDataSource(dataSource);
        return jdbcTokenRepositoryImpl;
    }

}
