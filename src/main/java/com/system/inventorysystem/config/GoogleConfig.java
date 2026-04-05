package com.system.inventorysystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {


    public static String googleClientId;

    @Value("${google.client.id}")
    public void setGoogleClientId(String id) {
        GoogleConfig.googleClientId = id;
    }

}