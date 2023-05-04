package com.kh.learnovation.domain.user.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown=true) 
@Data
public class KakaoProfile {
	public Long id;
	public String connected_at;
	public Properties properties;
	public KakaoAccount kakao_account;

	@Data
	@Getter
	public class Properties {
		public String nickname;
		public String profile_image;
		public String thumbnail_image;

	}

	@JsonIgnoreProperties(ignoreUnknown=true) 
	@Data
	public class KakaoAccount {
		public Boolean profile_Nickname_Needs_Agreement;
		public Boolean profile_Image_Needs_Agreement;
		public Profile profile;
		public Boolean has_email;
		public Boolean email_needs_agreement;
		public Boolean is_email_valid;
		public Boolean is_email_verified;
		public String email;

		@JsonIgnoreProperties(ignoreUnknown=true) 
		@Data
		public class Profile {
			public String nickname;
			public String thumbnail_image_url;
			public String profile_image_url;
			public Boolean is_default_image;
		}
	}
}
