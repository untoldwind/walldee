#! /usr/bin/env bash

DIRNAME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PID_FILE=$DIRNAME/target/universal/stage/RUNNING_PID

if [ -f $PID_FILE ]
then
    pid=`cat $PID_FILE`
    echo "stopping walldee ($pid)"
    kill -SIGTERM $pid
else
    echo "no walldee application is running"
fi

exit 0
