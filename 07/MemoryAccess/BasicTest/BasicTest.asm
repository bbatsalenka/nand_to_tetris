// Performing constant push
@10
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@LCL
D=M
@0
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
@21
D=A
@SP
M=M+1
A=M-1
M=D
// Performing constant push
@22
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@ARG
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
// Performing general pop
@ARG
D=M
@1
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
@36
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@THIS
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
// Performing constant push
@42
D=A
@SP
M=M+1
A=M-1
M=D
// Performing constant push
@45
D=A
@SP
M=M+1
A=M-1
M=D
// Performing general pop
@THAT
D=M
@5
D=A+D
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// Performing general pop
@THAT
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
@510
D=A
@SP
M=M+1
A=M-1
M=D
// Performing temp pop
@SP
AM=M-1
D=M
@11
M=D
// Performing general push
@LCL
D=M
@0
A=A+D
D=M
@SP
M=M+1
A=M-1
M=D
// Performing general push
@THAT
D=M
@5
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
// Performing general push
@ARG
D=M
@1
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
@THIS
D=M
@6
A=A+D
D=M
@SP
M=M+1
A=M-1
M=D
// Performing general push
@THIS
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
// Performing general sub or add
@SP
M=M-1
A=M-1
D=M
A=A+1
D=D-M
A=A-1
M=D
// Performing temp push
@11
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
