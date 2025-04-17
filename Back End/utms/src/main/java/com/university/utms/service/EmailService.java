package com.university.utms.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String verificationCode);
    void sendResetCode(String toEmail, String resetCode);
    public void sendConfirmationEmail(String toEmail);


}
