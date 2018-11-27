ALTER TABLE `rolegroup_accounts`
DROP FOREIGN KEY `FK_account_rolegroup_id`;
ALTER TABLE `rolegroup_accounts`
ADD CONSTRAINT `FK_account_rolegroup_id`
  FOREIGN KEY (`account_id`)
  REFERENCES `account` (`id`)
  ON DELETE CASCADE;
