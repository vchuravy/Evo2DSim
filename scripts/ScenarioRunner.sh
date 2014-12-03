#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
source "${DIR}/common.sh"

POOL_SIZE=500
GENERATIONS=500
PROBABILITY=0.05
STDPROBABILITY=0.08
BYTEPROBABILITY=0.01

FILE="Evo2DSim_cluster.sh"

cd $ROOT

CMD="${BASECMD} ${SCRIPTDIR}/${FILE} ${GENERATIONS} ${POOL_SIZE}"

declare -a ALGOS=("sus" "elite")

declare -a GENOMES=("ByteGenome" "STDGenome")

declare -a STDGENOME=("true:2" "true:3" "false:2" "false:3")

declare -a BYTEGENOME=("true" "false")

declare -a ENVS=("0:basicRandom" "0:basic;50:basicSimpleRandom;150:basicRandom" "0:ECECR" )

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
                    ${CMD} "${env}" "${algo}" "${genome}" "${STDPROBABILITY}" "-x ${setting}"
                done
             elif [ ${genome} == "ByteGenome" ]; then
                for setting in ${BYTEGENOME[@]}
                do
                    ${CMD} "${env}" "${algo}" "${genome}" "${BYTEPROBABILITY}" "-x ${setting}"
                done
             else
                 ${CMD} "${env}" "${algo}" "${genome}" "${PROBABILITY}"
             fi
        done
     done
done

