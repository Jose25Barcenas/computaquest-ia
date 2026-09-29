package com.computaquest.service;

import com.computaquest.exception.ServiceUnavailableException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final ObjectProvider<JavaMailSender> javaMailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.from:noreply@computaquest.app}")
    private String from;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (sender == null) {
            throw new ServiceUnavailableException(
                    "La recuperacion de contrasena no esta configurada en el servidor (falta spring.mail.host)");
        }

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Restablecer tu contrasena - ComputaQuest");

            String resetLink = frontendUrl + "/login?resetToken=" + resetToken;
            helper.setText(
                    "Hola,\n\n"
                            + "Recibimos una solicitud para restablecer tu contrasena en ComputaQuest.\n\n"
                            + "Tu token (valido por 1 hora):\n"
                            + resetToken + "\n\n"
                            + "O abre este enlace para restablecerla directamente:\n"
                            + resetLink + "\n\n"
                            + "Si no solicitaste este cambio, ignora este mensaje.\n",
                    false);

            sender.send(message);
            log.info("Email de restablecimiento enviado a {}", toEmail);
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fallo al enviar email de restablecimiento: {}", e.getMessage());
            throw new ServiceUnavailableException("No se pudo enviar el correo de restablecimiento, intenta mas tarde");
        }
    }
}
