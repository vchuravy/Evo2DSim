#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
cd ${DIR}

POOL_SIZE=500
GENERATIONS=500
FILE="Evo2DSim_cluster.sh"
PROBABILITY=0.1

CMD="qsub ${FILE} ${GENERATIONS} ${POOL_SIZE}"

declare -a ALGOS=("sus" "elite")

declare -a GENOMES=("ByteGenome" "NEATGenome")

declare -a ENVS=("0:basic" "0:basicSimpleRandom" "0:basicRandom" "0:dynamicSimpleRandom" "0:basic;100:basicSimpleRandom" "0:basic;100:basicSimpleRandom;250:basicRandom" "0:basicSimpleRandom;250:basicRandom" "0:positive" "0:positiveRandom" "0:ECECR")

for algo in ${ALGOS[@]}
do
    for genome in ${GENOMES[@]}
    do
        for env in ${ENVS[@]}
        do
            ${CMD} "${env}" "${algo}" "${genome}" "${PROBABILITY}"
            sleep 5
        done
     done
done

