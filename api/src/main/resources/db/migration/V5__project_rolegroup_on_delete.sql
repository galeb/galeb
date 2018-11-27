ALTER TABLE `rolegroup_projects`
DROP FOREIGN KEY `FK_project_rolegroup_id`;
ALTER TABLE `rolegroup_projects`
ADD CONSTRAINT `FK_project_rolegroup_id`
  FOREIGN KEY (`project_id`)
  REFERENCES `project` (`id`)
  ON DELETE CASCADE;
