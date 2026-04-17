package com.system.inventorysystem.service.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.system.inventorysystem.config.GoogleConfig;
import com.system.inventorysystem.dto.UserInforFromProvider;
import com.system.inventorysystem.exception.VerificationException;
import com.system.inventorysystem.service.OauthVerifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Service("GOOGLE")
public class GoogleOAuthVerifier implements OauthVerifier {
    @Override
    public UserInforFromProvider verify(String googleTokenId) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new JacksonFactory()
            ).setAudience(Collections.singletonList(GoogleConfig.googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(googleTokenId);
            if (idToken == null) {
                throw new VerificationException("Invalid Google ID token");
            }


            GoogleIdToken.Payload payload = idToken.getPayload();
            return UserInforFromProvider.builder()
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .build();
        } catch (Exception e) {
            throw new VerificationException("Google verification failed");
        }
    }
}
