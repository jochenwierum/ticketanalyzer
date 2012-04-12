#!/bin/bash

set -e

makeSH() {
	cat <<EOF
#!/bin/bash

set -e
cd "\$(dirname "\$(readlink -f \$0)")"

EOF

	echo -n "scala -cp \""
	for f in $(find -name '*.jar'); do
		echo -n $f':'
	done
	echo "\"" de.jowisoftware.mining.Main
}

makeBAT() {
	cat <<EOF
@echo off

cd "%~dp"

EOF

	echo -n "scala -cp \""
	for f in $(find -name '*.jar'); do
		echo -n $f';'
	done
	echo "\"" de.jowisoftware.mining.Main
	echo "@echo on"
}

echo "* checking preconditions"
if [ ! -d lib_managed ]; then
	echo "** compiling"
	sbt package
fi

echo "* preparing directories"
rm -rf target/dist
mkdir -p target/dist/lib
mkdir -p target/dist/plugins/importer

echo "* copying libraries"
find lib_managed -name '*.jar' -exec mv {} target/dist/lib/ \;

cp core/target/scala-2.9.1/ticketanalyzer-core_*.jar target/dist/
cp common/target/scala-2.9.1/ticketanalyzer-common_*.jar target/dist/lib/
cp importer/svn/target/scala-2.9.1/ticketanalyzer-importer-svn_*.jar target/dist/plugins/importer/
cp importer/trac/target/scala-2.9.1/ticketanalyzer-importer-trac_*.jar target/dist/plugins/importer/

echo "* writing start scripts"
makeSH > target/dist/start.sh
chmod 755 target/dist/start.sh
makeBAT > target/dist/start.bat

echo "* cleaning up"
rm -rf lib_managed
