#!/bin/bash

set -e
cd "$(dirname "$(cd "$(dirname "spaces.sh")" && pwd)/)")"

GIT=0
if [ "$1" == "--git" ]; then
	shift
	GIT=1
fi

DOIT=0
if [ "$1" == "--doit" ]; then
	shift
	DOIT=1
fi

result=1
PATTERN='^\s*\t|[\t\s]{1,}$'
if [ $DOIT -eq 1 ]; then
	find -name '*.scala' -print0 | xargs -r0 grep -PrlZ $PATTERN | xargs -0 perl -i.spacesfix -p -e 'if(/^(\s*\t[\s\t]*)/) {($s, $m) = ($1, $1); $s =~ s/\t/    /g; s/^$m/$s/}; s/[\t\s]{1,}([\n\r]+)/$1/; $_'
	find -name '*.scala.spacesfix' -print0 | xargs -r0 rm
	result=0
else
	if [ $GIT -eq 0 ]; then
		echo "This is only a dry run. Start this script with --doit to remove trailing spaces"
		echo "Before you do it, make sure you did a commit"
	else
		echo "Checking for illegal spaces..."
	fi
	set +e
	find -name '*.scala' -print0 | xargs -r0 grep -Prn $PATTERN
	if [ $? -eq 123 ]; then
		result=0
	fi
	set -e
fi

exit $result
