// Performing constant push
@111
D=A
@SP
M=M+1
A=M-1
M=D
// Performing constant push
@333
D=A
@SP
M=M+1
A=M-1
M=D
// Performing constant push
@888
D=A
@SP
M=M+1
A=M-1
M=D
// Performing temp pop
@SP
AM=M-1
D=M
@24
M=D
// Performing temp pop
@SP
AM=M-1
D=M
@19
M=D
// Performing temp pop
@SP
AM=M-1
D=M
@17
M=D
// Performing temp/static push
@19
D=M
@SP
M=M+1
A=M-1
M=D
// Performing temp/static push
@17
D=M
@SP
M=M+1
A=M-1
M=D
// Performing general sub or add
@SP
M=M-1
A=M-1
D=M
A=A+1
D=D-M
A=A-1
M=D
// Performing temp/static push
@24
D=M
@SP
M=M+1
A=M-1
M=D
// Performing general sub or add
@SP
M=M-1
A=M-1
D=M
A=A+1
D=D+M
A=A-1
M=D
