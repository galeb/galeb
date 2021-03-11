## ADMIN ##
export LOCAL_TOKEN=pass

## DB ##
export GALEB_DB_URL='jdbc:mysql://127.0.0.1:3306/galeb_api?autoReconnect=true&createDatabaseIfNotExist=true'
export GALEB_DB_USER=root
export GALEB_DB_PASS=password
export GALEB_DB_DRIVER=com.mysql.cj.jdbc.Driver
export GALEB_DB_DIALECT=org.hibernate.dialect.MySQLDialect
export SHOW_SQL=true

## REDIS ##
export REDIS_URL='redis://127.0.0.1'

## LDAP ##
export GALEB_LDAP_URL=ldap://127.0.0.1:3890
export GALEB_LDAP_USER='cn=user1,dc=test'
export GALEB_LDAP_PASS='xxx'
export GALEB_LDAP_BASE='dc=test'
export GALEB_LDAP_ATTR_DN=cn
