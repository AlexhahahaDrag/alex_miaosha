-- gift_person_avatar_20260727.sql
ALTER TABLE `alex_finance`.`gift_person_info_t`
  ADD COLUMN `avatar` bigint NULL COMMENT '头像 OSS 文件ID' AFTER `phone`;
