import csv
import matplotlib
matplotlib.use("pgf")
matplotlib.rcParams.update({
    "pgf.texsystem": "pdflatex",
    'font.family': 'serif',
    'text.usetex': True,
    'pgf.rcfonts': False,
    'font.size': 18
})
import matplotlib.pyplot  as plt
import sys
from datetime import datetime

file = sys.argv[1]
csv_file = open(file)
csv_reader = csv.reader(csv_file, delimiter=',')
benchmark = sys.argv[2]
tag = str(f"_{sys.argv[3]}" if len(sys.argv) >= 4 else "")

csv_iter = iter(csv_reader)
headers = next(csv_iter)
print(headers)
data = {h: [] for h in headers}
for row in csv_iter:

    for (col, val) in zip(headers, row):
        data[col].append(val)

def make_datestring():
    return str(datetime.today().strftime('%Y%m%d_%H%M%S'))

today = make_datestring()

def plot_benchmark(fig, ax, name, simple_name):
    a, b = [], []
    err = []
    max_param = 0
    for i in range(0, len(data['Benchmark'])):
        if data['Benchmark'][i] == name:
            b.append(float(data['Score'][i]) / 1000.0)
            
            param = int(data['Param: count'][i])
            if param > max_param:
                max_param = param
            a.append(int(data['Param: count'][i]))
            err.append(float(data['Score Error (99.9%)'][i]) / 1000.0)

    if len(a) == 0 or len(b) == 0:
        raise Exception("no data for benchmark: " + name)

    plt.yscale("linear")
    ax.errorbar(x=a, y=b, yerr=err, label=simple_name)
    plt.xticks([x for x in range(0, max_param+1, 500)], rotation=45)
    ax.set_ylim(bottom=0)
    plt.legend(loc="upper left")
    ax.set(xlabel='Size (lines)', ylabel='Time (s)', title='Synthetic Benchmarks')
    ax.grid(b=True)

fig, ax = plt.subplots()

plot_benchmark(fig, ax, "org.example.dr.BranchBenchmark.run", "branches")
plot_benchmark(fig, ax, "org.example.dr.VarsBenchmark.run", "variables")

# bbox_inches='tight' avoids legend from cutting off
plt.savefig(f"results/{benchmark}/{today}{tag}_benchmark.pgf", bbox_inches='tight')
plt.savefig(f"results/{benchmark}/{today}{tag}_benchmark.png", bbox_inches='tight')

