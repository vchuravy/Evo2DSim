#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
source "${DIR}/common.sh"

FILE="Data_cluster.sh"

CMD="${BASECMD} ${SCRIPTDIR}/${FILE}"

FILES=${ROOT}/results/*/

declare -a GEN=(10,13 20,23 30,50 250,270 480,500)
cd ${ROOT}

for f in ${FILES}
do
    for i in ${GEN[@]} ;
    do
        declare -a V=$(IFS=$','; set ${i}; echo "$1 $2")
        ${CMD} ${V[@]} ${f}
    done
done
