
package com.tibco.support.mail;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags;
import javax.mail.Multipart;
import javax.mail.search.FlagTerm;
import javax.mail.internet.InternetAddress;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import org.apache.commons.io.IOUtils;


public class ImapGetter {
    
public static String getXML(String host, int port, Boolean tlsEnable, String user, String password, Boolean getSeenMsg, Boolean getTextHtmlMsg) {
    String out = "";
    try {
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.starttls.enable", String.valueOf(tlsEnable));
        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore("imaps");
        store.connect(host, user, password);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message messages[] = inbox.search(new FlagTerm(new Flags(Flag.SEEN), getSeenMsg));
        out = "<inbox>";
        for (int i = 0, n = messages.length; i < n; i++) {
            messages[i].setFlag(Flag.SEEN, true);
            Message message = messages[i];
            Address[] froms = message.getFrom();
            String fromemail = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            String recipientsmail = recipients == null ? null : ((InternetAddress) recipients[0]).getAddress();

            String content = "";
            if (getTextHtmlMsg) {
                if (message.getContentType().toLowerCase().contains("text/plain") || message.getContentType().toLowerCase().contains("text/html")) {
                    content = checkBody(message);

                } else if (message.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) message.getContent();
                    int count = mp.getCount();
                    for (int c = 0; c < count; c++) {
                        content += checkPart(mp.getBodyPart(c));
                    }
                } else {
                    content = "<body>NULL</body>";
                }
            } else {
                content = "<body>" + URLEncoder.encode(convertStreamToString(message.getInputStream()), "UTF-8") + "</body>";
            }

            out = out + "\n<email>" +
                "\n<from>" + fromemail + "</from>\n" +
                "\n<to>" + recipientsmail + "</to>\n" +
                "\n<subject>" + URLEncoder.encode(message.getSubject(), "UTF-8") + "</subject>\n" +
                "\n" + content + "\n" +
                "\n</email>\n";
        }
        out = out + "</inbox>";
        inbox.close(false);
        store.close();
    } catch (NoSuchProviderException ex) {
        System.out.println(ex.getMessage());

    } catch (MessagingException | IOException ex) {
        System.out.println(ex.getMessage());
    }
    return out;
}
public static String convertStreamToString(java.io.InputStream is) throws IOException {
    return IOUtils.toString(is, StandardCharsets.UTF_8);
}
public static String checkPart(BodyPart bodypart) throws IOException, MessagingException {
    if (bodypart.getContentType().toLowerCase().contains("text/plain") || bodypart.getContentType().toLowerCase().contains("text/html")) {
        return "<body>" + URLEncoder.encode(convertStreamToString(bodypart.getInputStream()), "UTF-8") + "</body>";
    } else {
        return "<body>" + URLEncoder.encode("NOT text/plain OR text/html BODY PART", "UTF-8") + "</body>";
    }
}
public static String checkBody(Message message) throws IOException, MessagingException {
    if (message.getContentType().toLowerCase().contains("text/plain") || message.getContentType().toLowerCase().contains("text/html")) {
        return "<body>" + URLEncoder.encode(convertStreamToString(message.getInputStream()), "UTF-8") + "</body>";
    } else {
        return "<body>" + URLEncoder.encode("NOT text/plain OR text/html BODY PART", "UTF-8") + "</body>";
    }
}
    
}
