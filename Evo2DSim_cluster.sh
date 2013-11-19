# This is a simple SGE batch script you can run at OIST
# 
# request Bash shell as shell for job 
#$ -S /bin/bash 
#$ -N Evo2DSimVCHURAVY
# 
# Set all the variables SGE needs to run the job
#$ -q short 
#$ -M valentin-churavy@oist.jp 
#$ -m abe 
#$ -j yes 
#$ -l h_vmem=10g
#$ -l virtual_free=4g

cd /work/DoyaU/v-churavy/Evo2DSim/target
#java -server -Xmx4g -Xss100m -jar Evo2DSim-1.0-SNAPSHOT.jar
java -server -Xmx4g -Xms512M -Xss100m -jar Evo2DSim-1.0-SNAPSHOT.jar -g 500
