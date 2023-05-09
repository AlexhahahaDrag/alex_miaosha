package com.alex.product.service.shopProduct.jd.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.Content;
import com.alex.product.entity.Info;
import com.alex.product.service.shopProduct.jd.JdProductService;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
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
public class JdProductServiceImpl implements JdProductService {

    public List<Content> parseJD(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        Document document = Jsoup.parse(new URL(new String(url.getBytes(), "utf-8")), 3000);
        Element j_goodsList = document.getElementById("J_goodsList");
        Elements lis = j_goodsList.getElementsByTag("li");
        List<Content> list = new ArrayList<>();
        for (Element element : lis) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            if (StringUtils.isEmpty(price) && price.indexOf("￥") >= 0) {
                price = price.substring(1);
            }
            String productUrl = element.getElementsByClass("p-img").get(0).getElementsByTag("a").get(0).attr("href");
            String name = element.getElementsByClass("p-name").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            String icons = element.getElementsByClass("p-icons").eq(0).text();
            parseJDDetail(productUrl);
            Content content = Content.builder()
                    .image(img)
                    .name(name)
                    .shop(shop)
                    .price(price)
                    .icons(icons)
                    .productUrl(productUrl)
                    .build();
            list.add(content);
        }
        System.out.println(list.size());
        return list;
    }

    public List<Content> parseJDDetail(String url) throws IOException {
//        Document document = Jsoup.parse(new URL(new String(("https:" + url).getBytes(), "utf-8")), 3000);
        Document document = Jsoup.parse(new URL(new String(("https://item.jd.com/100049060778.html").getBytes(), "utf-8")), 3000);
        Elements select1 = document.select("div#choose-attrs");
        Elements prices1 = document.select("div#choose-attr-1 div.dd");
        Elements prices = document.select("div#choose-attr-1 div.dd ul li i");
        Elements select = document.select("div#choose-attr-1 div.dd");
        Elements items = document.select("div#choose-attr-1 div.dd ul li");
        Element j_goodsList = document.getElementById("J_goodsList");
        Elements lis = j_goodsList.getElementsByTag("li");

        List<Content> list = new ArrayList<>();
        for (Element element : lis) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            if (StringUtils.isEmpty(price) && price.indexOf("￥") >= 0) {
                price = price.substring(1);
            }
            String productUrl = element.getElementsByClass("p-img").get(0).getElementsByTag("a").get(0).attr("href");
            String name = element.getElementsByClass("p-name").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            String icons = element.getElementsByClass("p-icons").eq(0).text();
            Content content = Content.builder()
                    .image(img)
                    .name(name)
                    .shop(shop)
                    .price(price)
                    .icons(icons)
                    .productUrl(productUrl)
                    .build();
            list.add(content);
        }
        System.out.println(list.size());
        return list;
    }

    public static void main(String[] args) throws IOException {
        JdProductServiceImpl jdProductService = new JdProductServiceImpl();
        List<Content> contents11 = jdProductService.parseJD("耳机");
        System.out.println(JSONUtil.toJsonStr(contents11));
    }
}
