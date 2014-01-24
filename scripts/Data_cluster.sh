#!/bin/bash
# This is a simple SGE batch script you can run at OIST
#
# request Bash shell as shell for job
#$ -S /bin/bash
#$ -N Evo2DSimDATA-VCHURAVY
#
# Set all the variables SGE needs to run the job
#$ -q long
#$ -M valentin-churavy@oist.jp
#$ -m abe
#$ -j yes
#$ -l h_vmem=10g
#$ -l virtual_free=4g

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd "${DIR}/.."

BASEDIR=$( pwd )

JAVA_HOME="$HOME/java/current"
JAVA_OPTS="-server"
CMD="$BASEDIR/data/target/universal/stage/bin/evo2dsim-data"

$CMD -mem 4096 400 $1
