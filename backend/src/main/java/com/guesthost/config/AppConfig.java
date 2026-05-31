package com.guesthost.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(10_000);
        return new RestTemplate(factory);
    }

    @Bean
    public JavaMailSender javaMailSender(@Value("${spring.mail.host}") String host,
                                         @Value("${spring.mail.port}") int port,
                                         @Value("${spring.mail.protocol:smtp}") String protocol,
                                         @Value("${spring.mail.username:}") String username,
                                         @Value("${spring.mail.password:}") String password,
                                         @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean smtpAuth,
                                         @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean startTls,
                                         @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}") boolean sslEnabled,
                                         @Value("${spring.mail.properties.mail.smtp.auth.mechanisms:LOGIN PLAIN}") String authMechanisms) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setProtocol(protocol);
        sender.setUsername(username);
        sender.setPassword(password);
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", protocol);
        properties.put("mail.smtp.auth", Boolean.toString(smtpAuth));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        properties.put("mail.smtp.ssl.enable", Boolean.toString(sslEnabled));
        properties.put("mail.smtp.auth.mechanisms", authMechanisms);
        return sender;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
