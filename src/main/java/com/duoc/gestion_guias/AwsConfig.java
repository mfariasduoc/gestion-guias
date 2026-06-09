package com.duoc.gestion_guias;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class AwsConfig {

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.session.token}")
    private String sessionToken;

    @Bean
    public AmazonS3 amazonS3() {
        // Usamos BasicSessionCredentials porque es una cuenta de AWS Academy con Token
        BasicSessionCredentials credentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);
        
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion("us-east-1") // Asegúrate de usar la región que te da tu Lab, us-east-1 es la típica
                .build();
    }
}