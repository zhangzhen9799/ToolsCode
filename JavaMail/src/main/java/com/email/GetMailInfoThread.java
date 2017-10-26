package com.email;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import static java.util.regex.Pattern.*;

/**
 * 获取邮箱中邮件信息的线程
 */
@Log4j2
@AllArgsConstructor
public class GetMailInfoThread extends Thread {

    private Message[] message = null;

    @Override
    public void run() {
        if (null != message) {
            for (int i = 0; i < message.length; i ++) {
                try {
                    MailInfo mailInfo = new MailInfo((MimeMessage) message[i]);
                    log.info("邮件　" + i + "　主题:　" + mailInfo.getSubject());
                    log.info("邮件　" + i + "　是否需要回复:　" + mailInfo.getReplySign());
                    log.info("邮件　" + i + "　是否已读:　" + mailInfo.isNew());
                    log.info("邮件　" + i + "　是否包含附件:　" + mailInfo.isContainAttach((Part) message[i]));
                    log.info("邮件　" + i + "　发送时间:　" + mailInfo.getSentDate());
                    log.info("邮件　" + i + "　发送人地址:　" + mailInfo.getFrom());
                    log.info("邮件　" + i + "　收信人地址:　" + mailInfo.getMailAddress("to"));
                    log.info("邮件　" + i + "　抄送:　" + mailInfo.getMailAddress("cc"));
                    log.info("邮件　" + i + "　暗抄:　" + mailInfo.getMailAddress("bcc"));
                    log.info("邮件　" + i + "　发送时间:　" + mailInfo.getSentDate());
                    log.info("邮件　" + i + "　邮件ID:　" + mailInfo.getMessageId());
                    mailInfo.getMailContent((Part) message[i]);
                    log.info("邮件　" + i + "　正文内容:　\r\n" + mailInfo.getBodyText());

                    Document document = Jsoup.parse(mailInfo.getBodyText());
                    String href = null;
                    href = document.select("a.sel_btn").attr("href");
                    log.info("href:{}", href);
                    if (href != null) {
                        //http://warning.51wyq.cn/more.action?reviewCode=E5m6kyOyqGH&channelTag=1
                        Matcher matcher = compile("reviewCode=([0-9A-Za-z]+)").matcher(href);
                        if (matcher.find()) {
                            log.info(matcher.group(1));
                            Document doc = Jsoup.connect("http://warning.51wyq.cn/warningCenter/getMoreWarningDetail.shtml")
                                    .ignoreContentType(true)
                                    .data("reviewCode", matcher.group(1))
                                    .data("page", "1")
                                    .data("pagesize", "10")
                                    .data("tendencyCondition", "0")
                                    .header("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                                    .post();
                            log.info(doc.text());
                        }
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                    log.error("{}", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{}", e);
                }
            }
        }
    }
}