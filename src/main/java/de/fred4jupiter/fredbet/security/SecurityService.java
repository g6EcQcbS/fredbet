package de.fred4jupiter.fredbet.security;

import de.fred4jupiter.fredbet.domain.entity.AppUser;
import de.fred4jupiter.fredbet.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Provides security information of the current user.
 *
 * @author mstaehler
 */
@Service
public class SecurityService {

    private final AppUserRepository appUserRepository;

    public SecurityService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public boolean isUserLoggedIn() {
        try {
            getCurrentUser();
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    public String getCurrentUserName() {
        return getCurrentUser().getUsername();
    }

    public boolean isCurrentUserHavingPermission(String permission) {
        return getCurrentUser().hasPermission(permission);
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User is not logged in!");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AppUser appUser) {
            return appUser;
        }

        if (principal instanceof OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            AppUser appUser = appUserRepository.findByUsername(email);
            if (appUser == null) {
                throw new UsernameNotFoundException("No local user found for SSO email: " + email);
            }
            return appUser;
        }

        throw new UsernameNotFoundException("User is not logged in!");
    }

    public void resetFirstLogin(AppUser appUser) {
        appUser.setFirstLogin(false);

        try {
            AppUser currentUser = getCurrentUser();
            currentUser.setFirstLogin(false);
        } catch (UsernameNotFoundException e) {
            // ignore if user is not logged in
        }
    }
}
