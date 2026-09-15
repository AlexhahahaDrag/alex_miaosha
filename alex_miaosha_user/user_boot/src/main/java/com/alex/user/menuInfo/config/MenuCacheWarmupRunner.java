package com.alex.user.menuInfo.config;

import com.alex.user.menuInfo.service.MenuInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Warm global {@code menu_all_tree} into Redis after user-boot starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MenuCacheWarmupRunner implements ApplicationRunner {

    private final MenuInfoService menuInfoService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            menuInfoService.warmMenuAllTree();
            log.info("MenuCacheWarmupRunner: menu_all_tree warm completed");
        } catch (Exception e) {
            log.error("MenuCacheWarmupRunner: warm failed (non-fatal): {}", e.getMessage());
        }
    }
}
