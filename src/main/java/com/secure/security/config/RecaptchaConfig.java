package com.secure.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RecaptchaConfig.class);

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${recaptcha.url}")
    private String recaptchaUrl;

    @Value("${recaptcha.enabled:true}")
    private boolean recaptchaEnabled;

    @Service
    public class RecaptchaService {
        private final RestTemplate restTemplate = new RestTemplate();

        public boolean verifyRecaptcha(String recaptchaResponse) {
            // Skip verification if reCAPTCHA is disabled
            if (!recaptchaEnabled) {
                logger.info("reCAPTCHA verification is disabled");
                return true;
            }

            if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
                logger.warn("reCAPTCHA response is null or empty");
                return false;
            }

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", recaptchaSecret);
            params.add("response", recaptchaResponse);

            try {
                logger.debug("Sending reCAPTCHA verification request to: {}", recaptchaUrl);
                logger.debug("Using secret key: {}", recaptchaSecret);
                
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    recaptchaUrl,
                    params,
                    Map.class
                );

                if (response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    logger.debug("reCAPTCHA response body: {}", responseBody);
                    
                    Object success = responseBody.get("success");
                    boolean isSuccess = success != null && Boolean.parseBoolean(success.toString());
                    
                    if (!isSuccess) {
                        Object errorCodes = responseBody.get("error-codes");
                        logger.warn("reCAPTCHA verification failed. Error codes: {}", errorCodes);
                        logger.warn("Full response: {}", responseBody);
                    } else {
                        logger.info("reCAPTCHA verification successful");
                    }
                    
                    return isSuccess;
                } else {
                    logger.warn("reCAPTCHA response body is null");
                }
            } catch (Exception e) {
                logger.error("Error during reCAPTCHA verification: {}", e.getMessage(), e);
            }
            return false;
        }
    }
} 
