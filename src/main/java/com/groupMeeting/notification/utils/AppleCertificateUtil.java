package com.groupMeeting.notification.utils;

import io.jsonwebtoken.Jwts;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Objects;

@Deprecated
@Component
public class AppleCertificateUtil {
    private final String TEAM_ID;  // Your Apple Developer Team ID
    private final String KEY_ID;    // The Key ID from the .p8 file
    private final String KEY_PATH;

    public AppleCertificateUtil(
            @Value("${apple.team-id}") String teamId,
            @Value("${apple.key-id}") String keyId,
            @Value("${apple.key-name}") String keyPath
    ) {
        this.TEAM_ID = teamId;
        this.KEY_ID = keyId;
        this.KEY_PATH = keyPath;
    }

    public String generateToken() throws Exception {
        PrivateKey privateKey = getPrivateKey();
        Date now = new Date(System.currentTimeMillis());

        return Jwts.builder()
                .header()
                    .add("kid", KEY_ID)
                    .and()
                .issuer(TEAM_ID)
                .issuedAt(now)
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))  // Valid for one hour
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
    }

    public PrivateKey getPrivateKey() throws IOException, URISyntaxException {
        Reader pemReader =
                new StringReader(
                        new String(
                                Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(KEY_PATH)).toURI())))
                );

        PEMParser pemParser = new PEMParser(pemReader);
        return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) pemParser.readObject());
    }
}
