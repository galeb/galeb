ALTER TABLE `rule_pools`
DROP FOREIGN KEY `FK_rule_pool_id`;
ALTER TABLE `rule_pools`
ADD CONSTRAINT `FK_rule_pool_id`
  FOREIGN KEY (`pool_id`)
  REFERENCES `pool` (`id`)
  ON DELETE RESTRICT;