package org.zerock.myapp.servlet.naver;

import java.util.UUID;

public interface NaverOAuth2Utils {
	public static final String APP_NAME = "LoginTest";
	public static final String CLIENT_ID = "";
	public static final String CLIENT_SECRET = "";
	public static final String REDIRECT_URL = "https://localhost/login/oauth2/code/naver";

	public static final String OAUTH2_REQUEST_URI = "https://nid.naver.com/oauth2.0/authorize";
	public static final String TOKEN_REQUEST_URI = "https://nid.naver.com/oauth2.0/token";
	public static final String PROFILE_REQUEST_URI = "https://openapi.naver.com/v1/nid/me";
	
	public static final String RESPONSE_TYPE = "code";
	public static final String GRANT_TYPE = "authorization_code";
	public static final String STATE = UUID.randomUUID().toString();
} // end class
