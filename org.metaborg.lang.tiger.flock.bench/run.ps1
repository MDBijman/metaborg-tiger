[CmdletBinding()]
param (
    [Parameter(Position = 1)]
    [String]
    $tag
)

java -jar target/benchmarks.jar -f 1 -w 3 -wi 3 -r 3 -rff "results/result.csv" -gc true
py .\render.py "results/result.csv" $tag
py .\rename_with_date.py "results/result.csv" $tag