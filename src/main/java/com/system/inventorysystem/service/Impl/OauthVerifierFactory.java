package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.enums.AuthType;
import org.springframework.stereotype.Component;
import com.system.inventorysystem.service.OauthVerifier;
import java.util.Map;
import com.system.inventorysystem.exception.AuthException;
@Component
public class OauthVerifierFactory {
     private final Map<String, OauthVerifier> verifierMap;

    public OauthVerifierFactory(Map<String, OauthVerifier> verifierMap) {
        this.verifierMap = verifierMap;
    }

    public OauthVerifier getVerifier(AuthType authType) {
        if (authType == null) {
            throw new AuthException("AUTH_00","AuthType cannot be null");
        }

        OauthVerifier verifier = verifierMap.get(authType.name());

        if (verifier == null) {
            throw new AuthException("AUTH_001","OAuth verifier not supported yet for: " + authType);
        }

        return verifier;
    }
}