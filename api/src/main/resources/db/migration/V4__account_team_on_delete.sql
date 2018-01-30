ALTER TABLE `galeb_api`.`team_accounts`
DROP FOREIGN KEY `FK_account_id`;
ALTER TABLE `galeb_api`.`team_accounts`
ADD CONSTRAINT `FK_account_id`
  FOREIGN KEY (`account_id`)
  REFERENCES `galeb_api`.`account` (`id`)
  ON DELETE CASCADE;
