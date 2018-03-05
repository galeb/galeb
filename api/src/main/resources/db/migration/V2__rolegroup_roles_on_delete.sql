ALTER TABLE `rolegroup_roles`
DROP FOREIGN KEY `FK_rolegroup_role_id`;
ALTER TABLE `rolegroup_roles`
ADD CONSTRAINT `FK_rolegroup_role_id`
  FOREIGN KEY (`rolegroup_id`)
  REFERENCES `rolegroup` (`id`)
  ON DELETE CASCADE;
