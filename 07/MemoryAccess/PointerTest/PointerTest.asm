// Performing constant push
@3030
D=A
@SP
M=M+1
A=M-1
M=D
// Performing pointer pop
@SP
AM=M-1
D=M
@THIS
M=D
// Performing constant push
@3040
D=A
@SP
M=M+1
A=M-1
M=D
// Performing pointer pop
@SP
AM=M-1
D=M
@THAT
M=D
// Performing constant push
@32
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@THIS
D=M
@2
D=A+D
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Performing constant push
@46
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@THAT
D=M
@6
D=A+D
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Performing pointer push
@THIS
D=M
@SP
M=M+1
A=M-1
M=D
// Performing pointer push
@THAT
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
// Performing general push
@THIS
D=M
@2
A=A+D
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
// Performing general push
@THAT
D=M
@6
A=A+D
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
