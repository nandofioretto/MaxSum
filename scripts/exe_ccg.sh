#!/usr/bin/env bash

filein=$1
fileout=$2
# Paths
dcop_path="/home/fioretto/Repos/MaxSum/"
data_path=${dcop_path}"data/"
scripts_path=${dcop_path}"scripts/"
# Exe
ccg_maker=${scripts_path}"wcsp"
dcop_gen=${scripts_path}"dcop_gen_rand.py"
ccg_to_dcop=${scripts_path}"ccg_to_dcop.py"
dcop_stats=${scripts_path}"postprocess_ccg.py"
pipeline_path=${scripts_path}"code/"

N_AGTS=100
N_P1=0.8
N_ARIETY=3
N_NAME="rand"
# Generate Random WCSP
wcsp_file=$N_NAME

#######################
# Create instance
#######################
out_val=$(python $dcop_gen -a ${N_AGTS} -d 2 -p ${N_P1} -r ${N_ARIETY} -c 100 -n ${N_NAME} -o ${data_path}${wcsp_file})
fail="sanity check failed!"
if [ "$out_val" == "$fail" ]; then
    echo "captured error $out_val"
    exit
fi

#######################
# Convert WCSP to CCG
#######################
start_time=$(date +%s.%N)
$ccg_maker -K ${data_path}${wcsp_file}".wcsp" -c ${data_path}${wcsp_file}".ccg" > /dev/null 2>&1
python $ccg_to_dcop -i ${data_path}${wcsp_file}".ccg" -o ${data_path}${wcsp_file}"_ccg.json"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

#######################
# Solve JAVA
#######################
#java -jar XXX {data_path}${wcsp_file}"_ccg.json"
/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java -javaagent:/home/fioretto/Programs/intellij-2017/lib/idea_rt.jar=37513:/home/fioretto/Programs/intellij-2017/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/management-agent.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/home/fioretto/Repos/MaxSum/target/classes:/home/fioretto/.m2/repository/commons-io/commons-io/2.2/commons-io-2.2.jar:/home/fioretto/.m2/repository/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar:/home/fioretto/.m2/repository/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar:/home/fioretto/.m2/repository/junit/junit/4.10/junit-4.10.jar:/home/fioretto/.m2/repository/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar \
dcop_jtools ${data_path}${wcsp_file}"_ccg.json" -a CCG -i 20 -o ${data_path}${wcsp_file}"_stats.json"

#######################
# Compute solution
#######################
python $dcop_stats -i ${data_path}${wcsp_file}".json" -c ${data_path}${wcsp_file}"_ccg.json" -s ${data_path}${wcsp_file}"_stats.json" -o out.csv

#######################
# Solve c++
#######################
echo -n "wcsp with BP on original problem "
start_time=$(date +%s.%N)
$ccg_maker -M ${data_path}${wcsp_file}.wcsp -t 2 | grep "value"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

echo -n "wcsp with BP on CCG problem "
start_time=$(date +%s.%N)
./wcsp -m m ${data_path}${wcsp_file}.wcsp -t 2  | grep "value"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

echo -n "wcsp with LP: "
start_time=$(date +%s.%N)
./wcsp -L ${data_path}${wcsp_file}.wcsp -t 30 | grep "value"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

#######################
# Solve MaxSum
#######################
echo "MaxSum (FRODO)"
start_time=$(date +%s.%N)
${pipeline_path}maxSumPipeline.sh ${data_path}${wcsp_file}
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur


#######################
# Solve DSA
#######################
echo "DSA (FRODO)"
start_time=$(date +%s.%N)
${pipeline_path}dsaPipeline.sh ${data_path}${wcsp_file}
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur
