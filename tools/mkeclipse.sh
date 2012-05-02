#!/bin/bash

set -e

cd "$(dirname "$(readlink -f $0)")"/..
tools/sbt eclipse

find -name .classpath | xargs perl -i.orig -p -e 's#"(?:\.\.[\\/])*lib_managed(.*)kind="lib"#"PROJECT_LIBS$1kind="var"#'
find -name .classpath.orig | xargs rm
