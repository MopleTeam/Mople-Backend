package com.groupMeeting.auth.key;

import com.groupMeeting.core.exception.custom.JwtException;

import java.util.List;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

public record OidcPublicKeyList(
        List<OidcPublicKey> keys
) {
    public OidcPublicKey getMatchedKey(String kid, String alg){
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findAny()
                .orElseThrow(() -> new JwtException(EXTERNAL_SERVER_ERROR));
    }
}
