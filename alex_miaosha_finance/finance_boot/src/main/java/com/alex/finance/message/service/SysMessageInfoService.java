package com.alex.finance.message.service;

import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.finance.message.vo.SysMessageSummaryVo;
import com.alex.finance.message.entity.SysMessageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统消息与业务到期提醒服务接口
 */
public interface SysMessageInfoService extends IService<SysMessageInfo> {

    /**
     * XXL-Job 后台定时任务全量巡检（无登录态上下文）：
     * 扫描礼尚往来事件（提前1天/当天）、消费券到期、猫超卡及预付卡到期提醒并按归属人精准落库
     *
     * @return 本次新生成的消息条数
     */
    int executeRemindJob();

    /**
     * 获取当前用户未读消息统计汇总（纯查询，不隐式触发全表扫描）
     */
    SysMessageSummaryVo getSummary();

    /**
     * 分页查询消息列表（纯查询，不隐式触发全表扫描）
     */
    Page<SysMessageInfoVo> getPage(Long pageNum, Long pageSize, SysMessageInfoVo query);

    /**
     * 标记单条消息已读
     */
    Boolean markRead(Long id);

    /**
     * 一键全部标为已读（可按消息大类过滤）
     */
    Integer markAllRead(String msgType);

    /**
     * 发布系统通知或自定义提醒
     */
    SysMessageInfoVo publishSystemMessage(SysMessageInfoVo vo);

    /**
     * 批量删除消息（逗号分隔 ID）
     */
    Boolean deleteMessages(String ids);
}
