package com.crawler.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * 验证码识别工具类
 */
@Log4j2
public class TYCUtils {

    /**
     * 打码
     * @param token
     * @return
     */
    public static int killCaptcha(String token) {
        String id = builderImage(token);
        if ("".equals(id)) {
            return -1;
        }
        String imgString = getImageString("img_code.png");
        if ("".equals(imgString)) {
            return -1;
        }
        String result = postImage(imgString);
        JSONObject jsonObject = JSON.parseObject(result);
        String err_no = jsonObject.getString("err_no");
        String err_str = jsonObject.getString("err_str");
        String pic_id = jsonObject.getString("pic_id");
        String pic_str = jsonObject.getString("pic_str");
        String md5 = jsonObject.getString("md5");
        log.info("err_no:{}", err_no);
        log.info("err_str:{}", err_str);
        log.info("pic_id:{}", pic_id);
        log.info("pic_str:{}", pic_str);
        log.info("md5:{}", md5);
        String[] captcha = pic_str.split("\\|");
        JSONArray jsonArray = new JSONArray();
        for (String s: captcha) {
            String[] temp = s.split(",");
            int x = Integer.parseInt(temp[0]);
            int y = Integer.parseInt(temp[1]) - 100;
            JSONObject json = new JSONObject();
            json.put("x", x);
            json.put("y", y);
            jsonArray.add(json);
        }
        log.info("JsonArray:{}", jsonArray.toJSONString());
        String clickLocs = jsonArray.toJSONString();
        String status = checkCaptcha(id, clickLocs, token);
        JSONObject json = JSON.parseObject(status);
        if ("ok".equals(json.getString("state"))) {
            log.info("识别成功！");
            return 1;
        } else {
            log.info("识别失败！");
            reportError(pic_id);
            log.info("已申请报错返分！");
            return 0;
        }
    }

    /**
     * 构造验证码
     * @param token
     * @return
     */
    private static String builderImage(String token) {
        String captcha = getCaptcha(token);
        JSONObject jsonObject = JSON.parseObject(captcha);
        if ("ok".equals(jsonObject.getString("state"))) {
            jsonObject = JSON.parseObject(jsonObject.getString("data"));
            String id = jsonObject.getString("id");
            String bgImage = jsonObject.getString("bgImage");
            String targetImage = jsonObject.getString("targetImage");
            String userIp = jsonObject.getString("userIp");
            String currentTime = jsonObject.getString("currentTime");
            String mobile = jsonObject.getString("mobile");
            log.info("id:{}", id);
            log.info("bgImage:{}", bgImage);
            log.info("targetImage:{}",targetImage);
            log.info("userIp:{}", userIp);
            log.info("currentTime:{}", currentTime);
            log.info("mobile:{}", mobile);
            if (1 != generateImage(bgImage, "bottom.png")) {
                return "";
            }
            if (1 != generateImage(targetImage, "top.png")) {
                return "";
            }
            if (1 != createImgCode()) {
                return "";
            }
            return id;
        }
        return "";
    }

    /**
     * 识别结果提交
     * @param captchaId
     * @param clickLocs
     * @param cookie
     * @return
     */
    private static String checkCaptcha(String captchaId, String clickLocs, String cookie) {

        long t = System.currentTimeMillis();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://antirobot.tianyancha.com/captcha/checkCaptcha.json?captchaId=" + captchaId + "&clickLocs=" + clickLocs + "&t=" + t + "&_=" + (t - 12345))
                .get()
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3408.400 QQBrowser/9.6.12028.400")
                .build();

        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            log.info("Status Code:{}", response.code());
            log.info("Result:{}", result);
            return result;
        } catch (IOException e) {
            log.error("{}", e);
            return "";
        }

    }

    /**
     * 获取验证码
     * @param cookie
     * @return
     */
    private static String getCaptcha(String cookie) {

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder()
                .url("https://antirobot.tianyancha.com/captcha/getCaptcha.json")
                .get()
                .addHeader("Cookie", cookie)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3408.400 QQBrowser/9.6.12028.400")
                .build();

        try {
            Response response = client.newCall(request).execute();
            log.info("Status Code:{}", response.code());
            String result = response.body().string();
            log.info("Result:{}", result);
            return result;
        } catch (IOException e) {
            log.error("{}", e);
        }

        return "";

    }

    /**
     * 登录获取Token
     * @param mobile 账号
     * @param cdpassword 密码
     * @return
     */
    private static String getToken(String mobile, String cdpassword) {

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        JSONObject jsonObject = new JSONObject();
        // 用户名
        jsonObject.put("mobile", mobile);
        // 密码的md5值
        cdpassword = toMd5(cdpassword);
        jsonObject.put("cdpassword", cdpassword);
        jsonObject.put("loginway", "PL");
        jsonObject.put("autoLogin", "true");
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toJSONString());

        Request request = new Request.Builder()
                .url("https://www.tianyancha.com/cd/login.json")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3408.400 QQBrowser/9.6.12028.400")
                .build();

        try {
            Response response = client.newCall(request).execute();
            log.info("Status Code:{}", response.code());
            String result = response.body().string();
            log.info("Result:{}", result);
            return result;
        } catch (IOException e) {
            log.error("{}", e);
        }

        return "";

    }

    /**
     * 图片识别
     * @param imgBase64
     * @return
     */
    private static String postImage(String imgBase64) {

        FormBody formBody = new FormBody.Builder()
                .add("user", "账号")//账号
                .add("pass", "密码")//密码
                .add("softid", "软件ID")//软件ID
                .add("codetype", "9004")
                .add("len_min", "0")
                .add("file_base64", imgBase64)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("http://upload.chaojiying.net/Upload/Processing.php?=1&=1&=1&=9004&=0&=0")
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            log.info("Status Code:{}", response.code());
            String result = response.body().string();
            log.info("Result:{}", result);
            return result;
        } catch (IOException e) {
            log.error("{}", e);
        }

        return "";

    }

    /**
     * 错误返分
     * @param id
     * @return
     */
    private static String reportError(String id) {

        FormBody formBody = new FormBody.Builder()
                .add("user", "用户名")
                .add("pass", "密码")
                .add("softid", "软件ID")
                .add("id", id)
                .build();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://upload.chaojiying.net/Upload/ReportError.php")
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            log.info("Status Code:{}", response.code());
            String result = response.body().string();
            log.info("Result:{}", result);
            return result;
        } catch (IOException e) {
            log.error("{}", e);
        }

        return "";

    }

    /**
     * 将验证码图片与识别规则模板图片拼接
     * @return
     */
    private static int createImgCode() {
        try {
            // 读取第一张图片
            File fileOne = new File("top.png");
            BufferedImage imageOne = ImageIO.read(fileOne);
            // 读取图片的宽度和高度
            int widthOne = imageOne.getWidth();
            int heightOne = imageOne.getHeight();
            // 从图片中读取RGB
            int[] imageArrayOne = new int[widthOne * heightOne];
            imageArrayOne = imageOne.getRGB(0, 0, widthOne, heightOne, imageArrayOne, 0, widthOne);
            // 对第二张图片做相同的处理
            File fileTwo = new File("bottom.png");
            BufferedImage imageTwo = ImageIO.read(fileTwo);
            int widthTwo = imageTwo.getWidth();
            int heightTwo = imageTwo.getHeight();
            int[] imageArrayTwo = new int[widthTwo * heightTwo];
            imageArrayTwo = imageTwo.getRGB(0, 0, widthTwo, heightTwo, imageArrayTwo, 0, widthTwo);
            // 第三张图片
            File fileThree = new File("mould.png");
            BufferedImage imageThree = ImageIO.read(fileThree);
            int widthThree = imageThree.getWidth();
            int heightThree = imageThree.getHeight();
            int[] imageArrayThree = new int[widthThree * heightThree];
            imageArrayThree = imageThree.getRGB(0, 0, widthThree, heightThree, imageArrayThree, 0, widthThree);
            // 生成新图片
            BufferedImage imageNew = new BufferedImage(widthOne, heightOne + heightTwo + heightThree, BufferedImage.TYPE_INT_RGB);
            // 设置顶部的RGB
            imageNew.setRGB(0, 0, widthOne, heightOne, imageArrayOne, 0, widthOne);
            // 设置中部的RGB
            imageNew.setRGB(0, heightOne, widthThree, heightThree, imageArrayThree, 0, widthThree);
            // 设置底部的RGB
            imageNew.setRGB(0, heightOne + heightThree, widthTwo, heightTwo, imageArrayTwo, 0, widthTwo);
            // 写图片
            File outFile = new File("img_code.png");
            ImageIO.write(imageNew, "png", outFile);
            return 1;
        } catch (Exception e) {
            log.error("{}", e);
            return -1;
        }
    }

    /**
     * 进行Base64编码
     * @param imgFile
     * @return
     */
    private static String getImageString(String imgFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            log.error("{}", e);
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回Base64编码过的字节数组字符串
        if (data != null) {
            return encoder.encode(data);
        }
        return "";
    }

    /**
     * 进行Base64解码并生成图片
     * @param imgBase64
     * @param imgFilePath
     * @return
     */
    private static int generateImage(String imgBase64, String imgFilePath) {
        // 对字节数组字符串进行Base64解码并生成图片
        if (imgBase64 == null) {
            // 图像数据为空
            return -1;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] data = decoder.decodeBuffer(imgBase64);
            for (int i = 0; i < data.length; ++i) {
                if (data[i] < 0) {
                    // 调整异常数据
                    data[i] += 256;
                }
            }
            // 生成图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(data);
            out.flush();
            out.close();
            return 1;
        } catch (Exception e) {
            log.error("{}", e);
            return -1;
        }
    }

    /**
     * 字符串转md5
     * @param s
     * @return
     */
    private static String toMd5(String s) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            log.error("{}", e);
            return null;
        }
    }

    /**
     * 生成识别规则模板图片
     */
    public static void createImgMould() {
        int width = 320;
        int height = 70;
        String s = "按上图顺序点击";

        File file = new File("mould.png");

        Font font = new Font("微软雅黑", Font.BOLD, 33);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = (Graphics2D) bi.getGraphics();
        graphics2D.setFont(font);
        graphics2D.setBackground(Color.WHITE);
        graphics2D.clearRect(0, 0, width, height);
        graphics2D.setPaint(Color.BLACK);

        FontRenderContext context = graphics2D.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(s, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;

        graphics2D.drawString(s, (int) x, (int) baseY);

        try {
            ImageIO.write(bi, "png", file);
        } catch (IOException e) {
            log.error("{}", e);
        }
    }

}
