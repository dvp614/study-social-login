package org.zerock.myapp.servlet.naver;

import java.io.IOException;
import java.io.Serial;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor

@WebServlet("/NaverLogin")
public final class NaverLoginServlet extends HttpServlet {
	@Serial private static final long serialVersionUID = 1L;

	// Naver 의 인증화면(아이디와 암호입력화면)을 요청하는 서블릿
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		log.trace("service(req, res) invoked.");
		
		try {
			// Step1. 네이버의 로그인창을 띄워줄 수 있는 URL 주소와 필요한
			//		  전송파라미터로 구성된 최종 URL을 만든다.
			
			String requestNaverLoginURL = "%s?client_id=%s&redirect_uri=%s&response_type=%s&state=%s"
					.formatted(
							NaverOAuth2Utils.OAUTH2_REQUEST_URI,
							NaverOAuth2Utils.CLIENT_ID,
							NaverOAuth2Utils.REDIRECT_URL,
							NaverOAuth2Utils.RESPONSE_TYPE,
							NaverOAuth2Utils.STATE
					);
			
			log.info("\tStep1. requestNaverLoginURL : {}", requestNaverLoginURL);
			
			// Step2. Step1 에서 생성한 URL로 네이버의 인증창을 요청합니다.
			res.sendRedirect(requestNaverLoginURL);
		}catch (Exception e) {
			throw new IOException(e);
		} // try-catch
	} // service

} // end class
