package com.alex.api.finance.vo.finance;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * description: AI 自然语言预算指令解析请求
 * author: alex
 * createDate: 2026-10-12
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceBudgetAiParseReq", description = "AI 自然语言预算指令解析请求")
public class FinanceBudgetAiParseReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "自然语言指令内容不能为空")
    @ApiModelProperty(value = "自然语言指令(如: 帮我设3000预算，只管餐饮和交通)", required = true)
    @JsonAlias({"command"})
    private String text;

    @ApiModelProperty(value = "自然语言指令(兼容前端 command 字段)")
    private String command;

    @ApiModelProperty(value = "预算月份(YYYY-MM，如 2026-10，为空默认当月)")
    private String budgetMonth;

    public String getText() {
        return StringUtils.isNotBlank(text) ? text : command;
    }

    public String getCommand() {
        return StringUtils.isNotBlank(command) ? command : text;
    }

    public void setCommand(String command) {
        this.command = command;
        if (StringUtils.isBlank(this.text)) {
            this.text = command;
        }
    }

    public void setText(String text) {
        this.text = text;
        if (StringUtils.isBlank(this.command)) {
            this.command = text;
        }
    }
}
