import sys 
n = sys.argv[1]

def gen_vars(n):
    f = open("c/vars_" + str(n) + ".c", "w")
    f.write("int main() {\n")
    f.write("int a0 = 1;\n")
    for i in range(1, n):
        f.write(f"int a{i} = a{i-1} + 1;\n")
    f.write(f"return a{n - 1};\n")
    f.write("}")

gen_vars(int(n))

def gen_scoped_vars(n):
    f = open("c/scoped_vars_" + str(n) + ".c", "w")
    f.write("int main() {\n")
    f.write("int a0 = 1;\n")
    for i in range(1, n):
        f.write(f"int a{i} = a{i-1} + 1;\n{{\n")
        if i == n - 1:
            f.write(f"return a{i};\n")
    for i in range(1, n):
        f.write("}\n")
    f.write("}")

gen_scoped_vars(int(n))

def gen_scoping(n):
    f = open("c/scoping_" + str(n) + ".c", "w")

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
    f = open("c/branches_" + str(n) + ".c", "w")
    f.write("int main() {\n")
    f.write("int a0 = 1;\n")
    for i in range(1, n):
        f.write(f"int a{i};\n")
        f.write(f"if (a{i - 1} > 0) {{ a{i} = a{i-1}; }} else {{ a{i} = 0; }}\n")
    f.write(f"return a{n - 1};\n")
    f.write("}")

gen_branches(int(n))

def gen_recursive_inlining(n):
    f = open("c/rec_inline_" + str(n) + ".c", "w")
    f.write("int a0(int a) { return a + 1; }\n")
    for i in range(1, n):
        f.write(f"int a{i}(int a) {{ return a{i - 1}(a) + {i}; }}\n")
    f.write("int main() {\n")
    f.write(f"return a{n-1}(1);\n")
    f.write("}")

gen_recursive_inlining(int(n))

def gen_modular_inlining(n):
    f = open("c/mod_inline_" + str(n) + ".c", "w")
    f.write("int a0(int a) { return a + 1; }\n")
    for i in range(1, n):
        f.write(f"int a{i}(int a) {{ return a + {i}; }}\n")
    f.write("int main() {\n")
    f.write("return\n")
    for i in range(0, n - 1):
        f.write(f"\ta{i}(1) + \n")
    f.write(f"\ta{n - 1}(1);\n")
    f.write("}")

gen_modular_inlining(int(n))
