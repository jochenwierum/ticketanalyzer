#!/bin/bash

cd "$(dirname "$(readlink -f $0)")"/..
exec tools/sbt 'project ticketanalyzer-core' console
