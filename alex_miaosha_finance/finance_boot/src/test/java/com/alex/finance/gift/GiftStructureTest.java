package com.alex.finance.gift;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.finance.gift.analysis.controller.GiftAnalysisController;
import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.common.common.BaseEntity;
import com.alex.common.common.BaseVo;
import com.alex.finance.gift.event.controller.GiftEventInfoController;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.event.service.impl.GiftEventInfoServiceImp;
import com.alex.finance.gift.person.controller.GiftPersonInfoController;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.record.controller.GiftRecordInfoController;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GiftStructureTest {

    private static final Set<String> BASE_ENTITY_FIELDS = Set.of(
            "id",
            "creator",
            "createTime",
            "updater",
            "updateTime",
            "operator",
            "operateTime",
            "deleter",
            "deleteTime",
            "isDelete"
    );

    @Test
    void giftEntitiesExtendBaseEntity() {
        assertTrue(BaseEntity.class.isAssignableFrom(GiftPersonInfo.class));
        assertTrue(BaseEntity.class.isAssignableFrom(GiftEventInfo.class));
        assertTrue(BaseEntity.class.isAssignableFrom(GiftRecordInfo.class));
    }

    @Test
    void giftEntitiesDoNotRedeclareBaseEntityFields() {
        assertNoBaseEntityFields(GiftPersonInfo.class);
        assertNoBaseEntityFields(GiftEventInfo.class);
        assertNoBaseEntityFields(GiftRecordInfo.class);
    }

    @Test
    void giftVosExtendBaseVo() {
        assertTrue(BaseVo.class.isAssignableFrom(GiftPersonInfoVo.class));
        assertTrue(BaseVo.class.isAssignableFrom(GiftEventInfoVo.class));
        assertTrue(BaseVo.class.isAssignableFrom(GiftRecordInfoVo.class));
    }

    @Test
    void giftRecordVoUsesStableBusinessTypes() throws NoSuchFieldException {
        assertEquals(Long.class, GiftRecordInfoVo.class.getDeclaredField("orgId").getType());
        assertEquals(Long.class, GiftRecordInfoVo.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, GiftRecordInfoVo.class.getDeclaredField("eventId").getType());
        assertEquals(Long.class, GiftRecordInfoVo.class.getDeclaredField("relatedRecordId").getType());
        assertEquals(String.class, GiftRecordInfoVo.class.getDeclaredField("direction").getType());
        assertEquals(BigDecimal.class, GiftRecordInfoVo.class.getDeclaredField("amount").getType());
        assertEquals(LocalDateTime.class, GiftRecordInfoVo.class.getDeclaredField("payTime").getType());
        assertEquals(Integer.class, GiftRecordInfoVo.class.getDeclaredField("returnedFlag").getType());
    }

    @Test
    void giftQueriesExposePageFilterFields() throws NoSuchFieldException {
        assertEquals(String.class, GiftPersonQuery.class.getDeclaredField("keyword").getType());
        assertEquals(String.class, GiftEventQuery.class.getDeclaredField("keyword").getType());
        assertEquals(LocalDateTime.class, GiftRecordQuery.class.getDeclaredField("payTimeStart").getType());
        assertEquals(LocalDateTime.class, GiftRecordQuery.class.getDeclaredField("payTimeEnd").getType());
        assertEquals(BigDecimal.class, GiftRecordQuery.class.getDeclaredField("amountMin").getType());
        assertEquals(BigDecimal.class, GiftRecordQuery.class.getDeclaredField("amountMax").getType());
        assertEquals(String.class, GiftRecordQuery.class.getDeclaredField("direction").getType());
    }

    @Test
    void giftMappersUseMybatisPlusAndOrgDataPermission() throws NoSuchMethodException {
        assertMapper(GiftPersonInfoMapper.class, "gift_person_info_t");
        assertMapper(GiftEventInfoMapper.class, "gift_event_info_t");
        assertMapper(GiftRecordInfoMapper.class, "gift_record_info_t");
    }

    @Test
    void giftServicesUseMybatisPlusServiceStack() {
        assertTrue(IService.class.isAssignableFrom(GiftPersonInfoService.class));
        assertTrue(IService.class.isAssignableFrom(GiftEventInfoService.class));
        assertTrue(IService.class.isAssignableFrom(GiftRecordInfoService.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftPersonInfoServiceImp.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftEventInfoServiceImp.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftRecordInfoServiceImp.class));
    }

    @Test
    void giftControllersFollowExistingFinanceRouteShape() throws NoSuchMethodException {
        assertControllerRoutes(GiftPersonInfoController.class, "${api.version:/api/v1}/gift-person-info-t");
        assertControllerRoutes(GiftEventInfoController.class, "${api.version:/api/v1}/gift-event-info-t");
        assertControllerRoutes(GiftRecordInfoController.class, "${api.version:/api/v1}/gift-record-info-t");

        Method pendingReturnAmount = findMethod(GiftRecordInfoController.class, "pendingReturnAmount");
        assertTrue(pendingReturnAmount.isAnnotationPresent(GetMapping.class));
        assertEquals("/pending-return-amount", pendingReturnAmount.getAnnotation(GetMapping.class).value()[0]);
        assertRequestParam(pendingReturnAmount, "receiveRecordId");

        Method markReturned = findMethod(GiftRecordInfoController.class, "markReturned");
        assertTrue(markReturned.isAnnotationPresent(PutMapping.class));
        assertEquals("/mark-returned", markReturned.getAnnotation(PutMapping.class).value()[0]);
        assertRequestParam(markReturned, "receiveRecordId");
    }

    @Test
    void giftControllersExposeStitchAggregateRoutes() throws NoSuchMethodException {
        Method personSummary = findMethod(GiftPersonInfoController.class, "summary");
        assertEquals("/summary", personSummary.getAnnotation(GetMapping.class).value()[0]);

        Method personBusinessPage = findMethod(GiftPersonInfoController.class, "businessPage");
        assertEquals("/business-page", personBusinessPage.getAnnotation(PostMapping.class).value()[0]);

        Method personProfile = findMethod(GiftPersonInfoController.class, "profile");
        assertEquals("/profile", personProfile.getAnnotation(GetMapping.class).value()[0]);
        assertRequestParam(personProfile, "id");

        Method eventSummary = findMethod(GiftEventInfoController.class, "summary");
        assertEquals("/summary", eventSummary.getAnnotation(GetMapping.class).value()[0]);

        Method eventBusinessPage = findMethod(GiftEventInfoController.class, "businessPage");
        assertEquals("/business-page", eventBusinessPage.getAnnotation(PostMapping.class).value()[0]);

        Method recordSummary = findMethod(GiftRecordInfoController.class, "summary");
        assertEquals("/summary", recordSummary.getAnnotation(PostMapping.class).value()[0]);

        assertTrue(GiftAnalysisController.class.isAnnotationPresent(RestController.class));
        assertEquals("${api.version:/api/v1}/gift-analysis", GiftAnalysisController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/overview", findMethod(GiftAnalysisController.class, "overview").getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/trend", findMethod(GiftAnalysisController.class, "trend").getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/relation-distribution", findMethod(GiftAnalysisController.class, "relationDistribution").getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/event-ranking", findMethod(GiftAnalysisController.class, "eventRanking").getAnnotation(GetMapping.class).value()[0]);
        assertEquals("/person-ranking", findMethod(GiftAnalysisController.class, "personRanking").getAnnotation(GetMapping.class).value()[0]);
    }

    private void assertNoBaseEntityFields(Class<?> entityClass) {
        Set<String> declaredFields = Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> field.getName())
                .collect(Collectors.toSet());

        for (String field : BASE_ENTITY_FIELDS) {
            assertFalse(declaredFields.contains(field), entityClass.getSimpleName() + " redeclares " + field);
        }
    }

    private void assertMapper(Class<?> mapperClass, String tableName) throws NoSuchMethodException {
        assertTrue(BaseMapper.class.isAssignableFrom(mapperClass));
        assertTrue(mapperClass.isAnnotationPresent(Mapper.class));

        Method getPage = Arrays.stream(mapperClass.getDeclaredMethods())
                .filter(method -> "getPage".equals(method.getName()))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        DataPermission dataPermission = getPage.getAnnotation(DataPermission.class);
        assertEquals(tableName, dataPermission.table());
        assertEquals("user_id", dataPermission.field());
        assertEquals("org_id", dataPermission.orgField());
        assertEquals(DataPermissionScope.ORG_SHARED, dataPermission.scope());
    }

    private void assertControllerRoutes(Class<?> controllerClass, String route) throws NoSuchMethodException {
        assertTrue(controllerClass.isAnnotationPresent(RestController.class));
        assertEquals(route, controllerClass.getAnnotation(RequestMapping.class).value()[0]);

        Method getPage = findMethod(controllerClass, "getPage");
        assertEquals("/page", getPage.getAnnotation(PostMapping.class).value()[0]);

        Method query = findMethod(controllerClass, "query");
        assertTrue(query.isAnnotationPresent(GetMapping.class));
        assertRequestParam(query, "id");
        assertNoPathVariable(query);

        Method add = findMethod(controllerClass, "add");
        assertTrue(add.isAnnotationPresent(PostMapping.class));
        assertHasRequestBody(add);

        Method update = findMethod(controllerClass, "update");
        assertTrue(update.isAnnotationPresent(PutMapping.class));
        assertHasRequestBody(update);
        assertNoPathVariable(update);

        Method delete = findMethod(controllerClass, "delete");
        assertTrue(delete.isAnnotationPresent(DeleteMapping.class));
        assertRequestParam(delete, "ids");
        assertNoPathVariable(delete);
    }

    private Method findMethod(Class<?> controllerClass, String name) throws NoSuchMethodException {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> name.equals(method.getName()))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
    }

    private void assertHasRequestBody(Method method) {
        assertTrue(Arrays.stream(method.getParameters())
                .anyMatch(parameter -> parameter.isAnnotationPresent(RequestBody.class)));
    }

    private void assertRequestParam(Method method, String name) {
        assertTrue(Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .anyMatch(annotation -> annotation != null && name.equals(annotation.value())));
    }

    private void assertNoPathVariable(Method method) {
        for (Parameter parameter : method.getParameters()) {
            assertFalse(parameter.isAnnotationPresent(PathVariable.class), method.getName() + " uses path variable");
        }
    }
}
