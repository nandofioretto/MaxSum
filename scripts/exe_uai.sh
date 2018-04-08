#!/usr/bin/env bash

folder=$1
file=$2
ITERS=200

# Paths
dcop_path="/home/fioretto/Repos/MaxSum/"
data_path=${dcop_path}"data/uai/${folder}/"
scripts_path=${dcop_path}"scripts/"
# Exe
ccg_maker=${scripts_path}"wcsp"
dcop_gen=${scripts_path}"dcop_gen_rand.py"
dcop_gen_sf=${scripts_path}"dcop_gen_scalefree.py"
dcop_gen_grid=${scripts_path}"dcop_gen_grid.py"

ccg_to_dcop=${scripts_path}"ccg_to_dcop.py"
dcop_stats=${scripts_path}"postprocess_ccg.py"
pipeline_path=${scripts_path}"code/"


#file=$(basename "${file%.uai}")

#######################
# Create instance
#######################
if [ ! -f  ${data_path}${file}".wcsp" ]; then

    out_val=$(python uai_to_dcop.py -i ${data_path}${file}".uai" -o ${data_path}${file})

    if [ "$out_val" == "Graph is not connected" ]; then
        echo "$file: $out_val - skipping computations"
        continue
    fi

    if [ "$out_val" == "Domain size must be 2" ]; then
        echo "$file: $out_val - skipping computations"
        continue
    fi
    echo $file " (nodes: " $out_val
fi

#######################
# Convert WCSP to CCG
#######################
echo "Converting WCSP to CCG: $file"

start_time=$(date +%s.%N)
$ccg_maker -K ${data_path}${file}".wcsp" -c ${data_path}${file}".ccg" > /dev/null 2>&1
#$ccg_maker -K -f u ${data_path}${file}".uai" -c ${data_path}${file}".ccg" > /dev/null 2>&1

out_val=$(python $ccg_to_dcop -i ${data_path}${file}".ccg" -o ${data_path}${file}"_ccg")
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "GGC computation time: %.4f seconds\n" $dur

#######################
# Solve c++
#######################
echo -n "wcsp with BP on original problem "
start_time=$(date +%s.%N)
$ccg_maker -M -K -t 10 ${data_path}${file}.wcsp | egrep "value|best"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

echo -n "wcsp with BP on CCG problem + Kernelization "
start_time=$(date +%s.%N)
./wcsp -m m  -t 10 ${data_path}${file}.wcsp  | egrep "value|best"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

echo -n "wcsp with BP on CCG problem  (No KERNELIZATION) "
start_time=$(date +%s.%N)
./wcsp -m m -k -t 10 ${data_path}${file}.wcsp  | egrep "value|best"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

echo -n "wcsp with LP: "
start_time=$(date +%s.%N)
./wcsp -L -t 30 -K ${data_path}${file}.wcsp | egrep "value|best"
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur

#######################
# Solve CCG-DCOP
#######################
if [ "$out_val" == "Problem already solved" ]; then
    echo "$out_val - skipping computation"
else
    echo "calling CCG DCOP solver"
    taskset 0x1 java -jar ${dcop_path}ccg_dcop_jar/ccg_dcop.jar ${data_path}${file}"_ccg.json" -a CCG -i ${ITERS} -o ${data_path}${file}"_stats.json"
    #######################
    # Compute solution
    #######################
    python $dcop_stats -i ${data_path}${file}".json" -c ${data_path}${file}"_ccg.json" -s ${data_path}${file}"_stats.json" -o out.csv
fi


#######################
# Solve MaxSum
#######################
echo "MaxSum (FRODO)"
start_time=$(date +%s.%N)
taskset 0x1 ${pipeline_path}maxSumPipeline.sh ${data_path}${file}
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur


#######################
# Solve DSA
#######################
echo "DSA (FRODO)"
start_time=$(date +%s.%N)
taskset 0x1 ${pipeline_path}dsaPipeline.sh ${data_path}${file}
dur=$(echo "$(date +%s.%N) - $start_time" | bc)
printf "%.2f seconds\n" $dur
