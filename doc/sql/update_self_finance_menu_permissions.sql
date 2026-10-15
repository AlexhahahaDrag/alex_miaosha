-- ==============================================================================
-- 个人财务 (selfFinance) 统一命名空间菜单与权限迁移脚本
-- 日期: 2026-10-14
-- 目标: 将个人财务（日常记账、零钱预算、多维分析、账户流水、消费卡）全链路收拢至 selfFinance 空间
-- ==============================================================================

USE `alex_user`;

-- 1. 确保一级菜单「个人财务」存在且路径为 /selfFinance
INSERT INTO `t_menu_info` (
  `id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `is_frame`, `parent_id`, `show_in_home`, `status`, `sort`, `hide_in_menu`, `permission_code`
) VALUES (
  1800000000000000001, 'selfFinance', '/selfFinance', '个人财务', 'Layout', '/selfFinance/financeManager', 'finance', '0', NULL, '1', '1', 35, '0', 'selfFinance'
) ON DUPLICATE KEY UPDATE
  `name` = 'selfFinance',
  `path` = '/selfFinance',
  `title` = '个人财务',
  `component` = 'Layout',
  `redirect` = '/selfFinance/financeManager',
  `permission_code` = 'selfFinance',
  `status` = '1';

-- 获取个人财务一级菜单 ID 变量
SET @self_finance_parent_id = 1800000000000000001;

-- 2. 更新或挂载个人财务二级子菜单 (全部指向 /src/views/selfFinance/ 路径)
-- (1) 财务信息 (日常收支记账)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeManager',
    `component` = '/src/views/selfFinance/financeManager/index.vue',
    `permission_code` = 'selfFinance:financeManager',
    `sort` = 10
WHERE `name` = 'financeManager';

-- (2) 零花钱预算管理 (独立动态子路由)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeManager/budgetManage',
    `component` = '/src/views/selfFinance/financeManager/budgetManage/index.vue',
    `permission_code` = 'selfFinance:financeManager:budget:view',
    `sort` = 15
WHERE `name` = 'financeBudgetManage';

-- (3) 财务多维分析
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeAnalysis',
    `component` = '/src/views/selfFinance/financeAnalysis/index.vue',
    `permission_code` = 'selfFinance:financeAnalysis',
    `sort` = 20
WHERE `name` = 'financeAnalysis';

-- (4) 账户流水信息 (原“猫超管理”)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `title` = '账户流水信息',
    `path` = '/selfFinance/accountRecordInfo',
    `component` = '/src/views/selfFinance/accountRecordInfo/index.vue',
    `permission_code` = 'selfFinance:accountRecordInfo',
    `order_by` = 80
WHERE `name` = 'accountRecordInfo';

-- (5) 消费卡信息 (储值卡总览与开卡)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/prepaidCardInfoT',
    `component` = '/src/views/selfFinance/prepaidCardInfoT/index.vue',
    `permission_code` = 'selfFinance:prepaidCardInfoT',
    `sort` = 40
WHERE `name` = 'prepaidCardInfoT';

-- (6) 消费卡交易记录 (充销流水明细)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/prepaidConsumeRecordT',
    `component` = '/src/views/selfFinance/prepaidConsumeRecordT/index.vue',
    `permission_code` = 'selfFinance:prepaidConsumeRecordT',
    `sort` = 50
WHERE `name` = 'prepaidConsumeRecordT';


-- 3. 补充/更新 t_permission_info 权限标识码
INSERT INTO `t_permission_info` (`id`, `permission_code`, `name`, `description`, `status`)
VALUES
(1800000000000000010, 'selfFinance', '个人财务', '个人财务一级菜单', '1'),
(1800000000000000011, 'selfFinance:financeManager', '财务信息', '个人/家庭收支记账列表与查看', '1'),
(1800000000000000012, 'selfFinance:financeManager:query', '零钱查询', '日常收支记录查询', '1'),
(1800000000000000013, 'selfFinance:financeManager:add', '零钱记账', '新增日常收支记录', '1'),
(1800000000000000014, 'selfFinance:financeManager:edit', '零钱修改', '修改日常收支记录', '1'),
(1800000000000000015, 'selfFinance:financeManager:delete', '零钱删除', '删除日常收支记录', '1'),
(1800000000000000016, 'selfFinance:financeManager:budget:view', '零钱预算查看', '查看零钱月度预算及试算数据', '1'),
(1800000000000000017, 'selfFinance:financeManager:budget:edit', '零钱预算修改', '保存与配置月度零钱预算额度与分类', '1'),
(1800000000000000018, 'selfFinance:financeAnalysis', '财务分析', '财务多维收支走势与资产看板分析', '1'),
(1800000000000000019, 'selfFinance:accountRecordInfo', '账户流水', '各资金账户流水明细查看与管理', '1'),
(1800000000000000020, 'selfFinance:prepaidCardInfoT', '消费卡信息', '储值卡/消费卡总览与开卡管理', '1'),
(1800000000000000021, 'selfFinance:prepaidConsumeRecordT', '消费卡交易记录', '储值卡充值与消费交易流水查看', '1')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `status` = '1';

-- 4. 为系统角色授予新的 selfFinance 权限集 (幂等插入)
-- (A) 普通成员与家庭成员 (family_member, user)
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101410000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code IN (
    'selfFinance',
    'selfFinance:financeManager',
    'selfFinance:financeManager:query',
    'selfFinance:financeManager:add',
    'selfFinance:financeManager:edit',
    'selfFinance:financeManager:delete',
    'selfFinance:financeManager:budget:view',
    'selfFinance:financeAnalysis',
    'selfFinance:accountRecordInfo',
    'selfFinance:prepaidCardInfoT',
    'selfFinance:prepaidConsumeRecordT'
)
  AND ri.role_code IN ('user', 'family_member')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );

-- (B) 管理员与超管 (family_admin, admin, super_super) -> 授予全量 selfFinance 权限 (含预算编辑)
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101420000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code LIKE 'selfFinance%'
  AND ri.role_code IN ('family_admin', 'admin', 'super_super')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );
