package org.nmcpye.datarun.web.rest.v1.authenticate;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
public class JwksController {

    @Value("${jhipster.security.authentication.jwt.rsa-public-file:classpath:jwt-public.pem}")
    private Resource rsaPublicResource;

    // Initially Use a fixed kid so downstream JWKS cache is stable across restarts (you may change)
    @Value("${datarun.security.authentication.jwt.rsa-key-id:datarun-rs256-1}")
    private String keyId;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() throws Exception {
        String pem = new String(rsaPublicResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String normalized = pem
            .replaceAll("-----BEGIN PUBLIC KEY-----", "")
            .replaceAll("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(keySpec);
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) pub)
            .keyID(keyId)
            .build();
        return new JWKSet(jwk).toJSONObject().toString();
    }
}
