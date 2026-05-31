package de.fred4jupiter.fredbet.web.profile;

import de.fred4jupiter.fredbet.domain.entity.AppUser;
import de.fred4jupiter.fredbet.security.SecurityService;
import de.fred4jupiter.fredbet.user.OldPasswordWrongException;
import de.fred4jupiter.fredbet.user.RenameUsernameNotAllowedException;
import de.fred4jupiter.fredbet.user.UserAdministrationService;
import de.fred4jupiter.fredbet.user.UserAlreadyExistsException;
import de.fred4jupiter.fredbet.web.WebMessageUtil;
import de.fred4jupiter.fredbet.web.WebSecurityUtil;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    private static final String CHANGE_PASSWORD_PAGE = "profile/change_password";

    private static final String CHANGE_USERNAME_PAGE = "profile/change_username";

    private final UserAdministrationService userAdministrationService;

    private final WebMessageUtil webMessageUtil;

    private final WebSecurityUtil webSecurityUtil;

    private final SecurityService securityService;

    public UserProfileController(UserAdministrationService userAdministrationService,
                                 WebMessageUtil webMessageUtil, WebSecurityUtil webSecurityUtil,
                                 SecurityService securityService) {
        this.userAdministrationService = userAdministrationService;
        this.webMessageUtil = webMessageUtil;
        this.webSecurityUtil = webSecurityUtil;
        this.securityService = securityService;
    }

    @GetMapping("/changePassword")
    public String changePassword(@ModelAttribute ChangePasswordCommand changePasswordCommand, Model model) {
        if (webSecurityUtil.isUsersFirstLogin()) {
            webMessageUtil.addWarnMsg(model, "user.changePassword.firstLogin");
        }

        model.addAttribute("ssoUser", securityService.getCurrentUser().isSsoUser());
        return CHANGE_PASSWORD_PAGE;
    }

    @PostMapping("/changePassword")
    public String changePasswordPost(@Valid ChangePasswordCommand changePasswordCommand, BindingResult bindingResult,
                                    RedirectAttributes redirect, Model model) {
        AppUser currentUser = securityService.getCurrentUser();

        if (bindingResult.hasErrors()) {
           model.addAttribute("ssoUser", currentUser.isSsoUser());
           return CHANGE_PASSWORD_PAGE;
        }

        try {
            if (currentUser.isSsoUser()) {
                userAdministrationService.changePasswordForSsoUser(currentUser.getId(), changePasswordCommand.getNewPassword());
            } else {
                userAdministrationService.changePassword(currentUser.getId(), changePasswordCommand.getOldPassword(), changePasswordCommand.getNewPassword());
            }
        } catch (OldPasswordWrongException e) {
            webMessageUtil.addErrorMsg(model, "msg.bet.betting.error.oldPasswordWrong");
            model.addAttribute("changePasswordCommand", changePasswordCommand);
            model.addAttribute("ssoUser", currentUser.isSsoUser());
            return CHANGE_PASSWORD_PAGE;
        }

        webMessageUtil.addInfoMsg(redirect, "msg.user.profile.info.passwordChanged");
        return "redirect:/matches";
    }

    @GetMapping("/changeUsername")
    public String changeUsername(@ModelAttribute ChangeUsernameCommand changeUsernameCommand) {
        return CHANGE_USERNAME_PAGE;
    }

    @PostMapping("/changeUsername")
    public String changeUsernamePost(@Valid ChangeUsernameCommand changeUsernameCommand, BindingResult bindingResult,
                                     RedirectAttributes redirect, Model model) {
        AppUser currentUser = securityService.getCurrentUser();
        if (bindingResult.hasErrors()) {
            return CHANGE_USERNAME_PAGE;
        }

        try {
            userAdministrationService.renameUser(currentUser.getUsername(), changeUsernameCommand.getNewUsername());
        } catch (UserAlreadyExistsException e) {
            webMessageUtil.addErrorMsg(model, "user.username.duplicate");
            model.addAttribute("changeUsernameCommand", changeUsernameCommand);
            return "profile/change_username";
        } catch (RenameUsernameNotAllowedException e) {
            webMessageUtil.addErrorMsg(model, "user.username.changeNotAllowed");
            model.addAttribute("changeUsernameCommand", changeUsernameCommand);
            return "profile/change_username";
        }

        webMessageUtil.addInfoMsg(redirect, "msg.user.profile.info.usernameChanged");

        return "redirect:/profile/changeUsername";
    }
}
