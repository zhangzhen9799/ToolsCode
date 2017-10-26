package com.email;

import lombok.extern.log4j.Log4j2;

import javax.mail.*;
import java.util.Properties;

@Log4j2
public class MailTest {

    public static void main(String[] args) {
        String protocol = "pop3";
        boolean isSSL = true;
        String host = "pop.qq.com";
        int port = 995;
        String username = "QQ号码@qq.com";
        String password = "授权嘛";

        Properties properties = new Properties();
        properties.put("mail.pop3.ssl.enable", isSSL);
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
        Session session = Session.getDefaultInstance(properties);

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
            // int size = folder.getMessageCount();
            // 获取与给定消息号相对应的Message对象
            // Message message = folder.getMessage(size);

            Message[] message = folder.getMessages();

            Thread thread = new Thread(new GetMailInfoThread(message));
            thread.start();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            log.error("{}", e);
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("{}", e);
        } finally {
//            try {
//                if (folder != null) {
//                    folder.close(false);
//                }
//                if (store != null) {
//                    store.close();
//                }
//            } catch (MessagingException e) {
//                e.printStackTrace();
//                log.error("{}", e);
//            }
        }
    }

}
