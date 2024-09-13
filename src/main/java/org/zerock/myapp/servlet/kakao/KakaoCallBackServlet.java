package org.zerock.myapp.servlet.kakao;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

@Log4j2
@WebServlet("/login/oauth2/code/kakao")
public final class KakaoCallBackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        log.trace("service(req,res) invoked.");

        try {
        	// 수신받은 모든 전송파라미터는 콘솔에 차례대로 다 출력해 놓자!!
			Map<String, String[]> paramMap = req.getParameterMap();
			paramMap.forEach((k, v) -> log.info("\t+ param({}, {})", k, v));
			
			// ------------
			// Step1. 전송파라미터(code)로 들어온 "인가코드" 수신
			// ------------
			String code = req.getParameter(KakaoOAuth2Utils.RESPONSE_TYPE);
			log.info("\tStep1. code: {}", code);
			
			// ------------
			// Step2. Step1의 인가코드를 이용하여, Token 서버에 전송할 URL 생성
			// ------------
			Objects.requireNonNull(code);
			
			final String tokenURL = KakaoOAuth2Utils.TOKEN_URI;
			final String queryString = 
				"code=%s&client_id=%s&redirect_uri=%s&grant_type=%s"
				.formatted(
					code, 
					KakaoOAuth2Utils.CLIENT_ID, 
					KakaoOAuth2Utils.REDIRECT_URI, 
					KakaoOAuth2Utils.GRANT_TYPE
				);
			
			log.info("\tStep2. tokenURL: {}", tokenURL);
			log.info("\tStep2. queryString: {}", queryString);

			// ------------
			// Step3. Step2에서 정의한 변수를 이용하여, 
			//		  Token 서버에 요청을 전송하여 Access/Refresh Token 획득
			// ------------
			Objects.requireNonNull(tokenURL);
			Objects.requireNonNull(queryString);
			
			// 3-1. tokenURL을 이용하여, URL 객체를 생성
			URL tokenUrl = new URL(tokenURL);
			
			log.info("\tStep3. tokenUrl: {}", tokenUrl);
			
			// 3-2. URL 객체인 tokenUrl로부터, 지정된 서버에 대한 연결(Connection) 생성
			Objects.requireNonNull(tokenUrl);
			URLConnection tokenUrlconn = tokenUrl.openConnection();
			
			log.info("\tStep3. tokenUrlconn: {}", tokenUrlconn);
			
			// 3-3. 3-2 에서 얻은 URLConnection 으로부터, 입/출력스트림을 얻어서
			//      HTTP 프로토콜대로, HTTP request / response 를 송/수신합니다.
			Objects.requireNonNull(tokenUrlconn);
			
			if(tokenUrlconn instanceof HttpURLConnection httpTokenUrlConn) {
				log.info("\tStep3. httpTokenUrlConn: {}", httpTokenUrlConn);
								
				// 3-5. 입/출력 설정
				httpTokenUrlConn.setRequestMethod("POST");
				
				// 기본으로, HttpURLConnection은 입력스트림만 사용가능하도록 설정되어
				// 있습니다. 하지만, 얼마든지 아래설정메소드로 출력스트림도 사용하도록
				// 설정할 수 있습니다.
				httpTokenUrlConn.setDoOutput(true);	// OutputStream 활성화
				
				// 3-6. 3-4에서 생성한 URL커넥션으로부터, 
				//      출력스트림을 얻어내어(HTTP request 송신위해서), 
				//      Query String을 타겟서버로 전송
				@Cleanup OutputStream os = httpTokenUrlConn.getOutputStream();
				os.write(queryString.getBytes());	// 바가지 채로 부어 버려라!!!
				os.flush();
				
				// 3-7. 3-4에서 생성한 URL커넥션으로부터,
				//      입력스트림을 얻어내어(HTTP response 수신위해서),
				//      타겟토큰서버에서 보내오는 응답 수신
				@Cleanup InputStream is = httpTokenUrlConn.getInputStream();
				@Cleanup BufferedReader br = 
							new BufferedReader(new InputStreamReader(is));
				
				// 토큰서버에서 응답으로 보내온 응답메시지를 한행씩 문자열로
				// 읽어내서, 하나의 큰 문자열로 만들어야 합니다.
				StringBuffer sb = new StringBuffer();
				
				String line;
				while((line = br.readLine()) != null) {	// null -> EOF
					sb.append(line);	// StringBuffer 에 계속 한행씩 추가
				} // while
				
				final String responseJSON = sb.toString();
				log.info("\tStep3. responseJSON: {}", responseJSON);
        	
				// ------------
				// Step4. Step3의 결과로 얻은 토큰서버에서 준 JSON문자열에서, 우리에게 필요한
				//        토큰들을 JSON 역직렬화를 통해 얻어냅니다.
				// ------------
				Objects.requireNonNull(responseJSON);
				if("".equals(responseJSON)) throw new IOException("responseJSON is a empty string");
				
				JSONObject jsonObj = new JSONObject(responseJSON);
				
				final String accessToken = jsonObj.getString("access_token");
				final String tokenType = jsonObj.getString("token_type");
				final String refreshToken = jsonObj.getString("refresh_token");
				final String idToken = jsonObj.getString("id_token");
				final Integer expiresIn = jsonObj.getInt("expires_in");
				final String scope = jsonObj.getString("scope");
				final Integer refreshTokenExpiresIn = jsonObj.getInt("refresh_token_expires_in");
				
				log.info("\tStep4. accessToken: {}", accessToken);
				log.info("\tStep4. tokenType: {}", tokenType);
				log.info("\tStep4. refreshToken: {}", refreshToken);
				log.info("\tStep4. idToken: {}", idToken);
				log.info("\tStep4. expiresIn: {}", expiresIn);
				log.info("\tStep4. scope: {}", scope);
				log.info("\tStep4. refreshTokenExpiresIn: {}", refreshTokenExpiresIn);
				
				// ---------
				// Step5. Step4 에서 획득한 Access Token 을 이용해서,
				//        카카오에 등록되어 있는 나의 정보를 획득
				// ---------
				URL profileURL = new URL(KakaoOAuth2Utils.PROFILE_URI);
				log.info("\tStep5. profileURL: {}", profileURL);
				
				Objects.requireNonNull(profileURL);
				URLConnection profileUrlConn = profileURL.openConnection();
				
				if(profileUrlConn instanceof HttpURLConnection httpProfileUrlConn) {
					httpProfileUrlConn.setRequestMethod("GET");
					httpProfileUrlConn.setRequestProperty("Authorization", tokenType + ' ' + accessToken);
					
					@Cleanup InputStream is2 = httpProfileUrlConn.getInputStream();
					@Cleanup BufferedReader br2 = 
								new BufferedReader(new InputStreamReader(is2));
					
					StringBuffer sb2 = new StringBuffer();
					
					String line2;
					while((line2 = br2.readLine()) != null) {	// null => EOF
						sb2.append(line2);
					} // while
					
					String profileJSON = sb2.toString();
					log.info("\tStep5. profileJSON: {}", profileJSON);
            	} // if
            } // if
        } catch (Exception e) {
            throw new IOException(e);
        }  // try-catch
    } // service
} // end class

