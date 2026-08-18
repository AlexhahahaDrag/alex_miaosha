# Graph Report - alex_miaosha_finance  (2026-07-13)

## Corpus Check
- 234 files · ~39,745 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1398 nodes · 1937 edges · 101 communities detected
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 293 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 74|Community 74]]
- [[_COMMUNITY_Community 75|Community 75]]
- [[_COMMUNITY_Community 76|Community 76]]
- [[_COMMUNITY_Community 77|Community 77]]
- [[_COMMUNITY_Community 78|Community 78]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 80|Community 80]]
- [[_COMMUNITY_Community 81|Community 81]]
- [[_COMMUNITY_Community 82|Community 82]]
- [[_COMMUNITY_Community 83|Community 83]]
- [[_COMMUNITY_Community 84|Community 84]]
- [[_COMMUNITY_Community 85|Community 85]]
- [[_COMMUNITY_Community 86|Community 86]]
- [[_COMMUNITY_Community 87|Community 87]]
- [[_COMMUNITY_Community 88|Community 88]]
- [[_COMMUNITY_Community 89|Community 89]]
- [[_COMMUNITY_Community 90|Community 90]]
- [[_COMMUNITY_Community 91|Community 91]]
- [[_COMMUNITY_Community 92|Community 92]]
- [[_COMMUNITY_Community 93|Community 93]]
- [[_COMMUNITY_Community 94|Community 94]]
- [[_COMMUNITY_Community 95|Community 95]]
- [[_COMMUNITY_Community 96|Community 96]]
- [[_COMMUNITY_Community 97|Community 97]]
- [[_COMMUNITY_Community 98|Community 98]]
- [[_COMMUNITY_Community 99|Community 99]]
- [[_COMMUNITY_Community 100|Community 100]]

## God Nodes (most connected - your core abstractions)
1. `GiftPersonInfoServiceImp` - 27 edges
2. `GiftRecordInfoServiceImp` - 23 edges
3. `GiftEventInfoServiceImp` - 19 edges
4. `GiftRecordInfoServiceIT` - 19 edges
5. `GiftStructureTest` - 17 edges
6. `CpnCouponInfoServiceImp` - 15 edges
7. `GiftDataScopeSupport` - 15 edges
8. `DictInfoServiceImp` - 14 edges
9. `GiftEventTypeOptionServiceImp` - 14 edges
10. `GiftRecordInfoService` - 14 edges

## Surprising Connections (you probably didn't know these)
- `GiftRecordQuery` --implements--> `Serializable`  [EXTRACTED]
  finance_api\src\main\java\com\alex\api\finance\gift\record\query\GiftRecordQuery.java →   _Bridges community 12 → community 0_
- `GiftEventInfoServiceImp` --implements--> `GiftEventInfoService`  [EXTRACTED]
  finance_boot\src\main\java\com\alex\finance\gift\event\service\impl\GiftEventInfoServiceImp.java →   _Bridges community 1 → community 0_
- `GiftEventTypeOptionServiceImp` --implements--> `GiftEventTypeOptionService`  [EXTRACTED]
  finance_boot\src\main\java\com\alex\finance\gift\eventoption\service\impl\GiftEventTypeOptionServiceImp.java →   _Bridges community 3 → community 0_
- `GiftPersonInfoServiceImp` --implements--> `GiftPersonInfoService`  [EXTRACTED]
  finance_boot\src\main\java\com\alex\finance\gift\person\service\impl\GiftPersonInfoServiceImp.java →   _Bridges community 9 → community 0_

## Communities

### Community 0 - "Community 0"
Cohesion: 0.02
Nodes (44): TestEventService, TestPersonService, TestRecordService, GiftDeleteStringIdTest, TestEventService, TestPersonService, TestRecordService, GiftEventBusinessVo (+36 more)

### Community 1 - "Community 1"
Cohesion: 0.04
Nodes (7): CodeUtils, GiftDataScopeSupport, GiftEventInfoController, GiftEventInfoServiceImp, GiftExceptions, GiftRecordInfoController, GiftRecordInfoServiceImp

### Community 2 - "Community 2"
Cohesion: 0.05
Nodes (13): PrepaidCardConsumeVo, PrepaidCardInfoT, PrepaidCardInfoTController, PrepaidCardInfoTMapper, PrepaidCardInfoTService, PrepaidCardInfoTServiceImp, PrepaidCardInfoTVo, PrepaidConsumeRecordT (+5 more)

### Community 3 - "Community 3"
Cohesion: 0.05
Nodes (7): GiftEventTypeOptionMapper, GiftEventTypeOptionServiceImp, GiftEventTypePresetSupport, GiftPersonInfoMapper, GiftPersonRelationOptionMapper, GiftPersonRelationOptionServiceImp, GiftRelationPresetSupport

### Community 4 - "Community 4"
Cohesion: 0.05
Nodes (15): AccountCountInfoVo, AccountRecordInfo, AccountRecordInfoController, AccountRecordInfoService, AccountRecordInfoServiceImp, AccountRecordInfoVo, AccountRecordNoticeJob, ShopFinanceAnalysisController (+7 more)

### Community 5 - "Community 5"
Cohesion: 0.06
Nodes (10): FinanceFallbackFactory, GiftAggregateBusinessRuleTest, GiftAmountTrendVo, GiftAnalysisController, GiftAnalysisService, GiftAnalysisServiceImpl, GiftRankingItemVo, GiftRecordExportControllerTest (+2 more)

### Community 6 - "Community 6"
Cohesion: 0.06
Nodes (11): CpnRedemptionRecordInfo, CpnRedemptionRecordInfoController, CpnRedemptionRecordInfoService, CpnRedemptionRecordInfoServiceImp, CpnRedemptionRecordInfoVo, CpnUserCouponInfo, CpnUserCouponInfoController, CpnUserCouponInfoService (+3 more)

### Community 7 - "Community 7"
Cohesion: 0.06
Nodes (10): ShopStock, ShopStockAttrs, ShopStockAttrsController, ShopStockAttrsService, ShopStockAttrsServiceImp, ShopStockAttrsVo, ShopStockController, ShopStockService (+2 more)

### Community 8 - "Community 8"
Cohesion: 0.06
Nodes (10): ShopOrder, ShopOrderController, ShopOrderDetail, ShopOrderDetailController, ShopOrderDetailService, ShopOrderDetailServiceImp, ShopOrderDetailVo, ShopOrderService (+2 more)

### Community 9 - "Community 9"
Cohesion: 0.09
Nodes (3): GiftDataScopeOrgSharedTest, GiftPersonInfoController, GiftPersonInfoServiceImp

### Community 10 - "Community 10"
Cohesion: 0.06
Nodes (10): GiftOwnershipTest, TestRelationService, GiftRelationInfo, GiftRelationInfoController, GiftRelationInfoMapper, GiftRelationInfoService, GiftRelationInfoService, GiftRelationInfoServiceImp (+2 more)

### Community 11 - "Community 11"
Cohesion: 0.1
Nodes (4): GiftRecordBusinessRuleTest, GiftRecordControllerIT, GiftRecordInfoServiceIT, GiftRecordInfoServicePageIT

### Community 12 - "Community 12"
Cohesion: 0.09
Nodes (14): CheckContactsVo, ContactsGiftRecordVo, GiftEventQuery, GiftPersonQuery, GiftRelationQuery, IExcelDataModel, IExcelModel, ImportFinanceInfoVo (+6 more)

### Community 13 - "Community 13"
Cohesion: 0.09
Nodes (7): DictInfo, DictInfoController, DictInfoService, DictInfoServiceImp, DictInfoVo, IExcelDictHandler, IExcelDictHandlerImpl

### Community 14 - "Community 14"
Cohesion: 0.1
Nodes (5): CpnCouponInfo, CpnCouponInfoController, CpnCouponInfoService, CpnCouponInfoServiceImp, CpnCouponInfoVo

### Community 15 - "Community 15"
Cohesion: 0.1
Nodes (6): ShopStockBatch, ShopStockBatchController, ShopStockBatchMapper, ShopStockBatchService, ShopStockBatchServiceImp, ShopStockBatchVo

### Community 16 - "Community 16"
Cohesion: 0.11
Nodes (5): FinanceInfo, FinanceInfoController, FinanceInfoService, FinanceInfoServiceImp, FinanceInfoVo

### Community 17 - "Community 17"
Cohesion: 0.12
Nodes (5): ShopCart, ShopCartController, ShopCartService, ShopCartServiceImp, ShopCartVo

### Community 18 - "Community 18"
Cohesion: 0.13
Nodes (5): AnalysisVo, BalanceVo, FinanceAnalysisController, FinanceAnalysisService, FinanceAnalysisServiceImpl

### Community 19 - "Community 19"
Cohesion: 0.13
Nodes (5): ShopFinance, ShopFinanceController, ShopFinanceService, ShopFinanceServiceImp, ShopFinanceVo

### Community 20 - "Community 20"
Cohesion: 0.2
Nodes (1): GiftStructureTest

### Community 21 - "Community 21"
Cohesion: 0.14
Nodes (5): ShopStockAmountVo, ShopStockAnalysisController, ShopStockAnalysisService, ShopStockAnalysisServiceImp, ShopStockAnalysisVo

### Community 22 - "Community 22"
Cohesion: 0.17
Nodes (1): ShopFinanceMapper

### Community 23 - "Community 23"
Cohesion: 0.26
Nodes (1): GiftRecordDataPermissionPageIT

### Community 24 - "Community 24"
Cohesion: 0.18
Nodes (1): DictInfoService

### Community 25 - "Community 25"
Cohesion: 0.2
Nodes (1): CpnCouponInfoService

### Community 26 - "Community 26"
Cohesion: 0.2
Nodes (1): CpnUserCouponInfoService

### Community 27 - "Community 27"
Cohesion: 0.2
Nodes (1): PrepaidCardInfoTService

### Community 28 - "Community 28"
Cohesion: 0.2
Nodes (1): PrepaidConsumeRecordTService

### Community 29 - "Community 29"
Cohesion: 0.22
Nodes (1): CpnUserCouponInfoApi

### Community 30 - "Community 30"
Cohesion: 0.22
Nodes (1): FinanceInfoService

### Community 31 - "Community 31"
Cohesion: 0.22
Nodes (1): ShopFinanceAnalysisService

### Community 32 - "Community 32"
Cohesion: 0.22
Nodes (1): ShopStockService

### Community 33 - "Community 33"
Cohesion: 0.25
Nodes (1): AccountRecordInfoService

### Community 34 - "Community 34"
Cohesion: 0.25
Nodes (1): CpnRedemptionRecordInfoService

### Community 35 - "Community 35"
Cohesion: 0.25
Nodes (1): ShopCartService

### Community 36 - "Community 36"
Cohesion: 0.25
Nodes (1): ShopOrderService

### Community 37 - "Community 37"
Cohesion: 0.25
Nodes (1): ShopOrderDetailService

### Community 38 - "Community 38"
Cohesion: 0.25
Nodes (1): ShopStockAttrsService

### Community 39 - "Community 39"
Cohesion: 0.25
Nodes (1): ShopStockBatchService

### Community 40 - "Community 40"
Cohesion: 0.29
Nodes (1): ShopFinanceApi

### Community 41 - "Community 41"
Cohesion: 0.29
Nodes (1): ShopStockApi

### Community 42 - "Community 42"
Cohesion: 0.29
Nodes (1): CpnCouponInfoApi

### Community 43 - "Community 43"
Cohesion: 0.29
Nodes (1): CpnRedemptionRecordInfoApi

### Community 44 - "Community 44"
Cohesion: 0.29
Nodes (1): PersonalGiftApi

### Community 45 - "Community 45"
Cohesion: 0.29
Nodes (1): PrepaidCardInfoTApi

### Community 46 - "Community 46"
Cohesion: 0.29
Nodes (1): PrepaidConsumeRecordTApi

### Community 47 - "Community 47"
Cohesion: 0.29
Nodes (1): ShopCartApi

### Community 48 - "Community 48"
Cohesion: 0.29
Nodes (1): ShopOrderApi

### Community 49 - "Community 49"
Cohesion: 0.29
Nodes (1): ShopOrderDetailApi

### Community 50 - "Community 50"
Cohesion: 0.29
Nodes (1): ShopStockAttrsApi

### Community 51 - "Community 51"
Cohesion: 0.29
Nodes (1): ShopStockBatchApi

### Community 52 - "Community 52"
Cohesion: 0.29
Nodes (1): GiftAnalysisService

### Community 53 - "Community 53"
Cohesion: 0.29
Nodes (1): PrepaidConsumeRecordTMapper

### Community 54 - "Community 54"
Cohesion: 0.29
Nodes (1): ShopFinanceService

### Community 55 - "Community 55"
Cohesion: 0.33
Nodes (2): DataPermissionOrgSharedFallbackIT, TestMapper

### Community 56 - "Community 56"
Cohesion: 0.33
Nodes (1): FinanceAnalysisMapper

### Community 57 - "Community 57"
Cohesion: 0.33
Nodes (1): FinanceAnalysisService

### Community 58 - "Community 58"
Cohesion: 0.33
Nodes (1): GiftPersonRelationOptionService

### Community 59 - "Community 59"
Cohesion: 0.4
Nodes (1): AccountRecordInfoMapper

### Community 60 - "Community 60"
Cohesion: 0.5
Nodes (1): PushConfigure

### Community 61 - "Community 61"
Cohesion: 0.4
Nodes (1): CpnCouponInfoMapper

### Community 62 - "Community 62"
Cohesion: 0.4
Nodes (1): CpnRedemptionRecordInfoMapper

### Community 63 - "Community 63"
Cohesion: 0.4
Nodes (1): CpnUserCouponInfoMapper

### Community 64 - "Community 64"
Cohesion: 0.4
Nodes (1): DictInfoMapper

### Community 65 - "Community 65"
Cohesion: 0.4
Nodes (1): FinanceInfoMapper

### Community 66 - "Community 66"
Cohesion: 0.4
Nodes (1): ShopCartMapper

### Community 67 - "Community 67"
Cohesion: 0.4
Nodes (1): ShopStockMapper

### Community 68 - "Community 68"
Cohesion: 0.4
Nodes (1): ShopStockAnalysisMapper

### Community 69 - "Community 69"
Cohesion: 0.4
Nodes (1): ShopStockAnalysisService

### Community 70 - "Community 70"
Cohesion: 0.4
Nodes (1): WeChatService

### Community 71 - "Community 71"
Cohesion: 0.5
Nodes (1): FinanceApplication

### Community 72 - "Community 72"
Cohesion: 0.5
Nodes (1): MybatisPlusConfig

### Community 73 - "Community 73"
Cohesion: 0.67
Nodes (1): SwaggerConfig

### Community 74 - "Community 74"
Cohesion: 0.67
Nodes (1): WebMvcConfigurer

### Community 75 - "Community 75"
Cohesion: 0.5
Nodes (1): ShopOrderMapper

### Community 76 - "Community 76"
Cohesion: 0.5
Nodes (1): ShopOrderDetailMapper

### Community 77 - "Community 77"
Cohesion: 0.5
Nodes (1): ShopStockAttrsMapper

### Community 78 - "Community 78"
Cohesion: 0.67
Nodes (1): FinanceApi

### Community 79 - "Community 79"
Cohesion: 0.67
Nodes (1): XxlJobConfig

### Community 80 - "Community 80"
Cohesion: 0.67
Nodes (1): GiftEventTypeOptionConstants

### Community 81 - "Community 81"
Cohesion: 0.67
Nodes (1): GiftRelationOptionConstants

### Community 82 - "Community 82"
Cohesion: 0.67
Nodes (1): UserVerifyHandler

### Community 83 - "Community 83"
Cohesion: 0.67
Nodes (1): RainbowUtil

### Community 84 - "Community 84"
Cohesion: 1.0
Nodes (1): ContactsUserImportVo

### Community 85 - "Community 85"
Cohesion: 1.0
Nodes (1): ContactsUserVo

### Community 86 - "Community 86"
Cohesion: 1.0
Nodes (1): ContactsUserRelationVo

### Community 87 - "Community 87"
Cohesion: 1.0
Nodes (1): UserRoleSetVo

### Community 88 - "Community 88"
Cohesion: 1.0
Nodes (1): CpnCouponInfoImportVo

### Community 89 - "Community 89"
Cohesion: 1.0
Nodes (1): GiftEventTypeItemVo

### Community 90 - "Community 90"
Cohesion: 1.0
Nodes (1): GiftEventTypeOptionRowVo

### Community 91 - "Community 91"
Cohesion: 1.0
Nodes (1): GiftPersonRelationItemVo

### Community 92 - "Community 92"
Cohesion: 1.0
Nodes (1): GiftPersonRelationOptionRowVo

### Community 93 - "Community 93"
Cohesion: 1.0
Nodes (1): GiftDashboardSummaryVo

### Community 94 - "Community 94"
Cohesion: 1.0
Nodes (1): PersonalGiftVo

### Community 95 - "Community 95"
Cohesion: 1.0
Nodes (1): ConsumptionTrendPointVo

### Community 96 - "Community 96"
Cohesion: 1.0
Nodes (1): CommonMapper

### Community 97 - "Community 97"
Cohesion: 1.0
Nodes (1): Weather

### Community 98 - "Community 98"
Cohesion: 1.0
Nodes (1): WechatAccountConfig

### Community 99 - "Community 99"
Cohesion: 1.0
Nodes (1): XxlProperties

### Community 100 - "Community 100"
Cohesion: 1.0
Nodes (1): WeChatController

## Knowledge Gaps
- **17 isolated node(s):** `ContactsUserImportVo`, `ContactsUserVo`, `ContactsUserRelationVo`, `UserRoleSetVo`, `CpnCouponInfoImportVo` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 20`** (17 nodes): `GiftStructureTest`, `.assertControllerRoutes()`, `.assertHasRequestBody()`, `.assertMapper()`, `.assertNoBaseEntityFields()`, `.assertNoPathVariable()`, `.assertRequestParam()`, `.findMethod()`, `.giftControllersExposeStitchAggregateRoutes()`, `.giftControllersFollowExistingFinanceRouteShape()`, `.giftEntitiesDoNotRedeclareBaseEntityFields()`, `.giftEntitiesExtendBaseEntity()`, `.giftMappersUseMybatisPlusAndOrgDataPermission()`, `.giftQueriesExposePageFilterFields()`, `.giftRecordVoUsesStableBusinessTypes()`, `.giftServicesUseMybatisPlusServiceStack()`, `.giftVosExtendBaseVo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 22`** (12 nodes): `ShopFinanceMapper.java`, `ShopFinanceMapper`, `.getAllShopStockInfo()`, `.getBenefitInfo()`, `.getChainAndYear()`, `.getCurShopFinanceInfo()`, `.getDayShopFinanceInfo()`, `.getMonthShopFinanceInfo()`, `.getPage()`, `.getPayWayInfo()`, `.getShopNameInfo()`, `.queryShopFinance()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 23`** (12 nodes): `GiftRecordDataPermissionPageIT.java`, `GiftRecordDataPermissionPageIT`, `.financeInfo_should_keep_user_owner_admin_subquery()`, `.getList_should_also_resolve_data_permission_annotation()`, `.noLoginUser_should_not_append_filter()`, `.normalUser_should_append_org_id_filter_on_gift_getPage()`, `.orgAdmin_should_append_org_id_filter_on_gift_getPage()`, `.setUp()`, `.superAdmin_should_not_append_data_permission_on_getPage()`, `.unannotated_mappedStatement_should_not_append_filter()`, `.user_without_org_should_degrade_to_user_scope_on_gift_getPage()`, `.userWithRole()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 24`** (11 nodes): `DictInfoService`, `.addDictInfo()`, `.deleteDictInfo()`, `.getPage()`, `.initDictRedis()`, `.listByBelong()`, `.queryDictInfo()`, `.queryDictInfoByTypeCode()`, `.queryDictInfoByTypeName()`, `.updateDictInfo()`, `DictInfoService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (10 nodes): `CpnCouponInfoService`, `.addCpnCouponInfo()`, `.deleteCpnCouponInfo()`, `.downloadTemplate()`, `.getList()`, `.getPage()`, `.importCpnCouponInfo()`, `.queryCpnCouponInfo()`, `.updateCpnCouponInfo()`, `CpnCouponInfoService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 26`** (10 nodes): `CpnUserCouponInfoService`, `.addCpnUserCouponInfo()`, `.cancelRedeem()`, `.deleteCpnUserCouponInfo()`, `.getList()`, `.getPage()`, `.queryCpnUserCouponInfo()`, `.redeem()`, `.updateCpnUserCouponInfo()`, `CpnUserCouponInfoService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (10 nodes): `PrepaidCardInfoTService.java`, `PrepaidCardInfoTService`, `.addPrepaidCardInfoT()`, `.consumeAndRecharge()`, `.dashboardOverview()`, `.deletePrepaidCardInfoT()`, `.getList()`, `.getPage()`, `.queryPrepaidCardInfoT()`, `.updatePrepaidCardInfoT()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (10 nodes): `PrepaidConsumeRecordTService.java`, `PrepaidConsumeRecordTService`, `.addPrepaidConsumeRecordT()`, `.aggregateTrendByDay()`, `.deletePrepaidConsumeRecordT()`, `.getList()`, `.getPage()`, `.queryPrepaidConsumeRecordT()`, `.sumExpenseAndRecharge()`, `.updatePrepaidConsumeRecordT()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (9 nodes): `CpnUserCouponInfoApi`, `.addCpnUserCouponInfo()`, `.cancelRedeem()`, `.deleteCpnUserCouponInfo()`, `.getCpnUserCouponInfoPage()`, `.queryCpnUserCouponInfo()`, `.redeem()`, `.updateCpnUserCouponInfo()`, `CpnUserCouponInfoApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 30`** (9 nodes): `FinanceInfoService.java`, `FinanceInfoService`, `.addFinanceInfo()`, `.deleteFinanceInfo()`, `.getList()`, `.getPage()`, `.importFinance()`, `.queryFinanceInfo()`, `.updateFinanceInfo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (9 nodes): `ShopFinanceAnalysisService.java`, `ShopFinanceAnalysisService`, `.getBenefitInfo()`, `.getChainAndYear()`, `.getCurShopFinanceInfo()`, `.getDayShopFinanceInfo()`, `.getMonthShopFinanceInfo()`, `.getPayWayInfo()`, `.getShopNameInfo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (9 nodes): `ShopStockService.java`, `ShopStockService`, `.addShopStock()`, `.deleteShopStock()`, `.getPage()`, `.getShopList()`, `.importShopStockInfo()`, `.queryShopStock()`, `.updateShopStock()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (8 nodes): `AccountRecordInfoService`, `.addAccountRecordInfo()`, `.deleteAccountRecordInfo()`, `.getPage()`, `.queryAccountRecordInfo()`, `.queryRemindRecordInfo()`, `.updateAccountRecordInfo()`, `AccountRecordInfoService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (8 nodes): `CpnRedemptionRecordInfoService`, `.addCpnRedemptionRecordInfo()`, `.deleteCpnRedemptionRecordInfo()`, `.getList()`, `.getPage()`, `.queryCpnRedemptionRecordInfo()`, `.updateCpnRedemptionRecordInfo()`, `CpnRedemptionRecordInfoService.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (8 nodes): `ShopCartService.java`, `ShopCartService`, `.addShopCart()`, `.deleteShopCart()`, `.getPage()`, `.list()`, `.queryShopCart()`, `.updateShopCart()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (8 nodes): `ShopOrderService.java`, `ShopOrderService`, `.addShopOrder()`, `.deleteShopOrder()`, `.getPage()`, `.queryShopOrder()`, `.submitOrder()`, `.updateShopOrder()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 37`** (8 nodes): `ShopOrderDetailService.java`, `ShopOrderDetailService`, `.addShopOrderDetail()`, `.batchUpdateShopOrderDetail()`, `.deleteShopOrderDetail()`, `.getPage()`, `.queryShopOrderDetail()`, `.updateShopOrderDetail()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 38`** (8 nodes): `ShopStockAttrsService.java`, `ShopStockAttrsService`, `.addShopStockAttrs()`, `.deleteShopStockAttrs()`, `.deleteShopStockAttrsByStockId()`, `.getPage()`, `.queryShopStockAttrs()`, `.updateShopStockAttrs()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 39`** (8 nodes): `ShopStockBatchService.java`, `ShopStockBatchService`, `.addShopStockBatch()`, `.deleteShopStockBatch()`, `.getList()`, `.getPage()`, `.queryShopStockBatch()`, `.updateShopStockBatch()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 40`** (7 nodes): `ShopFinanceApi.java`, `ShopFinanceApi`, `.addShopFinance()`, `.deleteShopFinance()`, `.getShopFinancePage()`, `.queryShopFinance()`, `.updateShopFinance()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 41`** (7 nodes): `ShopStockApi.java`, `ShopStockApi`, `.addShopStock()`, `.deleteShopStock()`, `.getShopStockPage()`, `.queryShopStock()`, `.updateShopStock()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 42`** (7 nodes): `CpnCouponInfoApi`, `.addCpnCouponInfo()`, `.deleteCpnCouponInfo()`, `.getCpnCouponInfoPage()`, `.queryCpnCouponInfo()`, `.updateCpnCouponInfo()`, `CpnCouponInfoApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 43`** (7 nodes): `CpnRedemptionRecordInfoApi`, `.addCpnRedemptionRecordInfo()`, `.deleteCpnRedemptionRecordInfo()`, `.getCpnRedemptionRecordInfoPage()`, `.queryCpnRedemptionRecordInfo()`, `.updateCpnRedemptionRecordInfo()`, `CpnRedemptionRecordInfoApi.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (7 nodes): `PersonalGiftApi.java`, `PersonalGiftApi`, `.addPersonalGift()`, `.deletePersonalGift()`, `.getPersonalGiftPage()`, `.queryPersonalGift()`, `.updatePersonalGift()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 45`** (7 nodes): `PrepaidCardInfoTApi.java`, `PrepaidCardInfoTApi`, `.addPrepaidCardInfoT()`, `.deletePrepaidCardInfoT()`, `.getPrepaidCardInfoTPage()`, `.queryPrepaidCardInfoT()`, `.updatePrepaidCardInfoT()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 46`** (7 nodes): `PrepaidConsumeRecordTApi.java`, `PrepaidConsumeRecordTApi`, `.addPrepaidConsumeRecordT()`, `.deletePrepaidConsumeRecordT()`, `.getPrepaidConsumeRecordTPage()`, `.queryPrepaidConsumeRecordT()`, `.updatePrepaidConsumeRecordT()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 47`** (7 nodes): `ShopCartApi.java`, `ShopCartApi`, `.addShopCart()`, `.deleteShopCart()`, `.getShopCartPage()`, `.queryShopCart()`, `.updateShopCart()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 48`** (7 nodes): `ShopOrderApi.java`, `ShopOrderApi`, `.addShopOrder()`, `.deleteShopOrder()`, `.getShopOrderPage()`, `.queryShopOrder()`, `.updateShopOrder()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 49`** (7 nodes): `ShopOrderDetailApi.java`, `ShopOrderDetailApi`, `.addShopOrderDetail()`, `.deleteShopOrderDetail()`, `.getShopOrderDetailPage()`, `.queryShopOrderDetail()`, `.updateShopOrderDetail()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 50`** (7 nodes): `ShopStockAttrsApi.java`, `ShopStockAttrsApi`, `.addShopStockAttrs()`, `.deleteShopStockAttrs()`, `.getShopStockAttrsPage()`, `.queryShopStockAttrs()`, `.updateShopStockAttrs()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 51`** (7 nodes): `ShopStockBatchApi.java`, `ShopStockBatchApi`, `.addShopStockBatch()`, `.deleteShopStockBatch()`, `.getShopStockBatchPage()`, `.queryShopStockBatch()`, `.updateShopStockBatch()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 52`** (7 nodes): `GiftAnalysisService.java`, `GiftAnalysisService`, `.eventRanking()`, `.overview()`, `.personRanking()`, `.relationDistribution()`, `.trend()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 53`** (7 nodes): `PrepaidConsumeRecordTMapper.java`, `PrepaidConsumeRecordTMapper`, `.aggregateTrendByDay()`, `.getList()`, `.getPage()`, `.queryPrepaidConsumeRecordT()`, `.sumExpenseAndRecharge()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 54`** (7 nodes): `ShopFinanceService.java`, `ShopFinanceService`, `.addShopFinance()`, `.deleteShopFinance()`, `.getPage()`, `.queryShopFinance()`, `.updateShopFinance()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 55`** (7 nodes): `DataPermissionOrgSharedFallbackIT`, `.orgShared_without_orgField_should_use_org_member_subquery()`, `.setUp()`, `.userWithRole()`, `TestMapper`, `.probe()`, `DataPermissionOrgSharedFallbackIT.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (6 nodes): `FinanceAnalysisMapper.java`, `FinanceAnalysisMapper`, `.getBalance()`, `.getDayExpense()`, `.getIncomeAndExpense()`, `.getMonthExpense()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (6 nodes): `FinanceAnalysisService.java`, `FinanceAnalysisService`, `.getBalance()`, `.getDayExpense()`, `.getIncomeAndExpense()`, `.getMonthExpense()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (6 nodes): `GiftPersonRelationOptionService.java`, `GiftPersonRelationOptionService`, `.findRelationOptionId()`, `.listRelationOptions()`, `.rememberCustomRelation()`, `.resolveRelationType()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (5 nodes): `AccountRecordInfoMapper`, `.getPage()`, `.queryAccountRecordInfo()`, `.queryRemindRecordInfo()`, `AccountRecordInfoMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 60`** (5 nodes): `PushConfigure.java`, `PushConfigure`, `.PushConfigure()`, `.wxMpConfigStorage()`, `.wxMpService()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 61`** (5 nodes): `CpnCouponInfoMapper`, `.getList()`, `.getPage()`, `.queryCpnCouponInfo()`, `CpnCouponInfoMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 62`** (5 nodes): `CpnRedemptionRecordInfoMapper`, `.getList()`, `.getPage()`, `.queryCpnRedemptionRecordInfo()`, `CpnRedemptionRecordInfoMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 63`** (5 nodes): `CpnUserCouponInfoMapper`, `.getList()`, `.getPage()`, `.queryCpnUserCouponInfo()`, `CpnUserCouponInfoMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 64`** (5 nodes): `DictInfoMapper`, `.getPage()`, `.listByBelong()`, `.queryDictInfo()`, `DictInfoMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 65`** (5 nodes): `FinanceInfoMapper.java`, `FinanceInfoMapper`, `.getList()`, `.getPage()`, `.queryFinanceInfo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 66`** (5 nodes): `ShopCartMapper.java`, `ShopCartMapper`, `.getPage()`, `.list()`, `.queryShopCart()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 67`** (5 nodes): `ShopStockMapper.java`, `ShopStockMapper`, `.getPage()`, `.getShopList()`, `.queryShopStock()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 68`** (5 nodes): `ShopStockAnalysisMapper.java`, `ShopStockAnalysisMapper`, `.getAllAmountInfo()`, `.getAllShopStockInfo()`, `.getCashAmountInfo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 69`** (5 nodes): `ShopStockAnalysisService.java`, `ShopStockAnalysisService`, `.getAllAmountInfo()`, `.getAllShopStockInfo()`, `.getCashAmountInfo()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 70`** (5 nodes): `WeChatService.java`, `WeChatService`, `.getToken()`, `.sentMessage()`, `.sentShopFinanceMessage()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 71`** (4 nodes): `FinanceApplication.java`, `FinanceApplication`, `.configurer()`, `.main()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 72`** (4 nodes): `MybatisPlusConfig.java`, `MybatisPlusConfig`, `.mybatisPlusInterceptor()`, `.performanceInterceptor()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 73`** (4 nodes): `SwaggerConfig.java`, `SwaggerConfig`, `.apiInfo()`, `.buildDocket()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 74`** (4 nodes): `WebMvcConfigurer.java`, `WebMvcConfigurer`, `.shouldRegisterLinksMapping()`, `.webEndpointServletHandlerMapping()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 75`** (4 nodes): `ShopOrderMapper.java`, `ShopOrderMapper`, `.getPage()`, `.queryShopOrder()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 76`** (4 nodes): `ShopOrderDetailMapper.java`, `ShopOrderDetailMapper`, `.getPage()`, `.queryShopOrderDetail()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 77`** (4 nodes): `ShopStockAttrsMapper.java`, `ShopStockAttrsMapper`, `.getPage()`, `.queryShopStockAttrs()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 78`** (3 nodes): `FinanceApi.java`, `FinanceApi`, `.getList()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 79`** (3 nodes): `XxlJobConfig.java`, `XxlJobConfig`, `.xxlJobExecutor()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 80`** (3 nodes): `GiftEventTypeOptionConstants.java`, `GiftEventTypeOptionConstants`, `.GiftEventTypeOptionConstants()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 81`** (3 nodes): `GiftRelationOptionConstants.java`, `GiftRelationOptionConstants`, `.GiftRelationOptionConstants()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 82`** (3 nodes): `UserVerifyHandler.java`, `UserVerifyHandler`, `.verifyHandler()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 83`** (3 nodes): `RainbowUtil.java`, `RainbowUtil`, `.getRainbow()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 84`** (2 nodes): `ContactsUserImportVo`, `ContactsUserImportVo.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 85`** (2 nodes): `ContactsUserVo`, `ContactsUserVo.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 86`** (2 nodes): `ContactsUserRelationVo`, `ContactsUserRelationVo.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 87`** (2 nodes): `UserRoleSetVo.java`, `UserRoleSetVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 88`** (2 nodes): `CpnCouponInfoImportVo`, `CpnCouponInfoImportVo.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 89`** (2 nodes): `GiftEventTypeItemVo.java`, `GiftEventTypeItemVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 90`** (2 nodes): `GiftEventTypeOptionRowVo.java`, `GiftEventTypeOptionRowVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 91`** (2 nodes): `GiftPersonRelationItemVo.java`, `GiftPersonRelationItemVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 92`** (2 nodes): `GiftPersonRelationOptionRowVo.java`, `GiftPersonRelationOptionRowVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 93`** (2 nodes): `GiftDashboardSummaryVo.java`, `GiftDashboardSummaryVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 94`** (2 nodes): `PersonalGiftVo.java`, `PersonalGiftVo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 95`** (2 nodes): `ConsumptionTrendPointVo`, `ConsumptionTrendPointVo.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 96`** (2 nodes): `CommonMapper`, `CommonMapper.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 97`** (2 nodes): `Weather.java`, `Weather`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 98`** (2 nodes): `WechatAccountConfig.java`, `WechatAccountConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 99`** (2 nodes): `XxlProperties.java`, `XxlProperties`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 100`** (2 nodes): `WeChatController.java`, `WeChatController`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GiftExceptions` connect `Community 1` to `Community 0`, `Community 10`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **What connects `ContactsUserImportVo`, `ContactsUserVo`, `ContactsUserRelationVo` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.02 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._