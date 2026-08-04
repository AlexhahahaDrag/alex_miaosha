# Graphify Project Notes

This backend repository is a multi-module Spring Boot + Maven project. Gift management lives in the finance service and reuses common infrastructure from the existing system.

Implications for code navigation and architecture analysis:

- Shared entity fields come from `com.alex.common.common.BaseEntity`; gift entities should not redeclare id, audit, operator, delete, or timestamp fields.
- Controllers expose DTO, Query, and VO boundaries. Entity classes are persistence objects and should not be treated as API response contracts.
- MyBatis Plus service classes use `IService`, `ServiceImpl`, `LambdaQueryWrapper`, `LambdaUpdateWrapper`, and `Page`.
- RBAC, user, and organization capabilities are reused from existing user/common modules.

Gift management module notes:

- Backend implementation lives under `alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/gift`.
- SQL initialization files live at `doc/sql/gift_management_schema.sql` and `doc/sql/gift_management_permission.sql`.
- Data tables are `gift_person_info_t`, `gift_event_info_t`, and `gift_record_info_t` (the former `gift_relation_info_t` was removed on 2026-08-04; relation semantics live in `gift_person_info_t.relation_type` plus the `gift_person_relation_option_t` dictionary).
- Return records are represented in `gift_record_info_t` with `direction = RETURN` and `related_record_id`; there is no separate `gift_return_record_info_t`.
- All business queries must include `org_id` data isolation, and user-scoped access can additionally use `user_id`.
- Important indexes include organization, event, giver, receiver, pay time, and direction combinations for 10w+ gift records.
- Backend test coverage for this module includes `GiftRecordBusinessRuleTest`, `GiftOwnershipTest`, and `GiftStructureTest`.
