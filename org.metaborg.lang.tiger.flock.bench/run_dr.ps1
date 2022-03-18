[CmdletBinding()]
param (
    [Parameter(Position = 1)]
    [String]
    $tag
)

java -jar target/benchmarks.jar org.example.dr.* -f 1 -w 5 -wi 5 -r 3 -rff "results/dr/result.csv" -gc true
#py .\render_jmh.py "results/dr/result.csv" dr $tag
py .\rename_with_date.py "results/dr/result.csv" dr $tag