#!/bin/bash

fail=0

while IFS= read -r f; do
	dir=${f%%/*}
	if [ -x "$f" -a $dir != 'tools' -a $dir != 'gradlew' -a $dir != 'gradlew.bat' ]; then
		echo "$f"
		fail=1
	fi
done < <(git ls-files)

if [ $fail -eq 1 ]; then
	echo "There are executable files. Checkin stopped."
	exit 1
fi
