package de.fred4jupiter.fredbet.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * This handler will redirect the user to the password change page if its the
 * first login.
 * 
 * @author michael
 *
 */
public class ChangePasswordFirstLoginInterceptor implements HandlerInterceptor {

	private static final String CHANGE_PASSWORD_ENDPOINT = "/profile/changePassword";

	private final WebSecurityUtil webSecurityUtil;

	public ChangePasswordFirstLoginInterceptor(WebSecurityUtil webSecurityUtil) {
		this.webSecurityUtil = webSecurityUtil;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (!webSecurityUtil.isChangePasswordOnFirstLogin()) {
			return true;
		}

		final String requestURI = request.getRequestURI();
		if (requestURI.contains(CHANGE_PASSWORD_ENDPOINT)) {
			return true;
		}

		if (!response.isCommitted() && webSecurityUtil.isUserLoggedIn() && webSecurityUtil.isUsersFirstLogin()) {
 			response.sendRedirect(CHANGE_PASSWORD_ENDPOINT);
			return false;
 		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
		// redirect is now handled in preHandle
 	}
}
