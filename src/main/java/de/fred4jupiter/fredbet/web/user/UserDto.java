package de.fred4jupiter.fredbet.web.user;

public class UserDto {

	private final Long userId;
	private final String username;
	private String displayName;
	private String userProfileImageKey;

	public UserDto(Long userId, String username, String displayname) {
		this.userId = userId;
		this.username = username;
		this.displayName = displayName;
	}

	public UserDto(Long userId, String username, String displayname, String userProfileImageKey) {
		this(userId, username, displayname);
		this.userProfileImageKey = userProfileImageKey;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUserProfileImageKey() {
		return userProfileImageKey;
	}

}
