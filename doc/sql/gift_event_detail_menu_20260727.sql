-- =============================================================================
-- gift_event_detail_menu_20260727.sql
-- Mobile 事由详情隐藏菜单（修复新增/编辑 404）
-- 目标库：alex_user
-- =============================================================================

USE alex_user;

SET NAMES utf8mb4;

INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
SELECT
  1900000000000001013,
  'giftEventDetail',
  '/finance/gift/event/giftEventDetail',
  '事由管理详情',
  '/src/views/finance/gift/event/giftEventDetail/index.vue',
  NULL,
  'giftEventDetail',
  '1',
  (SELECT `id` FROM `t_menu_info` WHERE `name` = 'gift' LIMIT 1),
  '礼尚往来事由管理详情',
  '1',
  NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL,
  31,
  '0',
  'gift:event'
WHERE NOT EXISTS (
  SELECT 1 FROM `t_menu_info` WHERE `name` = 'giftEventDetail'
);
