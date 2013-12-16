#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd $DIR
git pull
sbt clean compile
rm $DIR/target/scala-2.10/classes/org/vastness/evo2dsim/App*
sbt compile stage

patch -p0 < java_version.patch
