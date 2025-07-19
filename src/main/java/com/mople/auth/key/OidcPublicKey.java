package com.mople.auth.key;

public record OidcPublicKey(
        String kid,
        String kty,
        String alg,
        String use,
        String n,
        String e
) {
}
