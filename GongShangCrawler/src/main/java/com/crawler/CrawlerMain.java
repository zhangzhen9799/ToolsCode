package com.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Log4j2
public class CrawlerMain {

    public static void main(String[] args) {

        String result = getValidate();
        log.info("{}", result);
        JSONObject json = JSON.parseObject(result);
        String searchWord = "百度";
        if ("1".equals(json.getString("success"))) {
            log.info("识别成功！");
            String validate = json.getString("validate");
            String challenge = json.getString("challenge");
            val param = new HashMap<String, String>();
            param.put("tab", "ent_tab");
            param.put("token", getToken());
            param.put("searchword", searchWord);
            param.put("geetest_challenge", challenge);
            param.put("geetest_validate", validate);
            param.put("geetest_seccode", validate + "|jordan");
            String html = getHtml(param);
            System.out.println(html);
        } else {
            log.info("识别失败！");
        }
    }

    private static String getHtml(Map<String, String> param) {

        try {
            Document document = Jsoup.connect("http://www.gsxt.gov.cn/corp-query-search-1.html")
                    .ignoreContentType(true)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3141.7 Safari/537.36")
                    .data("tab", param.get("tab"))
                    .data("token", param.get("token"))
                    .data("searchword", param.get("searchword"))
                    .data("geetest_challenge", param.get("geetest_challenge"))
                    .data("geetest_validate", param.get("geetest_validate"))
                    .data("geetest_seccode", param.get("geetest_seccode"))
                    .post();
            return document.toString();
        } catch (IOException e) {
            log.error("{}", e);
        }
        return "";
    }

    private static String getValidate( ) {
        val param = getParam();
        param.put("refer", "http://www.gsxt.gov.cn/");
        param.put("uid", "UID");
        try {
            Document document = Jsoup.connect("http://api.ocr.fm:7080/geetest/online")
                    .ignoreContentType(true)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3141.7 Safari/537.36")
                    .data("gt", param.get("gt"))
                    .data("challenge", param.get("challenge"))
                    .data("refer", param.get("refer"))
                    .data("uid", param.get("uid"))
                    .get();
            return document.body().text();
        } catch (IOException e) {
            log.error("{}", e);
        }
        return "";
    }

    private static Map<String, String> getParam() {
        val param = new HashMap<String, String>();
        try {
            Document document = Jsoup.connect("http://www.gsxt.gov.cn/SearchItemCaptcha")
                    .ignoreContentType(true)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3141.7 Safari/537.36")
                    .get();
            log.info("{}", document.body().text());
            JSONObject json = JSON.parseObject(document.body().text());
            String gt = json.getString("gt");
            String challenge = json.getString("challenge");
            param.put("gt", gt);
            param.put("challenge", challenge);
        } catch (IOException e) {
            log.error("{}", e);
        }
        return param;
    }

    private static String getToken() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

}
