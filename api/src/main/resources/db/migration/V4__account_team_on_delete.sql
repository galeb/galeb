ALTER TABLE `team_accounts`
DROP FOREIGN KEY `FK_account_id`;
ALTER TABLE `team_accounts`
ADD CONSTRAINT `FK_account_id`
  FOREIGN KEY (`account_id`)
  REFERENCES `account` (`id`)
  ON DELETE CASCADE;
