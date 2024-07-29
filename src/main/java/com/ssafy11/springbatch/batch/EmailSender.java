package com.ssafy11.springbatch.batch;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.ssafy11.springbatch.domain.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EmailSender {

	private final JavaMailSender javaMailSender;

	public void sendEmails(List<User> users, String subject, String text) {
		for (User user : users) {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(user.getEmail());
			message.setSubject(subject);
			message.setText(text);
			this.javaMailSender.send(message);
		}
	}
}
