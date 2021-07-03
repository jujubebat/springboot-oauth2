package springboot.oauth2.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/login/oauth2/code")
public class MemberOauth2Controller {

    @GetMapping(value = "/kakao")
    public String kakaoOauthRedirect(@RequestParam String code) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "4ad4e652cfa8fa6db82ca693773c7d3f");
        params.add("redirect_uri", "http://localhost:8080/login/oauth2/code/kakao");
        params.add("code", code);
        params.add("client_secret", "4A32EmOsHyuAKAmijabrPBjspReisLgx");

        HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = rt.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            kakaoRequest,
            String.class
        );

        String jsonString = response.getBody();
        JSONObject jsonObject = new JSONObject(jsonString);
        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        Integer expires_in = (Integer) jsonObject.get("expires_in");
        Integer refresh_token_expires_in = (Integer) jsonObject.get("refresh_token_expires_in");
        String token_type = jsonObject.getString("token_type");

        System.out.println("[카카오 서버로부터 access_token을 받아왔습니다.]");
        System.out.println("access_token : " + access_token);
        System.out.println();

        // 토큰으로 카카오 서버에서 사용자 정보 가져오기
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add("Authorization", "Bearer " + access_token);
        headers1.add("Content-type", "application/x-www-form-urlencoded;charset=utf8");

        HttpEntity<HttpHeaders> kakaoRequest1 = new HttpEntity<>(headers1);

        HttpEntity<String> profileResponse = rt.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoRequest1,
            String.class
        );

        JSONObject jsonObject2 = new JSONObject(profileResponse.getBody());
        System.out.println("[카카오 로그인 완료.. 카카오 사용자 정보를 출력합니다...]");
        System.out.println(jsonObject2);
        System.out.println();

        // 발급 받은 토큰이 유효한지 검증하기
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer " + access_token);
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoRequest2 = new HttpEntity<>(headers2);

        RestTemplate rt2 = new RestTemplate();
        ResponseEntity<String> response2 = rt2.exchange(
            "https://kapi.kakao.com/v1/user/access_token_info",
            HttpMethod.GET,
            kakaoRequest2,
            String.class
        );

        JSONObject jsonObject3 = new JSONObject(profileResponse.getBody());
        System.out.println("[토큰 유효성 검사 완료.. 결과를 출력합니다.]");
        System.out.println(jsonObject3);

        JSONObject kakao_account = (JSONObject) jsonObject3.get("kakao_account");
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

    @GetMapping(value = "/naver")
    @ResponseBody
    public String naverOAuthRedirect(@RequestParam String code, @RequestParam String state) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders accessTokenHeaders = new HttpHeaders();
        accessTokenHeaders.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> accessTokenParams = new LinkedMultiValueMap<>();
        accessTokenParams.add("grant_type", "authorization_code");
        accessTokenParams.add("client_id", "rhOOnuIVRpuKUyYMtzTh");
        accessTokenParams.add("client_secret", "SbYD_Wy7lz");
        accessTokenParams.add("code", code);
        accessTokenParams.add("state", state);

        HttpEntity<MultiValueMap<String, String>> accessTokenRequest = new HttpEntity<>(
            accessTokenParams, accessTokenHeaders);

        ResponseEntity<String> accessTokenResponse = rt.exchange(
            "https://nid.naver.com/oauth2.0/token",
            HttpMethod.POST,
            accessTokenRequest,
            String.class
        );

        String jsonString = accessTokenResponse.getBody();
        JSONObject jsonObject = new JSONObject(jsonString);
        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        String token_type = jsonObject.getString("token_type");
        String expires_in = jsonObject.getString("expires_in");

        // profile api로 생성해둔 헤더를 담아서 요청을 보냅니다.
        HttpHeaders profileRequestHeader = new HttpHeaders();
        profileRequestHeader.add("Authorization", "Bearer " + access_token);

        HttpEntity<HttpHeaders> profileHttpEntity = new HttpEntity<>(profileRequestHeader);

        // profile api로 생성해둔 헤더를 담아서 요청을 보냅니다.
        ResponseEntity<String> profileResponse = rt.exchange(
            "https://openapi.naver.com/v1/nid/me",
            HttpMethod.POST,
            profileHttpEntity,
            String.class
        );

        String jsonString2 = profileResponse.getBody();
        JSONObject jsonObject2 = new JSONObject(jsonString2);
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
}
