java -jar target/benchmarks.jar -f 1 -w 3 -wi 3 -r 3 -rff "results/result.csv" -gc true
py .\render.py "results/result.csv"
py .\rename_with_date.py "results/result.csv"