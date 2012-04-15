#!/bin/bash

set -e
cd "$(dirname "$(readlink -f $0)")"/..

DOIT=0
if [ "$1" == "--doit" ]; then
	DOIT=1
fi

PATTERN='^\s*\t|[\t\s]{1,}$'
if [ $DOIT -eq 1 ]; then
	find -name '*.scala' | xargs grep -Prl $PATTERN | xargs perl -i.spacesfix -p -e 'if(/^(\s*\t[\s\t]*)/) {($s, $m) = ($1, $1); $s =~ s/\t/    /g; s/^$m/$s/}; s/[\t\s]{1,}([\n\r]+)/$1/; print STDERR "\"$_\""; $_'
	find -name '*.scala.spacesfix' | xargs rm
else
	echo "This is only a dry run. Start this script with --doit to remove trailing spaces"
	echo "Before you do it, make sure you did a commit"
	find -name '*.scala' | xargs grep -Pr $PATTERN
fi
