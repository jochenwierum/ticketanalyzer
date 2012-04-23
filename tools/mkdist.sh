#!/bin/bash

set -e

mkCall() {
	MAIN=$1
	shift
	SEP=$1
	shift
	
	echo -n "scala -cp \""
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

cd "$(dirname "$(readlink -f $0)")"/..

SKIP=0
if [ "$1" == "--skip" ]; then
	SKIP=1
fi

if [ $SKIP -eq 0 ]; then
	echo "* building"
	sbt clean package >/dev/null
fi

echo "* preparing directories"
rm -rf target/dist
mkdir -p target/dist/lib
mkdir -p target/dist/plugins/importer

echo "* copying jar files"
cp core/target/scala-2.9.1/ticketanalyzer-core_*.jar target/dist/ticketanalyzer.jar
cp shell/target/scala-2.9.1/ticketanalyzer-shell*.jar target/dist/lib/shell.jar
cp common/target/scala-2.9.1/ticketanalyzer-common_*.jar target/dist/lib/common.jar
cp importer/svn/target/scala-2.9.1/ticketanalyzer-importer-svn_*.jar target/dist/plugins/importer/svn.jar
cp importer/trac/target/scala-2.9.1/ticketanalyzer-importer-trac_*.jar target/dist/plugins/importer/trac.jar

echo "* patching configuration"
jar uf target/dist/ticketanalyzer.jar -C core/src/main/resources.package/ .

echo "* copying dependencies"
find lib_managed -name '*.jar' -exec cp {} target/dist/lib/ \;

echo "* writing start scripts"
cd target/dist/
makeSH de.jowisoftware.mining.Main > start.sh
makeCYGWIN de.jowisoftware.mining.Main > startcygwin.sh
makeBAT de.jowisoftware.mining.Main > start.bat
makeSH de.jowisoftware.mining.shell.Main db > startshell.sh
makeCYGWIN de.jowisoftware.mining.shell.Main db > startshellcygwin.sh
makeBAT de.jowisoftware.mining.shell.Main db > startshell.bat
chmod 755 *.sh
