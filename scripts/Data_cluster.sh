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

NAME="data"
DIR="${HOME}/work/Evo2DSim/scripts"

if [ ! -d ${DIR} ]; then
  DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
fi

source "${DIR}/common.sh"
CMD=$(getCMD $NAME)
cd $ROOT

$CMD -mem 4096 $1 $2 $3  && ${ROOT}/data/scripts/eval_result_agent.r $1/
