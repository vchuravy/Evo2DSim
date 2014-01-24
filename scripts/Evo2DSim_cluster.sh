#!/bin/bash
# This is a simple SGE batch script you can run at OIST
#
# request Bash shell as shell for job
#$ -S /bin/bash
#$ -N Evo2DSimVCHURAVY
#
# Set all the variables SGE needs to run the job
#$ -q long
#$ -pe openmp 5
#$ -M valentin-churavy@oist.jp
#$ -m abe
#$ -j yes
#$ -l h_vmem=10g
#$ -l virtual_free=4g

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd ${DIR}
cd ..

JAVA_HOME="$HOME/java/current"
JAVA_OPTS="-server"
CMD=core/target/universal/stage/bin/evo2dsim-core

# -mem 4096 -g 500 -c "0:basic;50:basicSimpleRandom;200:basicRandom;400:dynamicSimpleRandom"
# -t => timeStep default=50
# -g => generations default=500
# -s => steps per Evaluation default=6000
# -e => Evaluation per Generation default=5
# -n => Pool size default=2000
# -z => Group Size default=10
# -c => Evaluation config default="0:basic"
# -a => Evolution algorithm default="sus"
# -y => GenomeType default="NEATGenome"
# -p => Probability for mutations default=0.1
# -x => GenomeSettings

$CMD -mem 4096 -s 1200 -g $1 -n $2 -c $3 -a $4 -y $5 -p $6 $7
