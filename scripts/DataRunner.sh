#! /bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
source "${DIR}/common.sh"

FILE="Data_cluster.sh"

CMD="${BASECMD} ${SCRIPTDIR}/${FILE}"

FILES=${ROOT}/results/*/
cd ${ROOT}

for f in $FILES
do
  $CMD "$f"
done
