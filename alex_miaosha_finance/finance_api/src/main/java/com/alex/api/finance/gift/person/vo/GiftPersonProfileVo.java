package com.alex.api.finance.gift.person.vo;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftPersonProfileVo", description = "gift person profile")
public class GiftPersonProfileVo {

    private GiftPersonBusinessVo person;

    private List<GiftRecordInfoTVo> records = new ArrayList<>();
}
