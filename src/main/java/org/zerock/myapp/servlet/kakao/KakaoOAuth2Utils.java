package org.zerock.myapp.servlet.kakao;


public interface KakaoOAuth2Utils {
	public static final String APP_NAME="LoginTest";
	
	public static final String REST_API_KEY="";
	
	public static final String CLIENT_ID = REST_API_KEY;
	
	public static final String REDIRECT_URI = "https://localhost/login/oauth2/code/kakao";

	public static final String REQUEST_URI = "https://kauth.kakao.com/oauth/authorize";
	
	public static final String TOKEN_URI="https://kauth.kakao.com/oauth/token";
	
	public static final String PROFILE_URI="https://kapi.kakao.com/v2/user/me";
	
	public static final String RESPONSE_TYPE="code";
	
	public static final String GRANT_TYPE="authorization_code";
} // end class
