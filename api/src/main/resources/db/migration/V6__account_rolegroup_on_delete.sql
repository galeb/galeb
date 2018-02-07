ALTER TABLE `galeb_api`.`rolegroup_accounts`
DROP FOREIGN KEY `FK_account_rolegroup_id`;
ALTER TABLE `galeb_api`.`rolegroup_accounts`
ADD CONSTRAINT `FK_account_rolegroup_id`
  FOREIGN KEY (`account_id`)
  REFERENCES `galeb_api`.`account` (`id`)
  ON DELETE CASCADE;
