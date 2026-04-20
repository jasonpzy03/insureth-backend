package com.insureth.notification.service.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender emailSender;
    private final Configuration freemarkerConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // This method is constantly listening to "email-queue"
    @JmsListener(destination = "email-queue")
    public void receiveMessage(Message jmsMessage) {
        Map<String, String> emailRequest = extractEmailRequest(jmsMessage);
        System.out.println("Received email request from queue for: " + emailRequest.get("to"));
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_RELATED, "UTF-8");
            
            helper.setFrom("insureth.insurance@gmail.com", "Insureth");
            helper.setTo(emailRequest.get("to"));
            helper.setSubject(emailRequest.get("subject"));

            String content;
            if (emailRequest.containsKey("template")) {
                Template t = freemarkerConfig.getTemplate(emailRequest.get("template"));
                content = FreeMarkerTemplateUtils.processTemplateIntoString(t, emailRequest);
                helper.setText(content, true); // true indicates HTML
            } else {
                content = emailRequest.get("body");
                helper.setText(content, false);
            }
            
            emailSender.send(message);
            System.out.println("Email successfully dispatched via SMTP!");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, String> extractEmailRequest(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String payload = textMessage.getText();
                return objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});
            }

            if (message instanceof MapMessage mapMessage) {
                Map<String, String> payload = new HashMap<>();
                Enumeration<?> mapNames = mapMessage.getMapNames();
                while (mapNames.hasMoreElements()) {
                    String key = String.valueOf(mapNames.nextElement());
                    Object value = mapMessage.getObject(key);
                    payload.put(key, value == null ? null : String.valueOf(value));
                }
                return payload;
            }

            throw new IllegalArgumentException("Unsupported JMS message type: " + message.getClass().getName());
        } catch (JMSException e) {
            throw new IllegalStateException("Failed to read JMS email message", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize email request payload", e);
        }
    }
}
