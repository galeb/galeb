#!/bin/bash

CONN_ESTABLISHED="$(netstat -an | grep ESTABLISHED | wc -l)"
exit $CONN_ESTABLISHED
