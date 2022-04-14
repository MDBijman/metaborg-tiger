[CmdletBinding()]
param (
    [Parameter(Position = 1)]
    [String]
    $tag
)

# -f 1 = 1 fork
# -w 2 = warmup at least 2 seconds
# -wi 2 = warmup 2 iters
# -r 3 = run at least 3 seconds
# -i 3 = run 3 iterations
#java -jar target/benchmarks.jar org.example.dr.* -f 1 -w 2 -wi 2 -r 3 -i 3 -rff "results/dr/result.csv" -gc true
#java -jar target/benchmarks.jar org.example.flock.* -f 1 -w 2 -wi 2 -r 3 -i 3 -rff "results/flock/result.csv" -gc true
#.\run_llvm.ps1
#py .\render_bench.py
py .\rename_with_date.py "results/dr/result.csv" dr $tag
py .\rename_with_date.py "results/flock/result.csv" flock $tag
py .\rename_with_date.py "results/llvm/result.csv" llvm $tag