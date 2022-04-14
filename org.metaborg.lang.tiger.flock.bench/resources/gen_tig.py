import sys 
n = sys.argv[1]

def gen_vars(n):
    f = open("tiger/vars_" + str(n) + ".tig", "w")
    f.write("let var a0: int := 1\n")
    for i in range(1, n):
        f.write(f"    var a{i}: int := a{i - 1} + 1\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

gen_vars(int(n))

def gen_scoped_vars(n):
    f = open("tiger/scoped_vars_" + str(n) + ".tig", "w")
    f.write(f"let var a{0}: int := 1 in\n")
    for i in range(1, n):
        f.write(f"let var a{i}: int := a{i - 1} + a{0} + 1 in\n")
    f.write(f"\ta{n - 1}\n")
    for i in range(n):
        f.write(f"end\n")

gen_scoped_vars(int(n))

def gen_scoping(n):
    f = open("tiger/scoping_" + str(n) + ".tig", "w")

    f.write("let var a0: int := 1\n")
    def gen_scope(n):
        pass
    # for i in range(1, n):
        # f.write(f"    var a{i}: int := a{i - 1} + 1\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

#gen_scoping(int(n))

def gen_branches(n):
    f = open("tiger/branches_" + str(n) + ".tig", "w")
    f.write("let var a0: int := 1\n")
    for i in range(1, n):
        f.write(f"    var a{i}: int := if a{i - 1} > 0 then a{i-1} else 0\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

gen_branches(int(n))

def gen_recursive_inlining(n):
    f = open("tiger/rec_inline_" + str(n) + ".tig", "w")
    f.write("let function a0(n: int): int = n + 1\n")
    for i in range(1, n):
        f.write(f"    function a{i}(n{i}: int): int = a{i-1}(n{i}) + 1\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}(1)\n")
    f.write(f"end\n")

gen_recursive_inlining(int(n))

def gen_modular_inlining(n):
    f = open("tiger/mod_inline_" + str(n) + ".tig", "w")
    f.write("let function a0(a: int): int = a + 1\n")
    for i in range(1, n):
        f.write(f"    function a{i}(a: int): int = a + {i}\n")
    f.write(f"in\n")
    for i in range(0, n - 1):
        f.write(f"\ta{i}(1) + \n")
    f.write(f"\ta{n - 1}(1)\n")
    f.write(f"end\n")

gen_modular_inlining(int(n))

