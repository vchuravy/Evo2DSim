# config settings
# Getting script dir
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}")" && pwd )"
ROOT=$( cd "${SCRIPTDIR}/.." && pwd )

SERVERJAVA="${HOME}/java/current"
SERVERROOT="/work/DoyaU/v-churavy/Evo2DSim"

CMDPATH="target/universal/stage/bin"

JAVA_OPTS="-server"

if [ -d ${SERVERJAVA} ]; then
  JAVA_HOME="$HOME/java/current"
fi

if [ ! -d "${ROOT}/core/${CMDPATH}" ]; then
  ROOT=${SERVERROOT}
fi

if [ "${SCRIPTDIR}" !=  "${ROOT}/scripts" ]; then
    SCRIPTDIR="${ROOT}/scripts"
fi

DISTRIBUTION=$(lsb_release -is)
declare -a DEV_DISTRIBUTIONS=("Arch")

if [[ " ${DEV_DISTRIBUTIONS[@]} " =~ " ${DISTRIBUTION} " ]]; then
  BASECMD="bash"
else
  BASECMD="qsub"
fi

function getCMD {
   local CMD="${ROOT}/${1}/${CMDPATH}/evo2dsim-${1}"
   echo "${CMD}"
}