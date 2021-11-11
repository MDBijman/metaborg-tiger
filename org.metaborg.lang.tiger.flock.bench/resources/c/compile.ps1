param ([Parameter(Mandatory)]$name)
Measure-Command { clang -S -emit-llvm -O2 "$name.c" | Out-Default }
