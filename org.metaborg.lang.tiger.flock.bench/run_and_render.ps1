[CmdletBinding()]
param (
    [Parameter(Position = 1)]
    [String]
    $tag
)

#java -jar target/benchmarks.jar org.example.dr.* -f 1 -w 3 -wi 3 -r 3 -rff "results/dr/result.csv" -gc true
java -jar target/benchmarks.jar org.example.flock.* -f 1 -w 3 -wi 3 -r 3 -rff "results/flock/result.csv" -gc true
.\run_llvm.ps1
# py .\render_bench.py
# py .\rename_with_date.py "results/dr/result.csv" dr $tag
# py .\rename_with_date.py "results/flock/result.csv" flock $tag
# py .\rename_with_date.py "results/llvm/result.csv" llvm $tag