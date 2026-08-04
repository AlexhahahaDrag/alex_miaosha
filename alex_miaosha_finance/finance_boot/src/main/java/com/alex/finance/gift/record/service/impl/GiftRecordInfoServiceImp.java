package com.alex.finance.gift.record.service.impl;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftDirectionAggVo;
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
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 汇总统计：下沉为 SQL GROUP BY direction 聚合（sumDirectionAgg），
     * 不再全量拉取记录在内存中累加，数据量增长时不劣化。
     */
    @Override
    public GiftRecordSummaryVo getSummary(GiftRecordQuery query) {
        List<GiftDirectionAggVo> aggRows = getBaseMapper().sumDirectionAgg(query);
        BigDecimal giveAmount = aggAmount(aggRows, DIRECTION_GIVE);
        BigDecimal receiveAmount = aggAmount(aggRows, DIRECTION_RECEIVE);
        BigDecimal returnAmount = aggAmount(aggRows, DIRECTION_RETURN);
        long receiveCount = aggCount(aggRows, DIRECTION_RECEIVE);
        long giveCount = aggCount(aggRows, DIRECTION_GIVE);
        long returnCount = aggCount(aggRows, DIRECTION_RETURN);
        return new GiftRecordSummaryVo()
                .setGiveAmount(giveAmount)
                .setReceiveAmount(receiveAmount)
                .setReturnAmount(returnAmount)
                .setNetAmount(receiveAmount.subtract(giveAmount).subtract(returnAmount))
                .setRecordCount(receiveCount + giveCount + returnCount)
                .setReceiveCount(receiveCount)
                .setGiveCount(giveCount)
                .setReturnCount(returnCount);
    }

    @Override
    public List<GiftAmountTrendVo> getTrend(String period, String direction) {
        // 月度 %Y-%m / 年度 %Y，与原 YearMonth 内存分组口径一致
        String dateFormat = "year".equalsIgnoreCase(period) ? "%Y" : "%Y-%m";
        return getBaseMapper().sumTrendAgg(dateFormat, normalizeDirection(direction));
    }

    @Override
    public GiftRecordInfoVo queryGiftRecordInfo(Long id) {
        GiftRecordInfo entity = getById(id);
        giftDataScopeSupport.assertRecordAccessible(entity);
        return toVo(entity);
    }

    /**
     * 新增礼金记录。
     * <p>
     * 事务边界：autoResolveOrCreateEvent 可能插入事由并更新词典 useCount，
     * 必须与记录落库保持原子；RETURN 记录落库后同步回礼状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        if (DIRECTION_RETURN.equals(entity.getDirection())) {
            syncReturnedFlag(entity.getRelatedRecordId());
        }
        return giftRecordInfoVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        Boolean updated = updateById(entity);
        // 回礼金额可能变化：原关联与新关联的收礼记录都要重算回礼状态
        if (DIRECTION_RETURN.equals(existing.getDirection())) {
            syncReturnedFlag(existing.getRelatedRecordId());
        }
        if (DIRECTION_RETURN.equals(giftRecordInfoVo.getDirection())) {
            syncReturnedFlag(giftRecordInfoVo.getRelatedRecordId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteGiftRecordInfo(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        // 记录被删 RETURN 关联的收礼ID，删除后重算其回礼状态
        List<Long> relatedReceiveIds = new java.util.ArrayList<>();
        for (Long id : idList) {
            GiftRecordInfo existing = getById(id);
            giftDataScopeSupport.assertRecordAccessible(existing);
            if (existing != null && DIRECTION_RETURN.equals(existing.getDirection())
                    && existing.getRelatedRecordId() != null) {
                relatedReceiveIds.add(existing.getRelatedRecordId());
            }
        }
        Boolean removed = removeBatchByIds(idList);
        relatedReceiveIds.stream().distinct().forEach(this::syncReturnedFlag);
        return removed;
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
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 按回礼流水重算收礼记录的 returned_flag：
     * 累计回礼金额 >= 收礼金额 时置 1，否则置 0（覆盖手动误标场景）。
     * 在 RETURN 记录新增/修改/删除后调用，保证状态与流水口径一致。
     */
    private void syncReturnedFlag(Long receiveRecordId) {
        if (receiveRecordId == null) {
            return;
        }
        GiftRecordInfo receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null || !DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            return;
        }
        BigDecimal receiveAmount = receiveRecord.getAmount() == null ? BigDecimal.ZERO : receiveRecord.getAmount();
        BigDecimal returnedAmount = getBaseMapper().sumReturnAmountByRelatedRecordId(receiveRecordId);
        int expectedFlag = receiveAmount.subtract(returnedAmount == null ? BigDecimal.ZERO : returnedAmount)
                .compareTo(BigDecimal.ZERO) <= 0 ? 1 : 0;
        if (receiveRecord.getReturnedFlag() == null || receiveRecord.getReturnedFlag() != expectedFlag) {
            GiftRecordInfo entity = new GiftRecordInfo();
            entity.setId(receiveRecordId);
            entity.setReturnedFlag(expectedFlag);
            updateById(entity);
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

    /** 从方向聚合行中取指定方向金额（无该方向记录时返回 0） */
    private BigDecimal aggAmount(List<GiftDirectionAggVo> aggRows, String direction) {
        return aggRows.stream()
                .filter(row -> row != null && direction.equals(row.getDirection()))
                .map(GiftDirectionAggVo::getTotalAmount)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    /** 从方向聚合行中取指定方向笔数 */
    private long aggCount(List<GiftDirectionAggVo> aggRows, String direction) {
        return aggRows.stream()
                .filter(row -> row != null && direction.equals(row.getDirection()))
                .map(GiftDirectionAggVo::getRecordCount)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(0L);
    }

    /** 方向参数白名单校验：非法值一律按"全部"处理，防止外部输入直接进 SQL 条件 */
    private String normalizeDirection(String direction) {
        if (DIRECTION_GIVE.equals(direction)
                || DIRECTION_RECEIVE.equals(direction)
                || DIRECTION_RETURN.equals(direction)) {
            return direction;
        }
        return null;
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
