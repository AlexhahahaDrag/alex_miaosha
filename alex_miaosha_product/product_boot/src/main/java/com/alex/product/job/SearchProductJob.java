package com.alex.product.job;

import com.alex.product.enums.SourceType;
import com.alex.product.service.shopProduct.ShopProductService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchProductJob {

    private final ShopProductService shopProductService;

    @XxlJob("searchJdProductJobHandler")
    public void searchJdProductJobHandler() throws Exception {
        log.info("===============开始执行查询jd商品===================");
        long startTime = System.nanoTime();
        shopProductService.getShopProduct();
        log.info("===============结束执行查询jd商品===================耗时：{}", Duration.ofNanos(System.nanoTime() - startTime));
    }
}
