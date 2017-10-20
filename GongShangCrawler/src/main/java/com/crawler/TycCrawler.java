package com.crawler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class TycCrawler {

    public static void main(String[] args) {

        for (int i = 0; i < 100; i ++) {
            doGet();
        }

    }

    public static void doGet() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://www.tianyancha.com/search?key=百度&checkFrom=searchBox")
                .get()
                .addHeader("cookie", "auth_token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMzU2Mjc3MTg1OCIsImlhdCI6MTUwODM3OTQyNywiZXhwIjoxNTIzOTMxNDI3fQ.b6BCbM8ep2YJ31-NJhshFLDOEQJX73u1fbDqO9iHQ5C5iMjhapPto_rvUlkFQ_vyelGVglvGtJWqBTDdeG7YHg")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.3408.400 QQBrowser/9.6.12028.400")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
            System.out.println(response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
