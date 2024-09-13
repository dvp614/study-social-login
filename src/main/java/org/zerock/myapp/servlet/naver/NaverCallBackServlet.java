package org.zerock.myapp.servlet.naver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serial;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

@Log4j2
@WebServlet("/login/oauth2/code/naver")
public final class NaverCallBackServlet extends HttpServlet {
	@Serial private static final long serialVersionUID = 1L;

	@Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        log.trace("service(req,res) invoked.");
        	
        try {
        	// 수신받은 모든 전송파라미터는 콘솔에 차례대로 다 출력해 놓자!!
			Map<String, String[]> paramMap = req.getParameterMap();
			paramMap.forEach((k, v) -> log.info("\t+ param({}, {})", k, v));
			
			String code = req.getParameter(NaverOAuth2Utils.RESPONSE_TYPE);
			String state = req.getParameter("state");
			
			log.info("\tStep1. code : {}, state : {}", code, state);
			
			if(!NaverOAuth2Utils.STATE.equals(state)) {
				log.info("\tStep1. - CSRF attack occured : original({}), state({})", 
						NaverOAuth2Utils.STATE, state);
				return;
			} //if
			
			// Step2. 
			final String tokenURL = new StringBuffer()
					.append(NaverOAuth2Utils.TOKEN_REQUEST_URI)
					.append('?')
					.append("grant_type=")
					.append(NaverOAuth2Utils.GRANT_TYPE).append('&')
					.append("client_id=")
					.append(NaverOAuth2Utils.CLIENT_ID).append('&')
					.append("client_secret=")
					.append(NaverOAuth2Utils.CLIENT_SECRET).append('&')
					.append("redirect_uri=")
					.append(NaverOAuth2Utils.REDIRECT_URL).append('&')
					.append("code=")
					.append(code).append('&')
					.append("state=")
					.append(state).append('&')
					.toString();
			
			log.info("\tStep2. tokenURL : {}", tokenURL);
			
			// Step3. Step2에서 완전하게 만든 URL을 코큰 서버에 전송해서
			//		  Access/Refresh Tokens 등의 정보를 획득
			// 단, 이 단계에서 우리가 직접 요청을 보내고, 또 응답메시지가 수신처리를 웹브라우저 처럼 해야
			// 하기 때문에, 이건 kakao와 또 다른 방식으로 한다
			// 이를 통해서, 웹브라우저의 가장 중요한 기능인 HTTO protocol 송신/수신 기능
			// 구현 방법을 잘 연습해야 한다.
			
			HttpClient client = HttpClientBuilder.create().build();
			
			Objects.requireNonNull(client);
			// HTTP request 생성
			HttpGet get = new HttpGet(tokenURL);
			HttpResponse response = client.execute(get);
			
			Objects.requireNonNull(response);
			
			log.info("\tStep3. client : {}", client);
			log.info("\tStep3. get : {}", get);
			log.info("\tStep3. response : {}", response);
			
			// Step4. Step3에서 수신한 응답 메시지의 Body에 들어있는 JSON 추출
			//		  이 추출된 JSON에 우리가 요청한 각종 Token 문자열이 들어 있습니다.
			
			int httpStatusCode = response.getStatusLine().getStatusCode();
			
			if(httpStatusCode == 200) {
				// 응답메시지의 Body안에 있는 JSON 추출
				@Cleanup BufferedReader br = 
						// response.getEterntiy() -> 응답메시지를 객체로 획득
						// 획득한 객체의 메소드인,  getContent() 메소드를 호출 -> InputStream 획득
						new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				StringBuffer sb = new StringBuffer();
				
				String line;
				while((line = br.readLine()) != null) {
					sb.append(line);
				} // while
				
				String tokenJSON = sb.toString();
				
				log.info("\tStep4. tokenJSON : {}", tokenJSON);
				
				Objects.requireNonNull(tokenJSON);
				if("".equals(tokenJSON)) throw new IOException("responseJSON is a empty string");
				
				JSONObject jsonObj = new JSONObject(tokenJSON);
				
				final String accessToken = jsonObj.getString("access_token");
				final String refreshToken = jsonObj.getString("refresh_token");
				final String tokenType = jsonObj.getString("token_type");
				final Integer expiresIn = jsonObj.getInt("expires_in");
				
				log.info("\tStep4. accessToken: {}", accessToken);
				log.info("\tStep4. refreshToken: {}", refreshToken);
				log.info("\tStep4. tokenType: {}", tokenType);
				log.info("\tStep4. expiresIn: {}", expiresIn);
				
				// Step5.
				HttpGet profileGet = new HttpGet(NaverOAuth2Utils.PROFILE_REQUEST_URI);
				profileGet.addHeader("Authorization", tokenType + ' ' + accessToken);
				
				HttpResponse profileResponse = client.execute(profileGet);
				log.info("\tStep5. profileResponse : {}", profileResponse);
				
				if(profileResponse.getStatusLine().getStatusCode() == 200) {
					InputStream is = profileResponse.getEntity().getContent();
					
					@Cleanup BufferedReader br2 = 
							new BufferedReader(new InputStreamReader(is));
					
					StringBuffer sb2 = new StringBuffer();
					
					String line2;
					while((line2 = br2.readLine()) != null){
						sb2.append(line2);
					} // while
					
					String profileJSON = sb2.toString();
					log.info("\tStep5. profileJSON : {}", profileJSON);
					
					
					//Step6
					JSONObject jsonObj2 = new JSONObject(profileJSON);
					JSONObject responseJsonObj = jsonObj2.getJSONObject("response");
					
					String id = responseJsonObj.getString("id");
					String gender = responseJsonObj.getString("gender");
					String email = responseJsonObj.getString("email");
					String name = responseJsonObj.getString("name");
					
					log.info("\tStep6. id : {}", id);
					log.info("\tStep6. gender : {}", gender);
					log.info("\tStep6. email : {}", email);
					log.info("\tStep6. name : {}", name);
					
				} // if
				
//				URL profileURL = new URL(NaverOAuth2Utils.PROFILE_REQUEST_URI);
//				log.info("\tStep5. profileURL: {}", profileURL);
//				
//				Objects.requireNonNull(profileURL);
//				URLConnection profileUrlConn = profileURL.openConnection();
//				
//				if(profileUrlConn instanceof HttpURLConnection httpProfileUrlConn) {
//					httpProfileUrlConn.setRequestMethod("GET");
//					httpProfileUrlConn.setRequestProperty("Authorization", tokenType + ' ' + accessToken);
//					
//					@Cleanup InputStream is2 = httpProfileUrlConn.getInputStream();
//					@Cleanup BufferedReader br2 = 
//								new BufferedReader(new InputStreamReader(is2));
//					
//					StringBuffer sb2 = new StringBuffer();
//					
//					String line2;
//					while((line2 = br2.readLine()) != null) {	// null => EOF
//						sb2.append(line2);
//					} // while
//					
//					String profileJSON = sb2.toString();
//					log.info("\tStep5. profileJSON: {}", profileJSON);
//				} // if
				
			} // if
			log.info("\tStpe4. httpStatusCode : {}", httpStatusCode);
			
        } catch (Exception e) {
            throw new IOException(e);
        }  // try-catch
    } // service
} // end class

