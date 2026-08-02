package com.alex.finance.gift.record.service.impl;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GiftRecordInfoServiceImp extends ServiceImpl<GiftRecordInfoMapper, GiftRecordInfo>
        implements GiftRecordInfoService {

    private static final String DIRECTION_GIVE = "GIVE";
    private static final String DIRECTION_RECEIVE = "RECEIVE";
    private static final String DIRECTION_RETURN = "RETURN";

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftPersonInfoMapper giftPersonInfoMapper;
    private final GiftEventInfoMapper giftEventInfoMapper;
    private final GiftEventTypeOptionMapper giftEventTypeOptionMapper;

    @Override
    public Page<GiftRecordInfoVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftRecordInfoVo> getList(GiftRecordQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftRecordSummaryVo getSummary(GiftRecordQuery query) {
        List<GiftRecordInfo> records = getBaseMapper().listEntities(query);
        BigDecimal giveAmount = sumByDirection(records, DIRECTION_GIVE);
        BigDecimal receiveAmount = sumByDirection(records, DIRECTION_RECEIVE);
        BigDecimal returnAmount = sumByDirection(records, DIRECTION_RETURN);
        return new GiftRecordSummaryVo()
                .setGiveAmount(giveAmount)
                .setReceiveAmount(receiveAmount)
                .setReturnAmount(returnAmount)
                .setNetAmount(receiveAmount.subtract(giveAmount).subtract(returnAmount))
                .setRecordCount((long) records.size());
    }

    @Override
    public GiftRecordInfoVo queryGiftRecordInfo(Long id) {
        GiftRecordInfo entity = getById(id);
        giftDataScopeSupport.assertRecordAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftRecordInfoVo addGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo) {
        autoResolveOrCreateEvent(giftRecordInfoVo);
        validateForSave(giftRecordInfoVo);
        fillOwner(giftRecordInfoVo);
        GiftRecordInfo entity = new GiftRecordInfo();
        BeanUtils.copyProperties(giftRecordInfoVo, entity);
        if (DIRECTION_RECEIVE.equals(entity.getDirection()) && entity.getReturnedFlag() == null) {
            entity.setReturnedFlag(0);
            giftRecordInfoVo.setReturnedFlag(0);
        }
        save(entity);
        giftRecordInfoVo.setId(entity.getId());
        return giftRecordInfoVo;
    }

    @Override
    public Boolean updateGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo) {
        if (giftRecordInfoVo == null || giftRecordInfoVo.getId() == null) {
            throw GiftExceptions.param("礼金记录ID不能为空");
        }
        GiftRecordInfo existing = getById(giftRecordInfoVo.getId());
        giftDataScopeSupport.assertRecordAccessible(existing);
        giftRecordInfoVo.setUserId(existing.getUserId());
        giftRecordInfoVo.setOrgId(existing.getOrgId());
        autoResolveOrCreateEvent(giftRecordInfoVo);
        validateForSave(giftRecordInfoVo);
        GiftRecordInfo entity = new GiftRecordInfo();
        BeanUtils.copyProperties(giftRecordInfoVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRecordInfo(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            GiftRecordInfo existing = getById(id);
            giftDataScopeSupport.assertRecordAccessible(existing);
        }
        return removeBatchByIds(idList);
    }

    @Override
    public BigDecimal calculatePendingReturnAmount(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw GiftExceptions.param("原始收礼记录不能为空");
        }
        GiftRecordInfo receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null) {
            throw GiftExceptions.param("礼金记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(receiveRecord);
        if (!DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            throw GiftExceptions.param("原始记录必须是收礼记录");
        }
        BigDecimal receiveAmount = receiveRecord.getAmount() == null ? BigDecimal.ZERO : receiveRecord.getAmount();
        BigDecimal returnedAmount = getBaseMapper().sumReturnAmountByRelatedRecordId(receiveRecordId);
        BigDecimal pendingAmount = receiveAmount.subtract(returnedAmount == null ? BigDecimal.ZERO : returnedAmount);
        return pendingAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pendingAmount;
    }

    @Override
    public Boolean markReturned(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw GiftExceptions.param("原始收礼记录不能为空");
        }
        GiftRecordInfo receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null) {
            throw GiftExceptions.param("礼金记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(receiveRecord);
        if (!DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            throw GiftExceptions.param("仅收礼记录可标记已回礼");
        }
        if (receiveRecord.getReturnedFlag() != null && receiveRecord.getReturnedFlag() == 1) {
            return true;
        }
        GiftRecordInfo entity = new GiftRecordInfo();
        entity.setId(receiveRecordId);
        entity.setReturnedFlag(1);
        return updateById(entity);
    }

    @Override
    public void exportGiftRecordInfo(GiftRecordQuery query, HttpServletResponse response) {
        List<GiftRecordInfoVo> list = getList(query);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("礼金记录");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "日期", "事由", "往来对象", "类型", "金额", "状态" };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (GiftRecordInfoVo vo : list) {
                Row row = sheet.createRow(rowNum++);
                // Date (日期)
                String dateStr = "";
                if (vo.getPayTime() != null) {
                    dateStr = vo.getPayTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                row.createCell(0).setCellValue(dateStr);

                // EventName (事由)
                row.createCell(1).setCellValue(vo.getEventName() != null ? vo.getEventName() : "");

                // PersonName (往来对象)
                row.createCell(2).setCellValue(vo.getPersonName() != null ? vo.getPersonName() : "");

                // Direction (类型)
                String typeStr = "";
                if (vo.getDirection() != null) {
                    switch (vo.getDirection()) {
                        case "RECEIVE":
                            typeStr = "收礼";
                            break;
                        case "GIVE":
                            typeStr = "送礼";
                            break;
                        case "RETURN":
                            typeStr = "回礼";
                            break;
                        default:
                            typeStr = vo.getDirection();
                    }
                }
                row.createCell(3).setCellValue(typeStr);

                // Amount (金额)
                double amountDouble = vo.getAmount() != null ? vo.getAmount().doubleValue() : 0.0;
                row.createCell(4).setCellValue(amountDouble);

                // Status (状态)
                String statusStr;
                if ("RECEIVE".equals(vo.getDirection())) {
                    statusStr = (vo.getReturnedFlag() != null && vo.getReturnedFlag() == 1) ? "已回礼" : "未回礼";
                } else {
                    statusStr = "-";
                }
                row.createCell(5).setCellValue(statusStr);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "gift_record_info_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (java.io.IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    private void validateForSave(GiftRecordInfoVo vo) {
        validateDirection(vo);
        validateAmount(vo);
        validateReferences(vo);
        validateReturnRelation(vo);
    }

    private void validateDirection(GiftRecordInfoVo vo) {
        if (vo == null || !StringUtils.hasText(vo.getDirection())) {
            throw GiftExceptions.param("礼金方向不能为空");
        }
        if (!DIRECTION_GIVE.equals(vo.getDirection())
                && !DIRECTION_RECEIVE.equals(vo.getDirection())
                && !DIRECTION_RETURN.equals(vo.getDirection())) {
            throw GiftExceptions.param("礼金方向不合法");
        }
        if (DIRECTION_RETURN.equals(vo.getDirection()) && vo.getRelatedRecordId() == null) {
            throw GiftExceptions.param("回礼记录必须关联原始收礼记录");
        }
    }

    private void validateAmount(GiftRecordInfoVo vo) {
        if (vo == null || vo.getAmount() == null || vo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw GiftExceptions.param("礼金金额必须大于0");
        }
    }

    private void validateReferences(GiftRecordInfoVo vo) {
        if (vo == null) {
            return;
        }
        if (vo.getGiverPersonId() != null) {
            GiftPersonInfo giver = giftPersonInfoMapper.selectById(vo.getGiverPersonId());
            giftDataScopeSupport.assertPersonAccessible(giver);
        }
        if (vo.getReceiverPersonId() != null) {
            GiftPersonInfo receiver = giftPersonInfoMapper.selectById(vo.getReceiverPersonId());
            giftDataScopeSupport.assertPersonAccessible(receiver);
        }
        if (vo.getEventId() != null) {
            GiftEventInfo event = giftEventInfoMapper.selectById(vo.getEventId());
            giftDataScopeSupport.assertEventAccessible(event);
        }
    }

    private void validateReturnRelation(GiftRecordInfoVo vo) {
        if (vo == null || !DIRECTION_RETURN.equals(vo.getDirection())) {
            return;
        }
        Long relatedRecordId = vo.getRelatedRecordId();
        if (vo.getId() != null && vo.getId().equals(relatedRecordId)) {
            throw GiftExceptions.param("回礼记录不能关联自身");
        }
        GiftRecordInfo related = getById(relatedRecordId);
        if (related == null) {
            throw GiftExceptions.param("关联的收礼记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(related);
        if (!DIRECTION_RECEIVE.equals(related.getDirection())) {
            throw GiftExceptions.param("回礼记录只能关联收礼记录");
        }
    }

    private void fillOwner(GiftRecordInfoVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        vo.setUserId(loginUser.getId());
        OrgInfoVo orgInfoVo = loginUser.getOrgInfoVo();
        vo.setOrgId(orgInfoVo == null ? loginUser.getOrgId() : orgInfoVo.getId());
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(","))
                    .map(s -> s == null ? "" : s.trim())
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException ex) {
            throw GiftExceptions.param("礼金记录ID格式不合法");
        }
    }

    private GiftRecordInfoVo toVo(GiftRecordInfo entity) {
        if (entity == null) {
            return null;
        }
        GiftRecordInfoVo vo = new GiftRecordInfoVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setPaymentMethod("-");
        vo.setHandlerName("-");
        return vo;
    }

    private BigDecimal sumByDirection(List<GiftRecordInfo> records, String direction) {
        return records.stream()
                .filter(record -> record != null && direction.equals(record.getDirection()))
                .map(record -> record == null ? null : record.getAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void autoResolveOrCreateEvent(GiftRecordInfoVo vo) {
        if (vo.getEventId() != null) {
            return;
        }
        String eventType = vo.getEventType();
        Long eventOptionId = vo.getEventOptionId();

        if (!StringUtils.hasText(eventType) && eventOptionId == null) {
            return;
        }

        if (vo.getUserId() == null || vo.getOrgId() == null) {
            fillOwner(vo);
        }

        if (eventOptionId != null) {
            GiftEventTypeOption option = giftEventTypeOptionMapper.selectById(eventOptionId);
            if (option != null) {
                eventType = "SYSTEM".equals(option.getOptionType()) ? option.getEventCode() : option.getEventLabel();
                option.setUseCount((option.getUseCount() == null ? 0 : option.getUseCount()) + 1);
                giftEventTypeOptionMapper.updateById(option);
            }
        } else if (StringUtils.hasText(eventType)) {
            eventOptionId = giftEventTypeOptionMapper.findOptionIdByEventType(vo.getOrgId(), eventType.trim());
            if (eventOptionId != null) {
                GiftEventTypeOption option = giftEventTypeOptionMapper.selectById(eventOptionId);
                if (option != null) {
                    option.setUseCount((option.getUseCount() == null ? 0 : option.getUseCount()) + 1);
                    giftEventTypeOptionMapper.updateById(option);
                }
            }
        }

        if (!StringUtils.hasText(eventType)) {
            return;
        }

        Long contactPersonId = "GIVE".equals(vo.getDirection()) ? vo.getReceiverPersonId() : vo.getGiverPersonId();
        if (contactPersonId == null) {
            return;
        }

        GiftPersonInfo contactPerson = giftPersonInfoMapper.selectById(contactPersonId);
        if (contactPerson == null) {
            return;
        }
        String personName = contactPerson.getPersonName();

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GiftEventInfo> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("host_person_id", contactPersonId)
                .eq("event_type", eventType)
                .eq("is_delete", 0);
        List<GiftEventInfo> existingEvents = giftEventInfoMapper.selectList(query);

        if (existingEvents != null && !existingEvents.isEmpty()) {
            vo.setEventId(existingEvents.get(0).getId());
        } else {
            String eventLabel = eventType;
            if (eventOptionId != null) {
                GiftEventTypeOption option = giftEventTypeOptionMapper.selectById(eventOptionId);
                if (option != null) {
                    eventLabel = option.getEventLabel();
                }
            }
            String eventName = personName + eventLabel;

            GiftEventInfo newEvent = new GiftEventInfo()
                    .setOrgId(vo.getOrgId())
                    .setUserId(vo.getUserId())
                    .setEventName(eventName)
                    .setEventType(eventType)
                    .setEventTime(vo.getPayTime() != null ? vo.getPayTime() : LocalDateTime.now())
                    .setHostPersonId(contactPersonId)
                    .setRemark("人情关系智能助手自动生成的事由");

            giftEventInfoMapper.insert(newEvent);
            vo.setEventId(newEvent.getId());
        }
    }
}
