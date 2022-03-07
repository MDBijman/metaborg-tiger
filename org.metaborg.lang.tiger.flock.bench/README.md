mvn install
java -jar .\target\benchmarks.jar

clang file.c -S -emit-llvm -Xclang -disable-O0-optnone && sed -i 's/noinline//g' file.ll
/mnt/d/dev/llvm-project/llvm/build/bin/opt branches_7000.ll -S -o branches_7000.opt.ll -time-passes --mem2reg --sccp --debug --stats 2> _out
opt branches_7000.ll -S -o branches_7000.opt.ll --mem2reg --simplifycfg --sccp --simplifycfg