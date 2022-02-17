package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
//@EnableAsync
@Transactional
public class MailService {

    @Value("${spring.mail.username}")
    private String EMAIL_FROM;
    private final JavaMailSender javaMailSender;

//    @Async
    public void sendEmail(String emailTo, String subject, String template) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(template, true);
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setFrom(EMAIL_FROM);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new ApiBadRequestException("Error while sending email : " + e.getMessage());
        }
    }

}
