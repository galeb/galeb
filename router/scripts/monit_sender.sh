#!/bin/bash

ZBX_SENDER=/bin/zabbix_sender
HOST=`hostname -f`
SERVICE=$1

if [ "x${SERVICE}" != "x" ]
then
  MESSAGE="$(echo ${HOST} ${SERVICE} FAIL)"

  echo $ZBX_SENDER -z $ZBX_SERVER -p $ZBX_PORT -s passive_${HOST}-${SERVICE} -k passive_message -o ${MESSAGE} | logger
  $ZBX_SENDER -z $ZBX_SERVER -p $ZBX_PORT -s passive_${HOST}-${SERVICE} -k passive_message -o "${MESSAGE}"

  echo $ZBX_SENDER -z $ZBX_SERVER -p $ZBX_PORT -s passive_${HOST}-${SERVICE} -k passive_check -o 1 | logger
  $ZBX_SENDER -z $ZBX_SERVER -p $ZBX_PORT -s passive_${HOST}-${SERVICE} -k passive_check -o 1
fi
