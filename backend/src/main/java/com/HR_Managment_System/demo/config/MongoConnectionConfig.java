package com.HR_Managment_System.demo.config;

import com.mongodb.ConnectionString;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConnectionConfig {

    @Bean
    public MongoConnectionDetails mongoConnectionDetails() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            uri = "mongodb://localhost:27017/hr_management_db";
        }
        final String resolvedUri = uri;
        return () -> new ConnectionString(resolvedUri);
    }
}
