cd resources/c

benchmarks=(branches vars mod_inline scoped_vars)
N=(1000 3000 5000)
repeats=3
files=(./*.)
timestamp=$(date +"%Y%m%d")_$(date +"%k%M%S")
resultfile=../../results/llvm/result.csv
echo \"Benchmark\",\"Param: count\",\"Score\",\"Unit\",\"Samples\" >> $resultfile
for benchmark in "${benchmarks[@]}"
do
    for n in "${N[@]}"
    do
        # for ((i=0;i<3;i++)); do 
        file="${benchmark}_${n}.c"
        echo $file
        clang $file -S -emit-llvm -Xclang -disable-O0-optnone -o $file.ll -fbracket-depth=10001
        sed -i 's/noinline//g' $file.ll
        ts=$(date +%s%N)
        opt $file.ll -S -o $file.opt.ll --mem2reg --simplifycfg --sccp --simplifycfg
        ta=$((($(date +%s%N) - $ts)/1000000))
        echo \"$benchmark\",$n,$ta,\"ms/op\",1 >> $resultfile
        # done
    done
done


