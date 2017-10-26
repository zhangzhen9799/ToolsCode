package com.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Log4j2
public class MailInfo {

    /**
     * MIME样式的电子邮件消息对象
     */
    private MimeMessage mimeMessage;
    /**
     * 附件下载后的存放目录
     */
    private final String saveAttachPath = "";
    /**
     * 存放邮件内容的StringBuffer对象
     */
    private StringBuffer bodyText = new StringBuffer();
    /**
     * 日期显示格式
     */
    private static String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 构造方法
     */
    public MailInfo(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }

    /**
     * 获得发件人的地址和姓名
     */
    public String getFrom() throws Exception {
        InternetAddress[] address = (InternetAddress[]) mimeMessage.getFrom();
        String fromAddress= address[0].getAddress();
        if (fromAddress == null) {
            fromAddress = "";
            log.info("无法知道发送者地址！");
        }
        String fromPersonal = address[0].getPersonal();
        if (fromPersonal == null) {
            fromPersonal = "";
            log.info("无法知道发送者的姓名！");
        }
        String from = null;
        if (fromPersonal != null || fromAddress != null) {
            from = fromPersonal + "<" + fromAddress + ">";
            log.info("发送者是：" + from);
        }
        return from;
    }

    /**
     * 根据所传递的参数的不同，获得邮件的收件人、抄送、和密送的地址和姓名
     * "TO"---收件人
     * "CC"---抄送人地址
     * "BCC"---密送人地址
     */
    public String getMailAddress(String type) throws Exception {
        String mailAddress = "";
        String addType = type.toUpperCase();
        InternetAddress[] address = null;
        if ("TO".equals(addType) || "CC".equals(addType) || "BCC".equals(addType)) {
            switch (addType) {
                case "TO":address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);break;
                case "CC":address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);break;
                case "BCC":address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);break;
                default:log.error("错误的电子邮件类型！");
            }
            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String emailAddress = address[i].getAddress();
                    if (emailAddress == null) {
                        emailAddress = "";
                    } else {
                        log.info("转换之前的EmailAddress:{}", emailAddress);
                        emailAddress = MimeUtility.decodeText(emailAddress);
                        log.info("转换之后的EmailAddress:{}", emailAddress);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null) {
                        personal = "";
                    } else {
                        log.info("转换之前的Personal:{}", personal);
                        personal = MimeUtility.decodeText(personal);
                        log.info("转换之后的Personal:{}", personal);
                    }
                    String compositeTo = personal + "<" + emailAddress + ">";
                    log.info("完整的邮件地址：" + compositeTo);
                    mailAddress += "," + compositeTo;
                }
                mailAddress = mailAddress.substring(1);
            }
        } else {
            throw new Exception("错误的电子邮件类型！");
        }
        return mailAddress;
    }

    /**
     * 获得邮件主题
     */
    public String getSubject() throws Exception {
        String subject = null;
        log.info("转换前的Subject:{}", mimeMessage.getSubject());
        subject = MimeUtility.decodeText(mimeMessage.getSubject());
        log.info("转换后的Subject:{}", subject);
        if (subject == null) {
            subject = "";
        }
        return subject;
    }

    /**
     * 获得邮件发送日期
     */
    public String getSentDate() throws Exception {
        Date sentDate = mimeMessage.getSentDate();
        log.info("发送日期 原始类型:{}", sentDate);
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        String strSentDate = format.format(sentDate);
        log.info("发送日期 可读类型:{}", strSentDate);
        return strSentDate;
    }

    /**
     * 获得邮件正文内容
     */
    public String getBodyText() {
        return bodyText.toString();
    }

    public void getMailContent(Part part) throws Exception {
        String contentType = part.getContentType();
        log.info("邮件的MimeType类型:{}", contentType);
        int nameIndex = contentType.indexOf("name");
        boolean conName = false;
        if (nameIndex != -1) {
            conName = true;
        }
        if (part.isMimeType("text/plain") && conName == false) {
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && conName == false) {
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent((Part) part.getContent());
        } else {
            log.error("Error!");
        }
    }

    /**
     * 判断此邮件是否需要回执，如果需要回执返回true否则返回false
     */
    public boolean getReplySign() throws Exception {
        boolean replySign = false;
        String[] needReply = mimeMessage.getHeader("Disposition-Notification-To");
        if (needReply != null) {
            replySign = true;
        }
        if (replySign) {
            log.info("该邮件需要回复！");
        } else {
            log.info("该邮件不需要回复！");
        }
        return replySign;
    }

    /**
     * 获得此邮件的Message-ID
     */
    public String getMessageId() throws MessagingException {
        String messageID = mimeMessage.getMessageID();
        log.info("邮件ID:{}", messageID);
        return messageID;
    }

    /**
     * 判断此邮件是否已读，如果未读返回false反之返回true
     */
    public boolean isNew() throws Exception {
        boolean isNew = false;
        Flags flags = mimeMessage.getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isNew = true;
                log.info("seen email...");
                break;
            }
        }
        return isNew;
    }

    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachFlag = false;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i ++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    attachFlag = true;
                } else if (mPart.isMimeType("multipart/*")) {
                    attachFlag = isContainAttach((Part) mPart);
                } else {
                    String conType = mPart.getContentType();
                    if (conType.toLowerCase().indexOf("application") != -1) {
                        attachFlag = true;
                    }
                    if (conType.toLowerCase().indexOf("name") != -1) {
                        attachFlag = true;
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachFlag = isContainAttach((Part) part.getContent());
        }
        return attachFlag;
    }

    /**
     * 保存附件
     */
    public void saveAttachMent(Part part) throws Exception {
        String fileName;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    fileName = mPart.getFileName();
                    if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mPart.getInputStream());
                } else if (mPart.isMimeType("multipart/*")) {
                    saveAttachMent(mPart);
                } else {
                    fileName = mPart.getFileName();
                    if ((fileName != null) && (fileName.toLowerCase().indexOf("GB2312") != -1)) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mPart.getInputStream());
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent());
        }
    }

    /**
     * 保存附件到指定目录里
     */
    private void saveFile(String fileName, InputStream in) throws Exception {
        String osName = System.getProperty("os.name");
        String storeDir = getSaveAttachPath();
        String separator;
        if (osName == null) {
            osName = "";
        }
        if (osName.toLowerCase().indexOf("win") != -1) {
            separator = "\\";
            if (storeDir == null || storeDir.equals("")) {
                storeDir = "c:\\tmp";
            }
        } else {
            separator = "/";
            storeDir = "/tmp";
        }
        File storeFile = new File(storeDir + separator + fileName);
        log.info("附件的保存地址:{}", storeFile.toString());
        // for(int　i=0;storefile.exists();i++){
        // storefile　=　new　File(storedir+separator+fileName+i);
        // }
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storeFile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new Exception("文件保存失败!");
        } finally {
            bos.close();
            bis.close();
        }
    }

}
