package org.zerock.myapp.servlet.google;

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

@WebServlet("/GoogleLogin")
public final class GoogleLoginServlet extends HttpServlet {
	@Serial private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException, IOException {
		log.trace("service(req,res) invoked.");
		
		try {
			String requestGoogleloginURL = "%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=%s".
			formatted(
					GoogleOAuth2Utils.AUTHORIZATION_URI,
					GoogleOAuth2Utils.CLIENT_ID,
					GoogleOAuth2Utils.REDIRECT_URI,
					GoogleOAuth2Utils.RESPONSE_TYPE,
					GoogleOAuth2Utils.SCOPE
			);
			
			log.info("\t+ requestGoogleloginURL : {}", requestGoogleloginURL);
			
			res.sendRedirect(requestGoogleloginURL);
		}catch (Exception e) {
			throw new IOException(e);
		} // try-catch
	} // service

} // end class
