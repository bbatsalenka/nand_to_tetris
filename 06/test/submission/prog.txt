package com.bbatsalenka.tc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HackAssembler {
	
	// ***** Literal constants *****
	private static final int A_INSTRUCTION_LENGTH = 16;
	private static final String NONE = "000";
	private static final String AT_SYMBOL = "@";
	private static final String SEMICOLON = ";";
	private static final String EQUALS_SIGN = "=";
	private static final String C_INSTRUCTION_PREFIX = "111";
	private static final String LABEL_PATTERN = "\\((.*)\\)";
	private static final String COMMENT = "//";
	
	// ***** Static data structures *****
	private static final Map<String, String> DEST_INSTRUCTION_TABLE = new HashMap<>();
	private static final Map<String, String> JUMP_INSTRUCTION_TABLE = new HashMap<>();
	private static final Map<String, String> COMP_A_EQ_ONE_TABLE = new HashMap<>();
	private static final Map<String, String> COMP_A_EQ_ZERO_TABLE = new HashMap<>();

	// ***** Member fields *****
	private Map<String, Integer> symbolTable = new HashMap<>();
	private List<String> programInAssembly = new ArrayList<>();
	private List<String> programInAssemblyClean = new ArrayList<>();
	private List<String> programInBinary = new ArrayList<>();
	private int registersCounter = 16;
	private int instrCounter = 0;
	private Pattern pattern;

	{
		// ****** Populate symbol table ******
		
		// First 16 registers
		symbolTable.put("R0", 0);
		symbolTable.put("R1", 1);
		symbolTable.put("R2", 2);
		symbolTable.put("R3", 3);
		symbolTable.put("R4", 4);
		symbolTable.put("R5", 5);
		symbolTable.put("R6", 6);
		symbolTable.put("R7", 7);
		symbolTable.put("R8", 8);
		symbolTable.put("R9", 9);
		symbolTable.put("R10", 10);
		symbolTable.put("R11", 11);
		symbolTable.put("R12", 12);
		symbolTable.put("R13", 13);
		symbolTable.put("R14", 14);
		symbolTable.put("R15", 15);
		// screen
		symbolTable.put("SCREEN", 16384);
		// keyboard
		symbolTable.put("KBD", 24576);
		// other
		symbolTable.put("SP", 0);
		symbolTable.put("LCL", 1);
		symbolTable.put("ARG", 2);
		symbolTable.put("THIS", 3);
		symbolTable.put("THAT", 4);
		
		// compile pattern
		pattern = Pattern.compile(LABEL_PATTERN);
	}
	
	static {
		
		// ****** Populate instruction tables ******
		
		// DEST table
		DEST_INSTRUCTION_TABLE.put(NONE, "000");
		DEST_INSTRUCTION_TABLE.put("M", "001");
		DEST_INSTRUCTION_TABLE.put("D", "010");
		DEST_INSTRUCTION_TABLE.put("MD", "011");
		DEST_INSTRUCTION_TABLE.put("A", "100");
		DEST_INSTRUCTION_TABLE.put("AM", "101");
		DEST_INSTRUCTION_TABLE.put("AD", "110");
		DEST_INSTRUCTION_TABLE.put("AMD", "111");
		
		// JUMP table
		JUMP_INSTRUCTION_TABLE.put(NONE, "000");
		JUMP_INSTRUCTION_TABLE.put("JGT", "001");
		JUMP_INSTRUCTION_TABLE.put("JEQ", "010");
		JUMP_INSTRUCTION_TABLE.put("JGE", "011");
		JUMP_INSTRUCTION_TABLE.put("JLT", "100");
		JUMP_INSTRUCTION_TABLE.put("JNE", "101");
		JUMP_INSTRUCTION_TABLE.put("JLE", "110");
		JUMP_INSTRUCTION_TABLE.put("JMP", "111");
		
		// COMP a equals 0 table
		COMP_A_EQ_ZERO_TABLE.put("0", "101010");
		COMP_A_EQ_ZERO_TABLE.put("1", "111111");
		COMP_A_EQ_ZERO_TABLE.put("-1", "111010");
		COMP_A_EQ_ZERO_TABLE.put("D", "001100");
		COMP_A_EQ_ZERO_TABLE.put("A", "110000");
		COMP_A_EQ_ZERO_TABLE.put("!D", "001101");
		COMP_A_EQ_ZERO_TABLE.put("!A", "110001");
		COMP_A_EQ_ZERO_TABLE.put("D+1", "011111");
		COMP_A_EQ_ZERO_TABLE.put("A+1", "110111");
		COMP_A_EQ_ZERO_TABLE.put("D-1", "001110");
		COMP_A_EQ_ZERO_TABLE.put("A-1", "110010");
		COMP_A_EQ_ZERO_TABLE.put("D+A", "000010");
		COMP_A_EQ_ZERO_TABLE.put("D-A", "010011");
		COMP_A_EQ_ZERO_TABLE.put("A-D", "000111");
		COMP_A_EQ_ZERO_TABLE.put("D&A", "000000");
		COMP_A_EQ_ZERO_TABLE.put("D|A", "010101");
		
		// COMP a equals 1 table
		COMP_A_EQ_ONE_TABLE.put("M", "110000");
		COMP_A_EQ_ONE_TABLE.put("!M", "110001");
		COMP_A_EQ_ONE_TABLE.put("M+1", "110111");
		COMP_A_EQ_ONE_TABLE.put("M-1", "110010");
		COMP_A_EQ_ONE_TABLE.put("D+M", "000010");
		COMP_A_EQ_ONE_TABLE.put("D-M", "010011");
		COMP_A_EQ_ONE_TABLE.put("M-D", "000111");
		COMP_A_EQ_ONE_TABLE.put("D&M", "000000");
		COMP_A_EQ_ONE_TABLE.put("D|M", "010101");
	}
	
	private String addressToAInstruction(int address) {
		String binaryEquivalent = Integer.toBinaryString(address);
		int lengthDifference = A_INSTRUCTION_LENGTH - binaryEquivalent.length();
		if (lengthDifference > 0) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < lengthDifference; i++) {
				builder.append("0");
			}
			return builder.toString() + binaryEquivalent;
		}
		return binaryEquivalent;
	}
	
	private void firstPass() {
		if (programInAssembly == null || programInAssembly.isEmpty()) {
			throw new RuntimeException("The program has not been loaded for some reason!");
		}
		for (int i = 0; i < programInAssembly.size(); i++) {
			if (programInAssembly.get(i).matches(LABEL_PATTERN)) {
				Matcher matcher = pattern.matcher(programInAssembly.get(i));
				matcher.matches(); // needed for the next line to work
				symbolTable.put(matcher.group(1), instrCounter);
			} else {
				programInAssemblyClean.add(programInAssembly.get(i));
				instrCounter++;
			}
		}
	}
	
	private void secondPass() {
		for (int i = 0; i < programInAssemblyClean.size(); i++) {
			String instruction = programInAssemblyClean.get(i);
			if (!instruction.matches(LABEL_PATTERN)) {
				if (isAInstruction(instruction)) {
					String cleanInstruction = instruction.split(AT_SYMBOL)[1];
					if (isSymbolAInstruction(instruction)) {
						if (!symbolTable.containsKey(cleanInstruction)) {
							symbolTable.put(cleanInstruction, registersCounter++);
						}
						programInBinary.add(addressToAInstruction(symbolTable.get(cleanInstruction)));
					} else {
						programInBinary.add(addressToAInstruction(Integer.valueOf(cleanInstruction)));
					}
				} else {
					String compBinary = null, jumpBinary = null, destBinary = null;
					boolean isAZero = false;
					StringBuilder finalCommand = new StringBuilder();
					finalCommand.append(C_INSTRUCTION_PREFIX);
					if (instruction.contains(EQUALS_SIGN)) {
						String[] destAndComp = instruction.split(EQUALS_SIGN);
						destBinary = DEST_INSTRUCTION_TABLE.get(destAndComp[0]);
						jumpBinary = JUMP_INSTRUCTION_TABLE.get(NONE);
						compBinary = destAndComp[1];
					} else if (instruction.contains(SEMICOLON)) {
						String[] evalAndJump = instruction.split(SEMICOLON);
						compBinary = evalAndJump[0];
						destBinary = DEST_INSTRUCTION_TABLE.get(NONE);
						jumpBinary = JUMP_INSTRUCTION_TABLE.get(evalAndJump[1]);
					}
					if (COMP_A_EQ_ZERO_TABLE.containsKey(compBinary)) {
						compBinary = COMP_A_EQ_ZERO_TABLE.get(compBinary);
						isAZero = true;
					} else {
						compBinary = COMP_A_EQ_ONE_TABLE.get(compBinary);
						if (compBinary == null) {
							throw new RuntimeException("The comp instruction is invalid!");
						}
					}
					finalCommand.append(isAZero ? "0" : "1");
					finalCommand.append(compBinary);
					finalCommand.append(destBinary);
					finalCommand.append(jumpBinary);
					programInBinary.add(finalCommand.toString());
				}
			}
		}
	}
	
	private boolean isSymbolAInstruction(String instruction) {
		String strippedOffInstr = instruction.split("@")[1];
		char[] instructionVariable = strippedOffInstr.toCharArray();
		for (char instructionChar : instructionVariable) {
			int charRemainder = instructionChar - '0';
			if (charRemainder > 9) {
				return true;
			}
		}
		return false;
	}
	
	private void readProgramIntoArray(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			throw new RuntimeException("File name is not provided!");
		}
		File programFile = new File(filePath);
		if (!programFile.exists()) {
			throw new RuntimeException("Could not find file specified!");
		}
		try {
			FileReader fileReader = new FileReader(programFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while((line = bufferedReader.readLine()) != null) {
				if (line != null && !line.isEmpty() && !line.startsWith(COMMENT)) {
					if (line.contains(COMMENT)) {
						line = line.split(COMMENT)[0];
					}
					line = line.trim();
					programInAssembly.add(line);
				}
			}
			fileReader.close();
		} catch (IOException e) {
			throw new RuntimeException("Got exception while trying to read program file " + programFile);
		}
	}
	
	private boolean isAInstruction(String instruction) {
		return instruction.startsWith(AT_SYMBOL);
	}
	
	private void writeBinaryInstructionsToFile(String originalProgramFilePath) {
		if (programInBinary == null || programInBinary.isEmpty()) {
			throw new RuntimeException("For some reason the program hasn't been converted!");
		}
		
		String[] programNameSplit = originalProgramFilePath.split(File.separator);
		String programName = programNameSplit[programNameSplit.length - 1].split("\\.")[0];
		BufferedWriter writer = null;
		System.out.println("Saving file as " + programName + ".hack");
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(programName + ".hack"), "utf-8"));
			for (String binaryInstruction : programInBinary) {
				writer.write(binaryInstruction);
				writer.write('\n');
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to create file with binary program.");
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					System.out.println("Failed to close filewriter!");
				}
			}
		}
	}
	
	private void convertAssemblyToBinaryInstructions(String filePath) {
		readProgramIntoArray(filePath);
		firstPass();
		secondPass();
		writeBinaryInstructionsToFile(filePath);
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please, provide the path to the file with your program!");
			System.exit(1);
		}
		HackAssembler assemblerInstance = new HackAssembler();
		System.out.println("Going to parse file " + args[0]);
		assemblerInstance.convertAssemblyToBinaryInstructions(args[0]);
	}
	
}
