package com.groupMeeting.auth.provider;

import com.groupMeeting.auth.key.OidcPublicKey;
import com.groupMeeting.auth.key.OidcPublicKeyList;
import com.groupMeeting.core.exception.custom.JwtException;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;


@Component
public class PublicKeyProvider {

    public PublicKey generatePublicKey(final Map<String, String> tokenHeaders, final OidcPublicKeyList publicKeys) {
        final OidcPublicKey publicKey = publicKeys.getMatchedKey(tokenHeaders.get("kid"), tokenHeaders.get("alg"));

        return getPublicKey(publicKey);
    }

    private PublicKey getPublicKey(final OidcPublicKey publicKey) {
        final byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
        final byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

        final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));

        try {
            return KeyFactory.getInstance(publicKey.kty()).generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JwtException(EXTERNAL_SERVER_ERROR);
        }
    }
}