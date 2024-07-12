package com.alex.finance.personalGift.service;

import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.personalGift.entity.PersonalGift;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人随礼信息表 服务类
 * author: alex
 * createDate: 2024-07-10 10:01:28
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PersonalGiftService extends IService<PersonalGift> {

    Page<PersonalGiftVo> getPage(Long pageNum, Long pageSize, PersonalGiftVo personalGiftVo);

    PersonalGiftVo queryPersonalGift(Long id);

    Boolean addPersonalGift(PersonalGiftVo personalGiftVo);

    Boolean updatePersonalGift(PersonalGiftVo personalGiftVo);

    Boolean deletePersonalGift(String ids);

    Boolean noticePersonalGift(Long id);

    Boolean importPersonalGift(MultipartFile file) throws Exception;
}
