package com.alex.api.user.dashboard;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 机构/家庭管理员工作台大盘 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "机构/家庭管理员工作台大盘数据")
public class OrgAdminDashboardVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "管辖机构/家庭主键 ID", example = "1001")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long orgId;

    @ApiModelProperty(value = "机构/家庭显示名称", example = "研发一部 / 幸福一家")
    private String orgName;

    @ApiModelProperty(value = "在册有效成员总数", example = "18")
    private Long memberCount;

    @ApiModelProperty(value = "关联角色数", example = "3")
    private Long roleCount;

    @ApiModelProperty(value = "机构负责人/家庭管理员", example = "alex")
    private String adminName;

    @ApiModelProperty(value = "当月综合总支出 (元)", example = "12800.00")
    private BigDecimal monthExpense;

    @ApiModelProperty(value = "当月综合总收入 (元)", example = "35000.00")
    private BigDecimal monthIncome;
}
