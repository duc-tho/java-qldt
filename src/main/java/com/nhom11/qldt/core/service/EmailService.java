package com.nhom11.qldt.core.service;

import java.util.Collection;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
/** Gửi email văn bản đơn giản cho một hoặc nhiều người nhận. */
public class EmailService {
  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /** Gửi nội dung đến danh sách người nhận; không làm gì khi danh sách rỗng. */
  public void send(Collection<String> recipients, String subject, String content) {
    if (recipients.isEmpty()) return;

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(recipients.toArray(String[]::new));
    message.setSubject(subject);
    message.setText(content);
    mailSender.send(message);
  }
}
