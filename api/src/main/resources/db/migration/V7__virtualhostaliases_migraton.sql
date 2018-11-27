CREATE TABLE `virtual_host_aliases` (
  `virtual_host` bigint(20) NOT NULL,
  `aliases` varchar(255) DEFAULT NULL,
  KEY `FK_virtual_host_aliases_virtualhost_id` (`virtual_host`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1