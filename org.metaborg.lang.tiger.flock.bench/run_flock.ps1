[CmdletBinding()]
param (
    [Parameter(Position = 1)]
    [String]
    $tag
)

java -jar target/benchmarks.jar org.example.flock.* -f 1 -w 5 -wi 5 -r 3 -rff "results/flock/result.csv" -gc true
py .\render_jmh.py "results/flock/result.csv" flock $tag
py .\rename_with_date.py "results/flock/result.csv" $tag