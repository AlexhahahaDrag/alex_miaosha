package com.alex.finance.personalGift.service;

import com.alex.api.finance.personalGift.vo.ContactsGiftRecordVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftOccasionDistributionVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftStatisticVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftTrendVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.alex.finance.personalGift.entity.PersonalGift;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * AI Agent: 获取联系人随礼记录列表
     * 用于前端展示"联系人记录"页面，显示每个联系人的随礼、收礼、净差额等统计信息
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param queryCondition 查询条件
     * @return 联系人随礼记录分页结果
     */
    Page<ContactsGiftRecordVo> getContactsGiftRecordList(Long pageNum, Long pageSize, ContactsGiftRecordVo queryCondition);

    /**
     * AI Agent: 获取个人随礼统计概览
     * 用于前端展示统计概览页面，包含本月、年度的随礼收礼数据、环比、同比、联系人统计等
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼统计概览数据
     */
    PersonalGiftStatisticVo getPersonalGiftStatistic(String startTime, String endTime);

    /**
     * AI Agent: 获取个人随礼近12个月趋势
     * 用于前端展示"个人随礼趋势"页面，显示近12个月的随礼收礼趋势
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统近12个月开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼近12个月趋势数据列表
     */
    List<PersonalGiftTrendVo> getPersonalGiftTrend(String startTime, String endTime);

    /**
     * AI Agent: 获取个人随礼场合分布
     * 用于前端展示"个人随礼场合分布"页面，显示不同场合的随礼数量
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼场合分布数据列表
     */
    List<PersonalGiftOccasionDistributionVo> getPersonalGiftOccasionDistribution(String startTime, String endTime);
}
