package com.alex.finance.gift;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.common.common.BaseEntity;
import com.alex.common.common.BaseVo;
import com.alex.finance.gift.event.controller.GiftEventInfoTController;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
import com.alex.finance.gift.event.service.impl.GiftEventInfoTServiceImp;
import com.alex.finance.gift.person.controller.GiftPersonInfoTController;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoTServiceImp;
import com.alex.finance.gift.record.controller.GiftRecordInfoTController;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.alex.finance.gift.relation.mapper.GiftRelationInfoTMapper;
import com.alex.finance.gift.relation.service.GiftRelationInfoTService;
import com.alex.finance.gift.relation.service.impl.GiftRelationInfoTServiceImp;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoTServiceImp;
import com.alex.finance.gift.relation.controller.GiftRelationInfoTController;
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
        assertTrue(BaseEntity.class.isAssignableFrom(GiftPersonInfoT.class));
        assertTrue(BaseEntity.class.isAssignableFrom(GiftRelationInfoT.class));
        assertTrue(BaseEntity.class.isAssignableFrom(GiftEventInfoT.class));
        assertTrue(BaseEntity.class.isAssignableFrom(GiftRecordInfoT.class));
    }

    @Test
    void giftEntitiesDoNotRedeclareBaseEntityFields() {
        assertNoBaseEntityFields(GiftPersonInfoT.class);
        assertNoBaseEntityFields(GiftRelationInfoT.class);
        assertNoBaseEntityFields(GiftEventInfoT.class);
        assertNoBaseEntityFields(GiftRecordInfoT.class);
    }

    @Test
    void giftVosExtendBaseVo() {
        assertTrue(BaseVo.class.isAssignableFrom(GiftPersonInfoTVo.class));
        assertTrue(BaseVo.class.isAssignableFrom(GiftRelationInfoTVo.class));
        assertTrue(BaseVo.class.isAssignableFrom(GiftEventInfoTVo.class));
        assertTrue(BaseVo.class.isAssignableFrom(GiftRecordInfoTVo.class));
    }

    @Test
    void giftRecordVoUsesStableBusinessTypes() throws NoSuchFieldException {
        assertEquals(Long.class, GiftRecordInfoTVo.class.getDeclaredField("orgId").getType());
        assertEquals(Long.class, GiftRecordInfoTVo.class.getDeclaredField("userId").getType());
        assertEquals(Long.class, GiftRecordInfoTVo.class.getDeclaredField("eventId").getType());
        assertEquals(Long.class, GiftRecordInfoTVo.class.getDeclaredField("relatedRecordId").getType());
        assertEquals(String.class, GiftRecordInfoTVo.class.getDeclaredField("direction").getType());
        assertEquals(BigDecimal.class, GiftRecordInfoTVo.class.getDeclaredField("amount").getType());
        assertEquals(LocalDateTime.class, GiftRecordInfoTVo.class.getDeclaredField("payTime").getType());
        assertEquals(Integer.class, GiftRecordInfoTVo.class.getDeclaredField("returnedFlag").getType());
    }

    @Test
    void giftQueriesExposePageFilterFields() throws NoSuchFieldException {
        assertEquals(String.class, GiftPersonQuery.class.getDeclaredField("keyword").getType());
        assertEquals(String.class, GiftRelationQuery.class.getDeclaredField("relationType").getType());
        assertEquals(String.class, GiftEventQuery.class.getDeclaredField("keyword").getType());
        assertEquals(LocalDateTime.class, GiftRecordQuery.class.getDeclaredField("payTimeStart").getType());
        assertEquals(LocalDateTime.class, GiftRecordQuery.class.getDeclaredField("payTimeEnd").getType());
        assertEquals(BigDecimal.class, GiftRecordQuery.class.getDeclaredField("amountMin").getType());
        assertEquals(BigDecimal.class, GiftRecordQuery.class.getDeclaredField("amountMax").getType());
        assertEquals(String.class, GiftRecordQuery.class.getDeclaredField("direction").getType());
    }

    @Test
    void giftMappersUseMybatisPlusAndOrgDataPermission() throws NoSuchMethodException {
        assertMapper(GiftPersonInfoTMapper.class, "gift_person_info_t");
        assertMapper(GiftRelationInfoTMapper.class, "gift_relation_info_t");
        assertMapper(GiftEventInfoTMapper.class, "gift_event_info_t");
        assertMapper(GiftRecordInfoTMapper.class, "gift_record_info_t");
    }

    @Test
    void giftServicesUseMybatisPlusServiceStack() {
        assertTrue(IService.class.isAssignableFrom(GiftPersonInfoTService.class));
        assertTrue(IService.class.isAssignableFrom(GiftRelationInfoTService.class));
        assertTrue(IService.class.isAssignableFrom(GiftEventInfoTService.class));
        assertTrue(IService.class.isAssignableFrom(GiftRecordInfoTService.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftPersonInfoTServiceImp.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftRelationInfoTServiceImp.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftEventInfoTServiceImp.class));
        assertTrue(ServiceImpl.class.isAssignableFrom(GiftRecordInfoTServiceImp.class));
    }

    @Test
    void giftControllersFollowExistingFinanceRouteShape() throws NoSuchMethodException {
        assertControllerRoutes(GiftPersonInfoTController.class, "${api.version:/api/v1}/gift-person-info-t");
        assertControllerRoutes(GiftRelationInfoTController.class, "${api.version:/api/v1}/gift-relation-info-t");
        assertControllerRoutes(GiftEventInfoTController.class, "${api.version:/api/v1}/gift-event-info-t");
        assertControllerRoutes(GiftRecordInfoTController.class, "${api.version:/api/v1}/gift-record-info-t");

        Method pendingReturnAmount = findMethod(GiftRecordInfoTController.class, "pendingReturnAmount");
        assertTrue(pendingReturnAmount.isAnnotationPresent(GetMapping.class));
        assertEquals("/pending-return-amount", pendingReturnAmount.getAnnotation(GetMapping.class).value()[0]);
        assertRequestParam(pendingReturnAmount, "receiveRecordId");

        Method markReturned = findMethod(GiftRecordInfoTController.class, "markReturned");
        assertTrue(markReturned.isAnnotationPresent(PutMapping.class));
        assertEquals("/mark-returned", markReturned.getAnnotation(PutMapping.class).value()[0]);
        assertRequestParam(markReturned, "receiveRecordId");
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
        assertEquals("org_id", dataPermission.field());
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
