package com.example.ecomerseapplication.Services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final String senderEmail = "noreply@agromag.local";

    public void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom(senderEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendHTMLEmailWithPDFAttachment(
            String to,
            String subject,
            String html,
            byte[] pdfBytes,
            String attachmentName
    ) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(senderEmail);
            helper.setSubject(subject);

            helper.setText(html, true);

            helper.addAttachment(
                    attachmentName + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}