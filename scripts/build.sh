#!/bin/bash

DIR="${HOME}/work/Evo2DSim/scripts"

if [ ! -d ${DIR} ]; then
  DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
fi

source "${DIR}/common.sh"
cd $ROOT

git pull --ff-only
sbt clean compile
rm ${ROOT}/core/target/scala-2.10/classes/org/vastness/evo2dsim/App*
sbt compile stage

# Patching files to run on cluster

declare -a BINARIES=("core" "data")
PATCHFILE="${ROOT}/patches/java_version.patch"

for bin in ${BINARIES[@]}
do
patch $(getCMD $bin) $PATCHFILE
done
