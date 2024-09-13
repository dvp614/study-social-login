package org.zerock.myapp.servlet.kakao;

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

@WebServlet("/KakaoLogin")
public final class KakaoLoginServlet extends HttpServlet {
	@Serial private static final long serialVersionUID = 1L;

	// KAKAO 의 인증화면(아이디와 암호입력화면)을 요청하는 서블릿
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		log.trace("service(req, res) invoked.");
		
		try {
			// Step1. 카카오의 인증창을 요청할 URL을 필수전송파라미터와 함께 구성
			String kakaoOAuth2URL = 
				KakaoOAuth2Utils.REQUEST_URI + '?' +
				"response_type=" + KakaoOAuth2Utils.RESPONSE_TYPE + '&' +
				"client_id=" + KakaoOAuth2Utils.CLIENT_ID + '&' +
				"redirect_uri=" + KakaoOAuth2Utils.REDIRECT_URI;
			
			log.info("\t+ kakaoOAuth2URL: {}", kakaoOAuth2URL);
			
			// Step2. 인증창 요청 with Step1's URL.
			res.sendRedirect(kakaoOAuth2URL);
		}catch (Exception e) {
			throw new IOException(e);
		} // try-catch
	} // service

} // end class
