package com.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LabelCrawler {

    public static void main(String[] args) {

        for (int i = 1; i < 104; i ++) {
            System.out.println("----------第" + i + "页----------");
            try {
                Document document = Jsoup.connect("https://www.oschina.net/question/tags?catalog=0&p=" + i)
                        .ignoreContentType(true)
                        .get();
                Elements elements = document.select("div.all-tags").select("li");
                for (Element element: elements) {
                    String ioc = element.select("img").attr("src");
                    if ("".equals(ioc)) {
                        ioc = "无";
                    } else {
                        if (!ioc.contains("http")) {
                            ioc = "https://www.oschina.net" + ioc;
                        }
                    }
                    String tagName = element.select("a").text();
                    System.out.println("ioc:" + ioc+ "    " + "tagName:" + tagName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
