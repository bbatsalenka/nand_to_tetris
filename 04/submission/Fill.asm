// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.

@8192
D=A

@MAX
M=D

(LOOP)
   @COUNTER
   M=0  
 
   @KBD
   D=M
   
   @DARKEN_SCREEN
   D;JGT

   @WHITEN_SCREEN
   D;JEQ

(DARKEN_SCREEN)
   @COUNTER
   D=M

   @MAX
   D=D-M

   @LOOP
   D;JEQ 
      
   @SCREEN      
   D=A
      
   @COUNTER
   A=D+M
   M=-1
      
   @COUNTER
   M=M+1
      
   @DARKEN_SCREEN
   0;JMP      

(WHITEN_SCREEN)
   @COUNTER
   D=M

   @MAX
   D=D-M

   @LOOP
   D;JEQ   
   
   @SCREEN   
   D=A
   
   @COUNTER
   A=D+M
   M=0
   
   @COUNTER
   M=M+1
   
   @WHITEN_SCREEN
   0;JMP   
