
class Generator:
    pass



def gen_many_loops(n):
    f = open(f"many_while_{n}.c", "w")
    f.write("int main() {\n")

    f.write("\tint a0 = 1;\n")
    for i in range(1, 30):
        f.write(f"\tint a{i};\n")
        f.write(f"\twhile(a{i-1} > 0) {{\n")
        f.write(f"\t\ta{i} = a{i - 1};\n")
        f.write(f"\t\ta{i-1}--;\n")
        f.write("\t}\n")

    f.write("\treturn a29;\n")
    f.write("}\n")

def gen_many_vars(n):
    f = open(f"many_vars_{n}.c", "w")
    f.write("#include \"stdio.h\"\n")
    f.write("int main() {\n")

    f.write("\tint a0 = 1;\n")
    for i in range(1, n):
        f.write(f"\tint a{i} = a{i - 1} + 1;\n")

    f.write(f"\tprintf(\"%i\", a{n-1});")
    f.write(f"\tprintf(\"%i\", a{n-1});")
    f.write(f"\treturn a{n - 1};\n")
    f.write("}\n")    


def gen_many_vars2(n):
    f = open(f"many_vars2_{n}.c", "w")
    f.write("#include \"stdio.h\"\n")
    f.write("int main() {\n")

    f.write("\tint a0 = 1;\n")
    f.write("\tint a1 = 1;\n")
    for i in range(2, n, 2):
        f.write(f"\tint a{i} = a{i - 1} + 1;\n")
        f.write(f"\tint a{i+1} = a{i - 2} + 1;\n")

    f.write(f"\tint a{n} = a{n-1} + a{n-2};\n")

    f.write(f"\tprintf(\"%i\", a{n});")
    f.write(f"\treturn a{n};\n")
    f.write("}\n")    

    
def gen_many_branch(n):
    f = open(f"branches_{n}.c", "w")
    f.write("#include \"stdio.h\"\n")
    f.write("int main() {\n")

    f.write("\tint a0 = 1;\n")
    for i in range(1, n):
        f.write(f"\tint a{i};\n")
        f.write(f"\tif(a{i-1} > 0) {{ a{i} = a{i-1}; }} else {{ a{i} = -1; }}\n")

    f.write(f"\tprintf(\"%i\", a{n-1});")
    f.write(f"\treturn a{n-1};\n")
    f.write("}\n")    

class BranchesInWhile(Generator):
    def __init__(self, filename, count) -> None:
        super().__init__()
        self.filename = filename
        self.count = count

    def gen(self):
        n = self.count
        stmts = '\n'.join([
        fr'''int a{i};
if(a{i-1} > 0) {{ a{i} = a{i-1}; }} else {{ a{i} = 0; }}'''
    for i in range(1, n-1)])

        f = open(f"{self.filename}_{n}.c", "w")
        f.write(fr'''#include "stdio.h"    

int main() {{
    int a0 = 1;
    int a{n-1};

    while (a0 >= 1) {{
{stmts}
a{n-1} = a{n-2};
    }}
    
    printf("%i", a{n-1});
    return a{n-1};
}}
''')


class BranchesWithInlining(Generator):
    def __init__(self, filename, count) -> None:
        super().__init__()
        self.filename = filename
        self.count = count

    def gen(self):
        n = self.count
        stmts = '\n'.join([
        fr'''int a{i};
if(a{i-1} > 0) {{ a{i} = id(a{i-1}); }} else {{ a{i} = 0; }}'''
    for i in range(1, n-1)])

        f = open(f"{self.filename}_{n}.c", "w")
        f.write(fr'''#include "stdio.h"    
int id(int n) {{
    return n;
}}

int main() {{
    int a0 = 1;
    int a{n-1};

{stmts}
a{n-1} = a{n-2};
    
    printf("%i", a{n-1});
    return a{n-1};
}}
''')

class FactorialInlining(Generator):
    def __init__(self, filename, count) -> None:
        super().__init__()
        self.filename = filename
        self.count = count

    def gen(self):
        n = self.count
        stmts = '\n'.join([
        fr'''int a{i};
if(fact(a{i-1}) > 0) {{ a{i} = a{i-1}; }} else {{ a{i} = 0; }}'''
    for i in range(1, n-1)])

        f = open(f"{self.filename}_{n}.c", "w")
        f.write(fr'''#include "stdio.h"    
int fact(int n) {{
    if (n > 2) {{
        return fact(n - 1) * n;
    }}
    return n;
}}

int main() {{
    int a0 = 3;
    int a{n-1};

{stmts}
a{n-1} = a{n-2};
    
    printf("%i", a{n-1});
    return a{n-1};
}}
''')

generators = [
    #BranchesInWhile("branches_in_while", 10),
    BranchesWithInlining("branches_with_inlining", 10),
    BranchesWithInlining("branches_with_inlining", 100),
    FactorialInlining("factorial_inlining", 10)
]

for gen in generators:
    gen.gen()

#gen_many_loops()
#gen_many_vars(3000)
#gen_many_vars2(10)
#gen_many_branch(100)
#gen_looped_many_branch(1000)
