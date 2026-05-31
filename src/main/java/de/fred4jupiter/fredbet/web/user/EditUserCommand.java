package de.fred4jupiter.fredbet.web.user;

import de.fred4jupiter.fredbet.props.FredbetConstants;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EditUserCommand {

	private Long userId;

	@NotEmpty
	@Size(min = 2, max = FredbetConstants.USERNAME_MAX_LENGTH)
	private String username;

	@Size(min = 2, max = FredbetConstants.USERNAME_MAX_LENGTH)
	private String displayName;

	private boolean deletable;

	@NotEmpty
	private Set<String> roles = new HashSet<>();

	private boolean child;

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public void addRole(String role) {
		roles.add(role);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
		builder.append("userId", userId);
		builder.append("roles", roles);
		return builder.toString();
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public boolean isChild() {
		return child;
	}

	public void setChild(boolean child) {
		this.child = child;
	}

}
