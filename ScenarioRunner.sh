#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd ${DIR}

POOL_SIZE=500
GENERATIONS=500
FILE="Evo2DSim_cluster.sh"

CMD="qsub ${FILE} ${GENERATIONS} ${POOL_SIZE}"

declare -a ALGOS=("sus" "elite")

declare -a ENVS=("0:basic" "0:basicSimpleRandom" "0:basic;100:basicSimpleRandom" "0:basic;100:basicSimpleRandom;250:basicRandom")

for algo in ${ALGOS[@]}
do
    for env in ${ENVS[@]}
    do
        ${CMD} "${env}" "${algo}"
        sleep 5 
    done
done

