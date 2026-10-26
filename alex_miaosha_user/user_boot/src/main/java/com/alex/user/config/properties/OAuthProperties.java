package com.alex.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description: 第三方 OAuth 授权配置属性
 * author: alex
 * version: 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private AppConfig wechat = new AppConfig(
            "wxec93a0ddb72c8cff",
            "1240434ae0be6dc4b0ba979d7c1f9b7a",
            "https://mjzp.xyz/login"
    );

    private AppConfig baidu = new AppConfig(
            "w7kcpHna8w8irDiMA4tdnnnQ",
            "8LTOzDkpVv5LkzPR9yyptsq7MMENyCVS",
            "https://mjzp.xyz/login"
    );

    @Data
    public static class AppConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;

        public AppConfig() {
        }

        public AppConfig(String clientId, String clientSecret, String redirectUri) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
        }
    }
}
