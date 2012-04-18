#!/bin/bash

cd "$(dirname "$(readlink -f $0)")"/..
exec sbt package 'project ticketanalyzer-shell' 'run db/'
