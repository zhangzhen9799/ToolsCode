package com.email;

import javax.mail.*;
import java.util.Date;
import java.util.Properties;

public class JavaMailTest {

    public static void main(String[] args) {
        String protocol = "pop3";
        boolean isSSL = true;
        String host = "pop.qq.com";
        int port = 995;
        String username = "577726590@qq.com";
        String password = "gxfiirauibaqbdcf";

        Properties props = new Properties();
        props.put("mail.pop3.ssl.enable", isSSL);
        props.put("mail.pop3.host", host);
        props.put("mail.pop3.port", port);

        Session session = Session.getDefaultInstance(props);

        Store store = null;
        Folder folder = null;
        try {
            store = session.getStore(protocol);
            store.connect(host, username, password);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            // 新邮件数量
            // int size = folder.getNewMessageCount();
            // 获取邮件总数
            int size = folder.getMessageCount();
            System.out.println(size);
            // 获取与给定消息号相对应的Message对象
            Message message = folder.getMessage(size);

//            Message message[] = folder.getMessages();

//            new MailInfo((MimeMessage) message);
            String from = message.getFrom()[0].toString();
            String subject = message.getSubject();
            Date date = message.getSentDate();
            String body = "";
            try {
                body = (String)message.getContentType();
//                message.writeTo(System.out);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("From: " + from);
            System.out.println("Subject: " + subject);
            System.out.println("Date: " + date);
            System.out.println("Body:" + body);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            try {
                if (folder != null) {
                    folder.close(false);
                }
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

}
