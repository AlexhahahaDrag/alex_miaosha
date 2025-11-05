package com.alex.finance.personalGift.mapper;

import com.alex.api.finance.personalGift.vo.ContactsGiftRecordVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftOccasionDistributionVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftStatisticVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftTrendVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.personalGift.entity.PersonalGift;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  个人随礼信息表 mapper
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@Mapper
public interface PersonalGiftMapper extends BaseMapper<PersonalGift> {

    @DataPermission(table = "t_personal_gift")
    Page<PersonalGiftVo> getPage(Page<PersonalGiftVo> page, @Param("personalGiftVo") PersonalGiftVo personalGiftVo);

    PersonalGiftVo queryPersonalGift(@Param("id") Long id);

    /**
     * 获取联系人随礼记录列表（包含统计数据）
     *
     * @param page 分页参数
     * @param queryCondition 查询条件
     * @return 联系人随礼记录分页结果
     */
    @DataPermission(table = "t_personal_gift")
    Page<ContactsGiftRecordVo> getContactsGiftRecordList(Page<ContactsGiftRecordVo> page, @Param("queryCondition") ContactsGiftRecordVo queryCondition);

    /**
     * AI Agent: 获取个人随礼统计概览
     * 用于前端展示统计概览页面，包含本月、年度的随礼收礼数据、环比、同比、联系人统计等
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼统计概览数据
     */
    PersonalGiftStatisticVo getPersonalGiftStatistic(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * AI Agent: 获取个人随礼近12个月趋势
     * 用于前端展示"个人随礼趋势"页面，显示近12个月的随礼收礼数据
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统近12个月开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼近12个月趋势数据列表
     */
    List<PersonalGiftTrendVo> getPersonalGiftTrend(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * AI Agent: 获取个人随礼场合分布
     * 用于前端展示"个人随礼场合分布"页面，显示不同场合的随礼收礼数据
     *
     * @param startTime 开始日期（格式：yyyy-MM-dd），不提供则使用系统当前月份开始日期
     * @param endTime 结束日期（格式：yyyy-MM-dd），不提供则使用系统当前日期
     * @return 个人随礼场合分布数据列表
     */
    List<PersonalGiftOccasionDistributionVo> getPersonalGiftOccasionDistribution(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
