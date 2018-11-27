ALTER TABLE `health_status`
DROP FOREIGN KEY `FK_healthstatus_target`;
ALTER TABLE `health_status`
ADD CONSTRAINT `FK_healthstatus_target`
  FOREIGN KEY (`target_id`)
  REFERENCES `target` (`id`)
  ON DELETE CASCADE;
