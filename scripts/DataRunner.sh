#! /bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"

FILE="Data_cluster.sh"

DISTRIBUTION=$(lsb_release -is)
declare -a DEV_DISTRIBUTIONS=("Arch")

if [[ " ${DEV_DISTRIBUTIONS[@]} " =~ " ${DISTRIBUTION} " ]]; then
BASECMD="bash"
else
BASECMD="qsub"
fi

CMD="${BASECMD} ${DIR}/${FILE}"

cd "$DIR/.."
BASEDIR=$( pwd )
FILES=$BASEDIR/results/*/

for f in $FILES 
do
  $CMD "$f"
done
