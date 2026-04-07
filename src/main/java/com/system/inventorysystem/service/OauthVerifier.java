package com.system.inventorysystem.service;

import com.system.inventorysystem.dto.UserInforFromProvider;

public interface OauthVerifier {
    UserInforFromProvider verify(String idToken);
}
