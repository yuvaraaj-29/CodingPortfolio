package chatbot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class RegistrationWithOTP {
    private static String generatedOTP;
    
    // Email credentials (replace with your sender email & app password)
    private static final String SENDER_EMAIL = "yuvaraj*****am@gmail.com";
    private static final String SENDER_PASSWORD = "qwertyuiop";

    public static void main(String[] args) {
        showRegistrationForm();
    }

    private static void showRegistrationForm() {
        JFrame frame = new JFrame("Registration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // First Name
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("First Name*"), c);
        c.gridx = 1;
        JTextField firstNameField = new JTextField(15);
        panel.add(firstNameField, c);

        // Last Name
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Last Name*"), c);
        c.gridx = 1;
        JTextField lastNameField = new JTextField(15);
        panel.add(lastNameField, c);

        // Email
        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Email*"), c);
        c.gridx = 1;
        JTextField emailField = new JTextField(15);
        panel.add(emailField, c);

        // Password
        c.gridx = 0; c.gridy = 3;
        panel.add(new JLabel("Password*"), c);
        c.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField, c);

        // Button to get OTP
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        JButton getOTPButton = new JButton("Get OTP");
        panel.add(getOTPButton, c);

        // OTP Field
        c.gridy = 5;
        JTextField otpField = new JTextField(15);
        otpField.setVisible(false);
        panel.add(otpField, c);

        // Submit Button
        c.gridy = 6;
        JButton submitButton = new JButton("Submit");
        submitButton.setVisible(false);
        panel.add(submitButton, c);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Action to generate OTP and send email
        getOTPButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            if(email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter email first");
                return;
            }

            generatedOTP = String.format("%04d", new Random().nextInt(10000));

            boolean sent = sendOTPEmail(email, generatedOTP);
            if(sent) {
                JOptionPane.showMessageDialog(frame, "OTP sent to email!");
                otpField.setVisible(true);
                submitButton.setVisible(true);
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to send OTP. Check your email settings.");
            }
        });

        // Submit Action
        submitButton.addActionListener(e -> {
            String otpInput = otpField.getText().trim();
            if(otpInput.equals(generatedOTP)) {
                JOptionPane.showMessageDialog(frame, "Registration Successful!");
                frame.dispose(); // close registration form
                showLoginForm(); // open login form
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid OTP!");
            }
        });
    }

    // Function to send OTP via JavaMail
    private static boolean sendOTPEmail(String recipientEmail, String otp) {
            String host = "smtp.gmail.com";  // or your SMTP host
           String port = "587"; 
    // Email credentials (replace with your sender email & app password)
        String SENDER_EMAIL = "yuvaraj******am@gmail.com";
         String SENDER_PASSWORD = "qwertyuiop";               
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );
            message.setSubject("Your OTP Code");
            message.setText("Your OTP for registration is: " + otp);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Simple Login Form GUI
    private static void showLoginForm() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 250);
        loginFrame.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Email
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Email"), c);
        c.gridx = 1;
        JTextField emailField = new JTextField(15);
        panel.add(emailField, c);

        // Password
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Password"), c);
        c.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField, c);

        // Login Button
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        panel.add(loginButton, c);

        loginFrame.add(panel);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            // Here you can verify credentials from DB
            JOptionPane.showMessageDialog(loginFrame, "Login successful for: " + email);
        });
    }
}
