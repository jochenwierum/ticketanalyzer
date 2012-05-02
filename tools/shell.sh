#!/bin/bash

cd "$(dirname "$(readlink -f $0)")"/..
exec tools/sbt package 'project ticketanalyzer-core' 'run-main de.jowisoftware.mining.gui.shell.Main db/'
