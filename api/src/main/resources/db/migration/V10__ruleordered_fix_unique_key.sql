ALTER TABLE `ruleordered`
DROP INDEX `UK_order__rule_id__virtualhostgroup_id__environment_id`;
ALTER TABLE `ruleordered`
ADD UNIQUE KEY `UK_order__rule_id__virtualhostgroup_id__environment_id` (`rule_order`,`rule_ruleordered_id`,`virtualhostgroup_ruleordered_id`,`environment_id`);