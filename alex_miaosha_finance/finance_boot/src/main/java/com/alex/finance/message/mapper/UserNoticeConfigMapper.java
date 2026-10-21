package com.alex.finance.message.mapper;

import com.alex.finance.message.entity.UserNoticeConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户个性化消息通知配置 Mapper 接口
 */
@Mapper
public interface UserNoticeConfigMapper extends BaseMapper<UserNoticeConfig> {
}
