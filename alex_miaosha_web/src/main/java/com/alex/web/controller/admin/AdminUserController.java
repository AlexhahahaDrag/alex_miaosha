package com.alex.web.controller.admin;

import com.alex.web.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  管理员相关 controller
 * @author:       majf
 * @createDate:   2022/8/9 11:54
 * @version:      1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;


}
