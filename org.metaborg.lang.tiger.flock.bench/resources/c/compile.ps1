param ([Parameter(Mandatory)]$name)
Measure-Command { clang -S -emit-llvm "$name.c" | Out-Default }
