ALTER TABLE `rolegroup_teams`
DROP FOREIGN KEY `FK_team_rolegroup_id`;
ALTER TABLE `rolegroup_teams`
ADD CONSTRAINT `FK_team_rolegroup_id`
  FOREIGN KEY (`team_id`)
  REFERENCES `team` (`id`)
  ON DELETE CASCADE;
