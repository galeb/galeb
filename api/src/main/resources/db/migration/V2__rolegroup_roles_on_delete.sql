ALTER TABLE `galeb_api`.`rolegroup_roles` 
DROP FOREIGN KEY `FK_rolegroup_role_id`;
ALTER TABLE `galeb_api`.`rolegroup_roles` 
ADD CONSTRAINT `FK_rolegroup_role_id`
  FOREIGN KEY (`rolegroup_id`)
  REFERENCES `galeb_api`.`rolegroup` (`id`)
  ON DELETE CASCADE;
