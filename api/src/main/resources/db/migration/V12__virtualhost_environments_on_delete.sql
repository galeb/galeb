ALTER TABLE `virtualhost_environments`
DROP FOREIGN KEY `FK_environment_virtualhost_id`;
ALTER TABLE `virtualhost_environments`
ADD CONSTRAINT `FK_environment_virtualhost_id`
  FOREIGN KEY (`virtualhost_id`)
  REFERENCES `virtualhost` (`id`)
  ON DELETE CASCADE;