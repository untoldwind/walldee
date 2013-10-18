#! /usr/bin/env bash

DIRNAME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

$DIRNAME/sbt stage
$DIRNAME/target/universal/stage/bin/walldee -DapplyEvolutions.default=true &
