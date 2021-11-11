import sys 

def gen_vars(n):
    f = open("vars_" + str(n) + ".tig", "w")
    f.write("let var a0: int := 1\n")
    for i in range(1, n):
        f.write(f"    var a{i}: int := a{i - 1} + 1\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

def gen_branches(n):
    f = open("branches_" + str(n) + ".tig", "w")
    f.write("let var a0: int := 1\n")
    for i in range(1, n):
        f.write(f"    var a{i}: int := if a{i - 1} > 0 then a{i-1}\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

n = sys.argv[1]
gen_vars(int(n))
gen_branches(int(n))


