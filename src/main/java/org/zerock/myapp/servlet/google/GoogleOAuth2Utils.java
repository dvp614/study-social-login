package org.zerock.myapp.servlet.google;


public interface GoogleOAuth2Utils {
	public static final String CLIENT_NAME = "Yoon";
	
	public static final String CLIENT_ID = 
			"";
	public static final String CLIENT_SECRET = "";
	
	public static final String AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/auth";
	public static final String REDIRECT_URI = "https://localhost/login/oauth2/code/google";	
	public static final String TOKEN_URI = "https://oauth2.googleapis.com/token";	
	public static final String USERINFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";
	
	public static final String PROJECT_ID = "engaged-wonder-428804-k4";
	public static final String AUTH_PROVIDER_X509_CERT_URL = 
			"https://www.googleapis.com/oauth2/v1/certs";
	
	public static final String SCOPE = 
			"https://www.googleapis.com/auth/userinfo.email" 
					+ " https://www.googleapis.com/auth/userinfo.profile";
	public static final String RESPONSE_TYPE = "code";
} // end class
