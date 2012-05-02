#!/bin/bash

set -e

mkCall() {
	MAIN=$1
	shift
	SEP=$1
	shift
	
	echo -n "java -cp \""
	for f in $(find lib -name '*.jar'); do
		echo -n $f$SEP
	done
	echo "ticketanalyzer.jar\"" $MAIN "$@"
}

makeSH() {
	MAIN=$1
	shift
	cat <<EOF
#!/bin/bash

set -e
cd "\$(dirname "\$(readlink -f \$0)")"

EOF
	mkCall $MAIN ":" "$@"
}

makeBAT() {
	MAIN=$1
	shift
	cat <<EOF
@echo off

cd "%~dp0"

EOF
	mkCall $MAIN ";" "$@"
	echo "@echo on"
}

makeCYGWIN() {
	MAIN=$1
	shift
	cat <<EOF
#!/bin/bash

set -e
cd "\$(dirname "\$(readlink -f \$0)")"

EOF
	mkCall $MAIN ";" "$@"
}

copyPlugins() {
	ptype=$1

	for plugin in $(ls $ptype); do
		mkdir -p target/dist/plugins/$ptype
		cp $ptype/$plugin/target/scala-*/*$plugin*.jar target/dist/plugins/$ptype/$plugin.jar
	done
}



cd "$(dirname "$(readlink -f $0)")"/..

SKIP=0
if [ "$1" == "--skip" ]; then
	SKIP=1
fi

if [ $SKIP -eq 0 ]; then
	echo "* building"
	tools/sbt clean package >/dev/null
fi

echo "* preparing directories"
rm -rf target/dist
mkdir -p target/dist/lib
mkdir -p target/dist/plugins/importer
mkdir -p target/dist/plugins/linker

echo "* copying plugins"
cp core/target/scala-*/ticketanalyzer-core_*.jar target/dist/ticketanalyzer.jar
cp common/target/scala-*/ticketanalyzer-common_*.jar target/dist/lib/common.jar
copyPlugins importer
copyPlugins linker

echo "* patching configuration"
jar uf target/dist/ticketanalyzer.jar -C core/src/main/resources.package/ .

echo "* copying dependencies"
tools/sbt copy-dependencies >/dev/null

echo "* writing start scripts"
cd target/dist/
makeSH de.jowisoftware.mining.Main > start.sh
makeCYGWIN de.jowisoftware.mining.Main > startcygwin.sh
makeBAT de.jowisoftware.mining.Main > start.bat
makeSH de.jowisoftware.mining.gui.shell.Main db > startshell.sh
makeCYGWIN de.jowisoftware.mining.gui.shell.Main db > startshellcygwin.sh
makeBAT de.jowisoftware.mining.gui.shell.Main db > startshell.bat
chmod 755 *.sh
