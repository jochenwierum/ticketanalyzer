#!/bin/bash

cd "$(dirname "$(readlink -f $0)")"/..
exec sbt 'project ticketanalyzer-core' run
