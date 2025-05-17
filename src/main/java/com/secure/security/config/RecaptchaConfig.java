package com.secure.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Configuration
public class RecaptchaConfig {
    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${recaptcha.url}")
    private String recaptchaUrl;

    @Service
    public class RecaptchaService {
        private final RestTemplate restTemplate = new RestTemplate();

        public boolean verifyRecaptcha(String recaptchaResponse) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", recaptchaSecret);
            params.add("response", recaptchaResponse);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    recaptchaUrl,
                    params,
                    Map.class
                );
                if (response.getBody() != null) {
                    Object success = response.getBody().get("success");
                    return success != null && Boolean.parseBoolean(success.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
} 