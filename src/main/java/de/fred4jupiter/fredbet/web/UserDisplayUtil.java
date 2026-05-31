package de.fred4jupiter.fredbet.web;

import de.fred4jupiter.fredbet.domain.entity.AppUser;
import de.fred4jupiter.fredbet.user.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserDisplayUtil {

    private static final Logger LOG = LoggerFactory.getLogger(UserDisplayUtil.class);

    private final AppUserRepository appUserRepository;

    public UserDisplayUtil(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public String getDisplayName(String username) {
        if (!StringUtils.hasText(username)) {
            return username;
        }

        try {
            AppUser user = appUserRepository.findByUsername(username);
            if (user == null) {
                return username;
            }
            return StringUtils.hasText(user.getDisplayName()) ? user.getDisplayName() : user.getUsername();
        } catch (Exception e) {
            LOG.warn("Could not resolve display name for user: {}", username, e);
            return username;
        }
    }
}
