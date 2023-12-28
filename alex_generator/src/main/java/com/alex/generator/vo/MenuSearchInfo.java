package com.alex.generator.vo;

import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import lombok.Data;

/**
 * @description:  查询菜单信息
 * @author:       majf
 * @createDate:   2023/12/27 14:02
 * @version:      1.0.0
 */
@Data
public class MenuSearchInfo {

    private Integer orderBy = 0;

    private MenuInfoVo menuInfoVo;

    private Boolean menuExists = false;
}
