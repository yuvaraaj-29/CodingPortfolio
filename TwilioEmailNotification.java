package velmurugan;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;

public class TwilioEmailNotification {

    // Fetch SendGrid API key from environment variable
    public static final String SENDGRID_API_KEY = System.getenv("SG.fzYs3cnbSLaiaj9ZFkViPw---rest of my api key");

    public static void sendEmail(String toEmail, String subject, String body) {
        
        if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isEmpty()) {
            System.err.println("Error: SendGrid API key not found. Set the SENDGRID_API_KEY environment variable.");
            return;
        }

        // Sender and Receiver Email
        Email from = new Email("yuvarajamanikkam_bit27@mepcoeng.ac.in"); // Sender email (must be verified)
        Email to = new Email(toEmail); // Receiver email
        Content content = new Content("text/plain", body); // Email body
        Mail mail = new Mail(from, subject, to, content); // Constructing Mail object

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        

        Request request = new Request(); // Create a SendGrid request object

        try {
            // Set the method to POST and the endpoint for sending email
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Send the request using the SendGrid API
            Response response = sg.api(request);

            // Output response details
            System.out.println("Email sent successfully!");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
        } catch (IOException ex) {
           
            System.err.println("Error sending email: " + ex.getMessage());
        }
        finally{
             System.out.println("Classpath: " + System.getProperty("java.class.path"));
        }
    }

    public static void main(String[] args) {
        // Test the sendEmail method with a sample recipient
        sendEmail("yuvaselva105@gmail.com", "Test Subject", "This is a test email.");
    }
}
