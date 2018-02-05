ALTER TABLE `galeb_api`.`rolegroup_projects`
DROP FOREIGN KEY `FK_project_rolegroup_id`;
ALTER TABLE `galeb_api`.`rolegroup_projects`
ADD CONSTRAINT `FK_project_rolegroup_id`
  FOREIGN KEY (`project_id`)
  REFERENCES `galeb_api`.`project` (`id`)
  ON DELETE CASCADE;
