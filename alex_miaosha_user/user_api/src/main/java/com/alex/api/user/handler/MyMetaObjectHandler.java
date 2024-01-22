package com.alex.api.user.handler;

import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 *description:  设置新增和修改的默认时间
 *author:       alex
 *createDate:   2021/6/6 15:16
 *version:      1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyMetaObjectHandler implements MetaObjectHandler {

    private final UserUtils userUtils;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter("isValid")) {
            this.strictInsertFill(metaObject, "isValid", Integer.class, 1);
        }
        if (metaObject.hasSetter("isDelete")) {
            this.strictInsertFill(metaObject, "isDelete", Integer.class, 0);
        }
        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        }
        if (metaObject.hasSetter("operateTime")) {
            metaObject.setValue("operateTime", null);
            this.strictInsertFill(metaObject, "operateTime", LocalDateTime.class, now);
        }
        TUserVo loginUser = userUtils.getLoginUser();
        log.info("insertFill loginUser:{}", loginUser);
        Long id = Optional.ofNullable(userUtils.getLoginUser()).map(BaseVo::getId).orElse(null);
        if (id != null) {
            if (metaObject.hasSetter("creator")) {
                this.strictInsertFill(metaObject, "creator", Long.class, id);
            }
            if (metaObject.hasSetter("operator")) {
                this.strictInsertFill(metaObject, "operator", Long.class, id);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter("operateTime")) {
            metaObject.setValue("operateTime", null);
            this.strictUpdateFill(metaObject, "operateTime", LocalDateTime.class,  now);
        }
        if (metaObject.hasSetter("updateTime")) {
            metaObject.setValue("updateTime", null);
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class,  now);
        }
        Long id = Optional.ofNullable(userUtils.getLoginUser()).map(BaseVo::getId).orElse(null);
        if (id != null) {
            if (metaObject.hasSetter("updater")) {
                metaObject.setValue("updater", null);
                this.strictUpdateFill(metaObject, "updater", Long.class, id);
            }
            if (metaObject.hasSetter("operator")) {
                metaObject.setValue("operator", null);
                this.strictUpdateFill(metaObject, "operator", Long.class, id);
            }
        }
    }
}