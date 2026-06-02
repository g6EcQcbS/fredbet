package de.fred4jupiter.fredbet.security;
import de.fred4jupiter.fredbet.domain.builder.AppUserBuilder;
import de.fred4jupiter.fredbet.domain.entity.AppUser;
import de.fred4jupiter.fredbet.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Collection;
import java.util.UUID;

/**
 * Loads the Keycloak OIDC user and maps the email claim to the local AppUser.
 * If no local user exists for the email, one is created on the fly with ROLE_USER.
 * Returns an OidcUser enriched with the local AppUser's authorities so Spring
 * Security grants the correct fredbet permissions.
 * Only active when fredbet.keycloak.enabled=true.
 */
@Service
@ConditionalOnProperty(name = "fredbet.keycloak.enabled", havingValue = "true")
public class KeycloakOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakOAuth2UserService.class);

    private final OidcUserService delegate = new OidcUserService();

    private final FredbetUserDetailsService userDetailsService;

    private final UserService userService;

    public KeycloakOAuth2UserService(FredbetUserDetailsService userDetailsService, UserService userService) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("missing_email"), "No email claim found in Keycloak token");
        }

        LOG.debug("SSO login attempt for email: {}", email);

        UserDetails localUser;
        try {
            localUser = userDetailsService.loadUserByUsername(email);
            LOG.info("SSO login for existing user: {}", localUser.getUsername());
        } catch (UsernameNotFoundException e) {
            localUser = createUserFromOidc(oidcUser, email);
            LOG.info("SSO created new user: {}", localUser.getUsername());
        }

        // Merge local authorities into the OidcUser so Spring Security
        // grants the correct fredbet roles and permissions
        Collection<? extends GrantedAuthority> localAuthorities = localUser.getAuthorities();
        return new DefaultOidcUser(localAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private UserDetails createUserFromOidc(OidcUser oidcUser, String email) {
        String displayName = resolveDisplayName(oidcUser);
        // Use a random password — SSO users never log in with password
        String randomPassword = UUID.randomUUID().toString();

        AppUser newUser = AppUserBuilder.create()
            .withUsernameAndPassword(email, randomPassword)
            .withDisplayName(displayName)
            .withSsoUser(true)
            .withFirstLogin(false)
            .build();

        userService.createUser(newUser);
        return userDetailsService.loadUserByUsername(email);
    }

    private String resolveDisplayName(OidcUser oidcUser) {
        String fullName = oidcUser.getFullName();
        if (StringUtils.hasText(fullName)) {
            return fullName;
        }
        String givenName = oidcUser.getGivenName();
        String familyName = oidcUser.getFamilyName();
        if (StringUtils.hasText(givenName) || StringUtils.hasText(familyName)) {
            return ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
        }
        return null;
    }

}
 
