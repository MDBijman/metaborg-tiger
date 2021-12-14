import csv
import matplotlib.pyplot  as plt

csv_file = open('results/result.csv')
csv_reader = csv.reader(csv_file, delimiter=',')

csv_iter = iter(csv_reader)
headers = next(csv_iter)
print(headers)
data = {h: [] for h in headers}
for row in csv_iter:

    for (col, val) in zip(headers, row):
        data[col].append(val)

#

def plot_benchmark(fig, ax, name, simple_name):
    a, b = [], []
    err = []
    for i in range(0, len(data['Benchmark'])):
        if data['Benchmark'][i] == name:
            b.append(float(data['Score'][i]) / 1000.0)
            a.append(int(data['Param: count'][i]))
            err.append(float(data['Score Error (99.9%)'][i]) / 1000.0)

    if len(a) == 0 or len(b) == 0:
        raise Exception("no data for benchmark: " + name)

    plt.yscale("linear")
    ax.errorbar(x=a, y=b, yerr=err, label=simple_name)
    plt.xticks([x for x in range(0, 5001, 500)])
    plt.legend(loc="upper left")
    ax.set(xlabel='Size (lines)', ylabel='Time (s)', title='Synthetic Benchmarks')
    ax.grid(b=True)

fig, ax = plt.subplots()
plot_benchmark(fig, ax, "org.example.BranchBenchmark.run", "branches")
plot_benchmark(fig, ax, "org.example.VarsBenchmark.run", "variables")
plt.savefig("benchmark.png")
