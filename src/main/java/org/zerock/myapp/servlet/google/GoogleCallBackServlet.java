package org.zerock.myapp.servlet.google;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import lombok.extern.log4j.Log4j2;

@Log4j2
@WebServlet("/login/oauth2/code/google")
public final class GoogleCallBackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        log.trace("service(req,res) invoked.");

        try {
            // Step1. 구글이 보내온 모든 전송파라미터 로깅으로 출력
            Map<String, String[]> paramMap = req.getParameterMap();
            paramMap.forEach((k, v) -> log.info("Step1. param: {}({})", k, String.join(",", v)));

            // Step2. 인가코드 추출(전송파라미터명이 "code"로 전달)
            String code = req.getParameter("code");
            log.info("+ code : {}", code);

            // Step3. Access Token 요청 및 수신 with Step2의 인가코드
            Objects.requireNonNull(code);

            NetHttpTransport transport = new NetHttpTransport();
            JsonFactory factory = new GsonFactory();
            log.info("Step3. transport : {}, factory : {}", transport, factory);

            AuthorizationCodeTokenRequest tokenRequest = 
					new GoogleAuthorizationCodeTokenRequest(
							transport, 
							factory, 
							GoogleOAuth2Utils.CLIENT_ID, 
							GoogleOAuth2Utils.CLIENT_SECRET, 
							code, 
							GoogleOAuth2Utils.REDIRECT_URI
					);
            
            log.info("Step3. tokenRequest : {}", tokenRequest);

            TokenResponse tokenResponse = tokenRequest.execute();
            log.info("Step3. tokenResponse : {}", tokenResponse);

            // Step4. 수신한 tokenResponse 객체에서, OAuth2 Tokens을 추출합니다.
            Objects.requireNonNull(tokenResponse);

            String accessToken = tokenResponse.getAccessToken();
			String refreshToken = tokenResponse.getRefreshToken();
			String tokenType = tokenResponse.getTokenType();

            log.info("Step4. accessToken : {}, refreshToken : {}, tokenType : {}",
                    accessToken, refreshToken, tokenType);

            // Step5. Access Token으로 나의 정보를 Google에 달라고 요청하려면,
            //        추가로 Credential 객체를 만들어야 한다.
            //        여기서 Credential 객체는 Google 정한 것으로 만들어야 합니다.

			GoogleCredential credential = new GoogleCredential().setFromTokenResponse(tokenResponse	);

            Objects.requireNonNull(credential);
            Oauth2 oauth2 = new Oauth2.Builder(transport, factory, credential)
                    .setApplicationName(GoogleOAuth2Utils.CLIENT_NAME)
                    .build();

            log.info("Step5. oauth2 : {}", oauth2);

            Objects.requireNonNull(oauth2);
            Userinfo userInfo = oauth2.userinfo().get().execute();

            log.info("Step6. userInfo : {}", userInfo);

            Objects.requireNonNull(userInfo);

            res.setCharacterEncoding("utf8");
            res.setContentType("text/html; charset=utf8");

            PrintWriter out = res.getWriter();

            out.println("<ol>");
            out.println("<li>" + userInfo.getId() + "</li>");
            out.println("<li>" + userInfo.getHd() + "</li>");
            out.println("<li>" + userInfo.getGender() + "</li>");
            out.println("<li>" + userInfo.getGivenName() + "</li>");
            out.println("<li>" + userInfo.getFamilyName() + "</li>");
            out.println("<li>" + userInfo.getName() + "</li>");
            out.println("<li>" + userInfo.getEmail() + "</li>");
            out.println("<li>" + userInfo.getVerifiedEmail() + "</li>");
            out.println("<li>" + userInfo.getLocale() + "</li>");
            out.println("<li>" + userInfo.getPicture() + "</li>");
            out.println("</ol>");

        } catch (Exception e) {
            throw new IOException(e);
        } // try-catch
    } // service
} // end class
