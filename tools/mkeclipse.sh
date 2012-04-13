#!/bin/bash

set -e

cd "$(dirname "$(readlink -f $0)")"/..
sbt eclipse

find -name .classpath | xargs perl -i.orig -p -e 's#"(\.\.[\\/])*lib_managed#"../lib_managed#'
find -name .classpath.orig | xargs rm
