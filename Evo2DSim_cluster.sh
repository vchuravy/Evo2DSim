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

JAVA_OPTS="-server"
CMD=target/universal/stage/bin/evo2dsim

cd /work/DoyaU/v-churavy/Evo2DSim
$CMD -mem 4096 -g 500 -c "0:basic;50:basicSimpleRandom;200:basicRandom;400:dynamicSimpleRandom"
