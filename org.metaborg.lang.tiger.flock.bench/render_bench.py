from datetime import datetime
import sys
from matplotlib.axes import Axes
import matplotlib.pyplot as plt
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

def render_benchmark(fig, ax: Axes, bench):
    lines = []
    labels = []
    for instance in bench['instances']:
        if "errors" in instance:
            lines += ax.errorbar(x=instance['counts'], y=instance['scores'], yerr=instance['errors'])
        else:
            lines += ax.plot(instance['counts'], instance['scores'])
        labels.append(instance['type'])
    ax.legend(lines, labels)
    ax.set_ylim(bottom=0)
    ax.set(xlabel='Count', ylabel='Time (s)', title=bench['name'])
    ax.grid(b=True)


benchmarks = [
    {
        'name': "Linear Def-Use",
        'outname': "vars",
        'instances': [
            {
                'key': "org.example.dr.VarsBenchmark.run",
                'type': "dynamic rules",
                'file': "results/dr/result.csv"
            },
            {
                'key': "org.example.flock.VarsBenchmark.run",
                'type': "flock",
                'file': "results/flock/result.csv"
            },
            {
                'key': "vars",
                'type': "llvm",
                'file': "results/llvm/result.csv"
            },
        ]
    },
    {
        'name': "Branched Def-Use",
        'outname': "branches",
        'instances': [
            {
                'key': "org.example.dr.BranchBenchmark.run",
                'type': "dynamic rules",
                'file': "results/dr/result.csv"
            },
            {
                'key': "org.example.flock.BranchBenchmark.run",
                'type': "flock",
                'file': "results/flock/result.csv"
            },
            {
                'key': "branches",
                'type': "llvm",
                'file': "results/llvm/result.csv"
            },
        ]
    },
    {
        'name': "Inlining (a)",
        'outname': "mod_inline",
        'instances': [
            {
                'key': "org.example.dr.BranchBenchmark.run",
                'type': "dynamic rules",
                'file': "results/dr/result.csv"
            },
            {
                'key': "org.example.flock.BranchBenchmark.run",
                'type': "flock",
                'file': "results/flock/result.csv"
            },
            {
                'key': "branches",
                'type': "llvm",
                'file': "results/llvm/result.csv"
            },
        ]
    }
]

def fill_benchmark_data(benchmark):
    for instance in benchmark['instances']:
        csv_reader = csv.reader(open(instance['file']), delimiter=',')

        csv_iter = iter(csv_reader)
        headers = next(csv_iter)
        benchmark_name_idx = headers.index("Benchmark")
        score_idx = headers.index("Score")
        count_idx = headers.index("Param: count")

        counts = []
        scores = []
        for row in csv_iter:
            if row[benchmark_name_idx] == instance['key']:
                counts.append(int(row[count_idx]))
                scores.append(float(row[score_idx]) / 1000.0)

        instance["counts"] = counts
        instance["scores"] = scores

for bench in benchmarks:
    fill_benchmark_data(bench)

datestring = str(datetime.today().strftime('%Y%m%d_%H%M%S'))

for bench in benchmarks:
    fig, ax = plt.subplots()
    render_benchmark(fig, ax, bench)

    # plt.savefig(f"results/render/{datestring}_{bench['outname']}.pgf", bbox_inches='tight')
    plt.savefig(f"results/render/{datestring}_{bench['outname']}.png", bbox_inches='tight')
