package com.togetherly.demo.service.email;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
}
