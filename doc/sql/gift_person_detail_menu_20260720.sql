USE alex_user;

SET NAMES utf8mb4;

-- giftPersonDetail: hide_in_menu=1, parent=礼尚往来顶级菜单
INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
SELECT
  1900000000000001012,
  'giftPersonDetail',
  '/finance/gift/person/giftPersonDetail',
  '亲友管理详情',
  '/src/views/finance/gift/person/giftPersonDetail/index.vue',
  NULL,
  'giftPersonDetail',
  '1',
  1900000000000001000,
  '礼尚往来亲友管理详情',
  '1',
  NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL,
  21,
  '0',
  'gift:person'
WHERE NOT EXISTS (
  SELECT 1 FROM `t_menu_info` WHERE `name` = 'giftPersonDetail'
);
