
def gen_many_vars(n):
    f = open("many_vars.tig", "w")
    f.write("let var a0: int := 1\n")
    for i in range(1, n):
        f.write(f"    var a{i}: int := a{i - 1} + 1\n")
    f.write(f"in\n")
    f.write(f"\ta{n - 1}\n")
    f.write(f"end\n")

gen_many_vars(300)
