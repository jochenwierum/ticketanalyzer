#!/bin/bash

set -e
cd "$(dirname "$(readlink -f $0)")"/..

generateProjects() {
	basepath=$1; shift
	varprefix=$1; shift
	prefix=$1; shift

	for plugin in $(ls $basepath); do
		echo "lazy val $varprefix$plugin = Project(id=\"$prefix-$plugin\", base=file(\"$basepath/$plugin\")) dependsOn(common)"
		echo "projectList = $varprefix$plugin :: projectList"
	done
}

generateImporter() {
	generateProjects importer ticketanalyzerimporter ticketanalyzer-importer
}

generateLinker() {
	generateProjects linker ticketanalyzerlinker ticketanalyzer-linker
}

generateClass() {
	echo "//This file is autoatically generated by mksbt.sh - do not edit"
	echo "import sbt._"
	echo "import Keys._"
	echo "trait Projects {"
	echo "val common = Project(id=\"ticketanalyzer-common\", base=file(\"common\"))"
	echo "var core = Project(id=\"ticketanalyzer-core\", base=file(\"core\")) dependsOn(common)"
	echo "var projectList: List[Project] = List(common, core)"
	generateImporter
	generateLinker
	echo "}"
}

generateClass > project/projects.scala
