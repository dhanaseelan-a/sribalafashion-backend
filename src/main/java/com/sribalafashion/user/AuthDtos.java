package com.sribalafashion.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
class GoogleAuthRequest {
	@NotBlank
	private String accessToken;
}

@Data
class MessageResponse {
	private String message;

	public MessageResponse(String message) {
		this.message = message;
	}
}

@Data
@AllArgsConstructor
class UserInfoResponse {
	private String email;
	private String role;
	private String fullName;
}
