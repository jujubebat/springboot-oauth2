package springboot.oauth2.controller;

import com.google.common.base.Splitter;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/login/oauth2/code")
public class Oauth2Controller {

    @GetMapping(value = "/kakao") // 2. 인증 코드 전달
    public String kakaoOauthRedirect(@RequestParam String code) {
        // 3. 인증 코드로 토큰 요청
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenRequestHeader = new HttpHeaders(); // http 요청 헤더 만들기
        tokenRequestHeader.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>(); // http 요청 바디 만들기
        tokenRequestBody.add("grant_type", "authorization_code");
        tokenRequestBody.add("code", code);
        tokenRequestBody.add("client_id", "4ad4e652cfa8fa6db82ca693773c7d3f");
        tokenRequestBody.add("client_secret", "4A32EmOsHyuAKAmijabrPBjspReisLgx");
        tokenRequestBody.add("redirect_uri", "http://localhost:8080/login/oauth2/code/kakao");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenRequestBody,
            tokenRequestHeader);

        ResponseEntity<String> tokenResponse = restTemplate.exchange( // 인증 코드로 토큰을 요청한다.
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            tokenRequest,
            String.class
        );

        // 4. 토큰 전달 받음
        JSONObject jsonObject = new JSONObject(tokenResponse.getBody());

        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        Integer expires_in = (Integer) jsonObject.get("expires_in");
        Integer refresh_token_expires_in = (Integer) jsonObject.get("refresh_token_expires_in");
        String token_type = jsonObject.getString("token_type");

        // 5. 토큰으로 카카오 API 호출 (카카오 서버에서 토큰 유효성 확인후 사용자 데이터 받아옴)
        HttpHeaders apiRequestHeader = new HttpHeaders();
        apiRequestHeader.add("Authorization", "Bearer " + access_token);
        apiRequestHeader.add("Content-type", "application/x-www-form-urlencoded;charset=utf8");
        HttpEntity<HttpHeaders> apiRequest = new HttpEntity<>(apiRequestHeader);

        HttpEntity<String> apiResponse = restTemplate.exchange( // 토큰과 함께 api를 호출한다.
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            apiRequest,
            String.class
        );

        // 응답 결과 파싱
        JSONObject jsonObject2 = new JSONObject(apiResponse.getBody());
        JSONObject kakao_account = (JSONObject) jsonObject2.get("kakao_account");
        String birthday = kakao_account.getString("birthday");
        String gender = kakao_account.getString("gender");
        String email = kakao_account.getString("email");

        JSONObject profile = (JSONObject) kakao_account.get("profile");
        String nickname = profile.getString("nickname");

        StringBuilder result = new StringBuilder();
        result.append("<h1>카카오 로그인 성공</h1>");
        result.append("nickname : " + nickname + "<br><br>");
        result.append("gender : " + gender + "<br><br>");
        result.append("birthday : " + birthday + "<br><br>");
        result.append("email : " + email + "<br><br>");
        result.append("token_type : " + token_type + "<br><br>");
        result.append("access_token : " + access_token + "<br><br>");
        result.append("access_token_expires_in : " + expires_in + "<br><br>");
        result.append("refresh_token : " + refresh_token + "<br><br>");
        result.append("refresh_token_expires_in : " + refresh_token_expires_in + "<br><br>");

        return result.toString();
    }

    @GetMapping(value = "/naver") // 2. 인증 코드 전달
    public String naverOauthRedirect(@RequestParam String code, @RequestParam String state) {
        // 3. 인증 코드로 토큰 요청
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenRequestHeader = new HttpHeaders();
        tokenRequestHeader.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> tokenReqestBody = new LinkedMultiValueMap<>();
        tokenReqestBody.add("grant_type", "authorization_code");
        tokenReqestBody.add("client_id", "rhOOnuIVRpuKUyYMtzTh");
        tokenReqestBody.add("client_secret", "SbYD_Wy7lz");
        tokenReqestBody.add("code", code);
        tokenReqestBody.add("state", state);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenReqestBody,
            tokenRequestHeader);

        ResponseEntity<String> tokenResponse = restTemplate.exchange(
            "https://nid.naver.com/oauth2.0/token",
            HttpMethod.POST,
            tokenRequest,
            String.class
        );

        // 4. 토큰 전달 받음
        JSONObject jsonObject = new JSONObject(tokenResponse.getBody());
        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        String token_type = jsonObject.getString("token_type");
        String expires_in = jsonObject.getString("expires_in");

        // 5. 토큰으로 카카오 API 호출 (카카오 서버에서 토큰 유효성 확인후 사용자 데이터 받아옴)
        HttpHeaders apiRequestHeader = new HttpHeaders();
        apiRequestHeader.add("Authorization", "Bearer " + access_token);
        HttpEntity<HttpHeaders> profileHttpEntity = new HttpEntity<>(apiRequestHeader);

        ResponseEntity<String> apiResponse = restTemplate.exchange(
            "https://openapi.naver.com/v1/nid/me",
            HttpMethod.POST,
            profileHttpEntity,
            String.class
        );

        // 응답 결과 파싱
        JSONObject jsonObject2 = new JSONObject(apiResponse.getBody());
        JSONObject response = (JSONObject) jsonObject2.get("response");

        String id = response.getString("id");
        String nickname = response.getString("nickname");
        String profile_image = response.getString("profile_image");
        String age = response.getString("age");
        String gender = response.getString("gender");
        String email = response.getString("email");
        String mobile = response.getString("mobile");
        String mobile_e164 = response.getString("mobile_e164");
        String name = response.getString("name");
        String birthday = response.getString("birthday");
        String birthyear = response.getString("birthyear");

        StringBuilder result = new StringBuilder();
        result.append("<h1>네이버 로그인 성공</h1>");
        result.append("id : " + id + "<br><br>");
        result.append("nickname : " + nickname + "<br><br>");
        result.append("profile_image : " + profile_image + "<br><br>");
        result.append("age : " + age + "<br><br>");
        result.append("gender : " + gender + "<br><br>");
        result.append("email : " + email + "<br><br>");
        result.append("mobile : " + mobile + "<br><br>");
        result.append("mobile_e164 : " + mobile_e164 + "<br><br>");
        result.append("name : " + name + "<br><br>");
        result.append("birthday : " + birthday + "<br><br>");
        result.append("birthyear : " + birthyear + "<br><br>");
        result.append("token_type : " + token_type + "<br><br>");
        result.append("access_token : " + access_token + "<br><br>");
        result.append("access_token_expires_in : " + expires_in + "<br><br>");
        result.append("refresh_token : " + refresh_token + "<br><br>");

        return result.toString();
    }

    @GetMapping(value = "/github")
    public String githubOauthRedirect(@RequestParam String code) { // 2. 인증 코드 전달
        // 3. 인증 코드로 토큰 요청
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders tokenRequestHeader = new HttpHeaders();
        tokenRequestHeader.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> tokenResponseBody = new LinkedMultiValueMap<>();
        tokenResponseBody.add("client_id", "96f270ce3e0af33e8075");
        tokenResponseBody.add("client_secret", "470fd0775463bf507847f704c63d8d57a67aaf88");
        tokenResponseBody.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenResponseBody,
            tokenRequestHeader);

        ResponseEntity<String> tokenResponse = restTemplate.exchange(
            "https://github.com/login/oauth/access_token",
            HttpMethod.POST,
            tokenRequest,
            String.class
        );

        // 4. 토큰 전달 받음
        String queryString = tokenResponse.getBody();
        Map<String, String> queryParameters = Splitter
            .on("&")
            .withKeyValueSeparator("=")
            .split(queryString);

        String access_token = queryParameters.get("access_token");

        // 5. 토큰으로 카카오 API 호출 (카카오 서버에서 토큰 유효성 확인후 사용자 데이터 받아옴)
        HttpHeaders apiRequestHeader = new HttpHeaders();
        apiRequestHeader.add("Authorization", "token " + access_token);
        HttpEntity<HttpHeaders> apiRequest = new HttpEntity<>(apiRequestHeader);

        ResponseEntity<String> apiResponse = restTemplate.exchange(
            "https://api.github.com/user",
            HttpMethod.GET,
            apiRequest,
            String.class
        );

        // 응답 결과 파싱
        JSONObject jsonObject = new JSONObject(apiResponse.getBody());
        String login = jsonObject.getString("login");
        String avatar_url = jsonObject.getString("avatar_url");
        String name = jsonObject.getString("name");
        String company = jsonObject.getString("company");
        String blog = jsonObject.getString("blog");
        String email = jsonObject.getString("email");
        String location = jsonObject.getString("location");

        StringBuilder result = new StringBuilder();
        result.append("<h1>깃헙 로그인 성공</h1>");
        result.append("login : " + login + "<br><br>");
        result.append("avatar_url : " + avatar_url + "<br><br>");
        result.append("name : " + name + "<br><br>");
        result.append("company : " + company + "<br><br>");
        result.append("blog : " + blog + "<br><br>");
        result.append("email : " + email + "<br><br>");
        result.append("location : " + location + "<br><br>");
        result.append("access_token : " + access_token + "<br><br>");

        return result.toString();
    }

}
