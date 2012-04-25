#!/bin/bash

cd "$(dirname "$(readlink -f $0)")"/..
exec sbt package 'project ticketanalyzer-core' 'run-main de.jowisoftware.mining.Main'
