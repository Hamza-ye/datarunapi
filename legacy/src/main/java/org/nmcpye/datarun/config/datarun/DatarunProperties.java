package org.nmcpye.datarun.config.datarun;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * @author Hamza Assada 16/04/2025 (7amza.it@gmail.com)
 */
@Getter
@ConfigurationProperties(
    prefix = "datarun",
    ignoreInvalidFields = true
)
public class DatarunProperties {
    private final Security security = new Security();

    public DatarunProperties() {
    }

    @Getter
    public static class Security {
        private final Security.ClientAuthorization clientAuthorization = new Security.ClientAuthorization();
        private final Security.Authentication authentication = new Security.Authentication();

        public Security() {
        }

        @Setter
        @Getter
        public static class ClientAuthorization {
            private String accessTokenUri;
            private String tokenServiceId;
            private String clientId;
            private String clientSecret;

            public ClientAuthorization() {
                this.accessTokenUri = DatarunDefaults.Security.ClientAuthorization.accessTokenUri;
                this.tokenServiceId = DatarunDefaults.Security.ClientAuthorization.tokenServiceId;
                this.clientId = DatarunDefaults.Security.ClientAuthorization.clientId;
                this.clientSecret = DatarunDefaults.Security.ClientAuthorization.clientSecret;
            }

        }

        @Getter
        public static class Authentication {
            private final Security.Authentication.Jwt jwt = new Security.Authentication.Jwt();

            public Authentication() {
            }

            @Setter
            @Getter
            public static class Jwt {
                private String secret;
                private String base64Secret;
                private Resource rsaPrivateFile;
                private Resource rsaPublicFile;
                private String[] acceptedAlgorithms;
                private String rsaKeyId;
                private long tokenValidityInSeconds;
                private long tokenValidityInSecondsForRememberMe;
                private long refreshTokenValidityInSeconds;

                public Jwt() {
                    this.secret = DatarunDefaults.Security.Authentication.Jwt.secret;
                    this.base64Secret = DatarunDefaults.Security.Authentication.Jwt.base64Secret;
                    this.rsaPrivateFile = DatarunDefaults.Security.Authentication.Jwt.rsaPrivateFile;
                    this.rsaPublicFile = DatarunDefaults.Security.Authentication.Jwt.rsaPublicFile;
                    this.acceptedAlgorithms = DatarunDefaults.Security.Authentication.Jwt.acceptedAlgorithms;
                    this.rsaKeyId = DatarunDefaults.Security.Authentication.Jwt.rsaKeyId;
                    this.tokenValidityInSeconds = 1800L;
                    this.tokenValidityInSecondsForRememberMe = 2592000L;
                    this.refreshTokenValidityInSeconds = 2592000L;
                }

            }
        }
    }
}
