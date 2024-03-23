package mate.academy.service;

public interface EmailService {
    void sendEmail(String toEmail, String subject, String body);
}
