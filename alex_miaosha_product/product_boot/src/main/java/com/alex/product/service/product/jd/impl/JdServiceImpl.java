package com.alex.product.service.product.jd.impl;

import com.alex.api.product.vo.product.jd.Content;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.service.product.jd.JdService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JdServiceImpl implements JdService {

    public static List<Content> parseJD(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        Document document = Jsoup.parse(new URL(new String(url.getBytes(), "utf-8")), 3000);
        Element j_goodsList = document.getElementById("J_goodsList");
        Elements lis = j_goodsList.getElementsByTag("li");
        List<Content> list = new ArrayList<>();
        for (Element element : lis) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            if (!StringUtils.isEmpty(price) && price.indexOf("￥") >= 0) {
                price = price.substring(1);
            }
            String name = element.getElementsByClass("p-name").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            Content content = new Content(img, price, name, shop);
            list.add(content);
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        JdServiceImpl jdService = new JdServiceImpl();
        List<Content> contents11 = jdService.parseJD("airpodspro二代");
        System.out.println(contents11);
    }
}
