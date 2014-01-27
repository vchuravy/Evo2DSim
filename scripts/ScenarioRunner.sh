#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
source "${DIR}/common.sh"

POOL_SIZE=500
GENERATIONS=500
PROBABILITY=0.05

FILE="Evo2DSim_cluster.sh"

cd $ROOT

CMD="${BASECMD} ${SCRIPTDIR}/${FILE} ${GENERATIONS} ${POOL_SIZE}"

declare -a ALGOS=("sus" "elite")

declare -a GENOMES=("ByteGenome"  "NEATGenome" "STDGenome")

declare -a STDGENOME=("true:1" "true:2" "true:3" "true:4" "true:5" "false:1" "false:2" "false:3" "false:4" "false:5")

declare -a ENVS=("0:basicRandom" "0:dynamicSimpleRandom" "0:basic;100:basicSimpleRandom;250:basicRandom" "0:ECECR" )

for algo in ${ALGOS[@]}
do
    for env in ${ENVS[@]}
    do
        for genome in ${GENOMES[@]}
        do
            if [ ${genome} == "STDGenome" ]
             then
                for setting in ${STDGENOME[@]}
                do
                    ${CMD} "${env}" "${algo}" "${genome}" "${PROBABILITY}" "-x ${setting}"
                done
             else
                 ${CMD} "${env}" "${algo}" "${genome}" "${PROBABILITY}"
             fi
        done
     done
done

