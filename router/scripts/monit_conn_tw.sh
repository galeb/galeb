#!/bin/bash

CONN_TW="$(netstat -an | grep TIME_WAIT | wc -l)"
exit $CONN_TW
