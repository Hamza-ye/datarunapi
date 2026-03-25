package org.nmcpye.datarun.config.datarun;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.nmcpye.datarun.iam.security.SecurityUtils.JWT_ALGORITHM;

/**
 * @author Hamza Assada 03/03/2026 (7amza.it@gmail.com)
 */
@Configuration
public class JwtDualConfig {

    @Value("${datarun.security.authentication.jwt.rsa-public-file:classpath:jwt-public.pem}")
    private Resource rsaPublicResource;

    @Value("${datarun.security.authentication.jwt.rsa-private-file:classpath:jwt-private-pkcs8.pem}")
    private Resource rsaPrivateResource;

    @Value("${datarun.security.authentication.jwt.base64-secret}")
    private String hs256SecretOrBase64;

    @Value("${datarun.security.authentication.jwt.rsa-key-id:datarun-rs256-1}")
    private String rsaKeyId;
    // ---------------------------
    // JwtDecoder : dual validation
    // ---------------------------
    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey rsaPublic = loadPublicKey();
        NimbusJwtDecoder rsDecoder = NimbusJwtDecoder.withPublicKey(rsaPublic).build();

        NimbusJwtDecoder hsDecoder = null;
        if (hs256SecretOrBase64 != null && !hs256SecretOrBase64.isBlank()) {
            hsDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtils.JWT_ALGORITHM).build();
        }

        final NimbusJwtDecoder finalRs = rsDecoder;
        final NimbusJwtDecoder finalHs = hsDecoder;

        // implements JwtDecoder.decode(String token)
        return token -> {
            try {
                String alg = SignedJWT.parse(token).getHeader().getAlgorithm().getName();
                if ("RS256".equalsIgnoreCase(alg)) {
                    return finalRs.decode(token);
                } else if ("HS256".equalsIgnoreCase(alg)) {
                    if (finalHs == null) {
                        throw new JwtException("HS256 token received but HS256 secret is not configured");
                    }
                    return finalHs.decode(token);
                } else {
                    throw new JwtException("Unsupported JWT alg: " + alg);
                }
            } catch (java.text.ParseException e) {
                throw new JwtException("Malformed JWT", e);
            }
        };
    }

    // ---------------------------
    // JwtEncoder : sign new tokens with RS256
    // ---------------------------
    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey();
        RSAPublicKey publicKey = loadPublicKey();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID(rsaKeyId)
            .build();

        JWKSource<SecurityContext> jwkSource =
            new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    // ---------------------------
    // Helpers to load PEM keys
    // ---------------------------
    private RSAPublicKey loadPublicKey() throws Exception {
        String pem = new String(rsaPublicResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        PublicKey pk = getPublicKeyFromPem(pem);
        return (RSAPublicKey) pk;
    }

    private RSAPrivateKey loadPrivateKey() throws Exception {
        String pem = new String(rsaPrivateResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        PrivateKey pk = getPrivateKeyFromPemPkcs8Only(pem);
        return (RSAPrivateKey) pk;
    }

    private static PublicKey getPublicKeyFromPem(String pem) throws Exception {
        String normalized = pem
            .replaceAll("-----BEGIN PUBLIC KEY-----", "")
            .replaceAll("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    /// Expects PKCS#8 PEM (-----BEGIN PRIVATE KEY-----). If PKCS#1 key,
    /// convert it first with:
    /// ```bash
    /// openssl pkcs8 -topk8 -nocrypt -in jwt-private.pem -out jwt-private-pkcs8.pem
    /// ```
    /// This method intentionally does not try to parse PKCS#1 bytes to keep the code small.
    private static PrivateKey getPrivateKeyFromPemPkcs8Only(String pem) throws Exception {
        String normalized = pem
            .replaceAll("-----BEGIN PRIVATE KEY-----", "")
            .replaceAll("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    // ---------------------------
    // Legacy Secret Helpers
    // ---------------------------
    private SecretKey getSecretKey() {
        byte[] keyBytes = com.nimbusds.jose.util.Base64.from(hs256SecretOrBase64).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
