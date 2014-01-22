#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd $DIR

cd ..
git pull
sbt clean compile
rm core/target/scala-2.10/classes/org/vastness/evo2dsim/App*
sbt compile core:stage

cd core
patch -p0 < java_version.patch
