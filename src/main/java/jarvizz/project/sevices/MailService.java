package jarvizz.project.sevices;


import jarvizz.project.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@PropertySource("classpath:application.properties")
@Service
public class MailService {
      @Autowired
      JavaMailSender javaMailSender;
         @Autowired
         Environment env;

         public void send(String email,User user) throws MessagingException, IOException {
             MimeMessage mimeMessage = javaMailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
             Path path = Paths.get(System.getProperty("user.dir") + File.separator + "icons" +  File.separator + "logo.png");
             MultipartFile file = new MockMultipartFile("logo.png",
                     "logo.png",  "text/plain", Files.readAllBytes(path));
             try {
                 mimeMessage.setFrom(new InternetAddress(env.getProperty("spring.mail.username")));
                 helper.setTo(email);
                 helper.setSubject("Підтвердження регістраціїї на GAME PLAY");
                 helper.addAttachment(file.getOriginalFilename(),file);
                 helper.setText("<h2>Привіт " + user.getUsername() + "</h2>" +
                         "<h3> Будь-ласка натисніть кнопку нижче для завершення регістрації \n </h3> "+
                         " <a target='_blank' href= http://localhost:8080/register/confirm/"+user.getUsername()+"><button>Підтвердити регістрацію</button></a>",true);
             } catch (MessagingException e) {
                 e.printStackTrace();
             }
                 javaMailSender.send(mimeMessage);

         }
}
