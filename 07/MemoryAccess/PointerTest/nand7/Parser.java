package nand7;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Converting script " + Arrays.toString(args));
		Parser parser = new Parser(args[0]);
		parser.generateAssemblyCode();
	}
	
	// MEMORY SEGMENTS
	private static final String SP_POINTER = "SP";
	private static final String LCL_POINTER = "LCL";
	private static final String ARG_POINTER = "ARG";
	private static final String THIS_POINTER = "THIS";
	private static final String THAT_POINTER = "THAT";
	
	// GENERAL LITERAL CONSTANTS
	private static final char NEW_LINE_CHAR = '\n';
	private static final int TEMP_SEGMENT_FIRST_REG = 5;
	private static final char AT = '@';
	
	// VARIABLE STORAGE REGISTERS
	private static final String R13 = "@R13";
	private static final String R14 = "@R14";
	private static final String R15 = "@R15";
	
	private List<String> vmScript;
	private List<String> assemblyScript;
	private String scriptName;
	
	public Parser(String fileName) throws FileNotFoundException {
		
		final File script = new File(fileName);
		scriptName = fileName.split("\\.")[0];
		
		if (!script.exists()) {
			throw new FileNotFoundException("Could not find file with name " + fileName);
		}
		
		vmScript = new ArrayList<>();
	    
		BufferedReader reader = new BufferedReader(new FileReader(script));
	    
	    String line = null;
	    
	    try {
	    	
	    	while ((line = reader.readLine()) != null) {
	    		vmScript.add(line);
	    	}
	    	
	    } catch (IOException e) {
	    	System.out.println("Error occurred while reading file " + fileName);
	    	e.printStackTrace();
	    	throw new RuntimeException(e);
	    }
	    
	    try {
	    	reader.close();
	    } catch (IOException e) {
	    	System.out.println("Failed to close reader");
	    	throw new RuntimeException(e);
	    }
	}
	
	public void generateAssemblyCode() {
		
		assemblyScript = new ArrayList<>();
		AbstractParser aParser = new ArithmeticParser(assemblyScript);
		AbstractParser maParser = new MemoryAccessParser(assemblyScript);
		
		for (String line : vmScript) {
			
			if (line != null && !line.isEmpty() && !line.startsWith("//")) {
				
				System.out.println("Parsing line " + line);
				Command command = new Command(line);
				
				if (command.isPop() || command.isPush()) {
					maParser.handleOperation(command);
				} else {
					aParser.handleOperation(command);
				}
				
			}
		}
		if (!assemblyScript.isEmpty()) {
			BufferedWriter writer = null;
            System.out.println("Saving file as " + scriptName + ".asm");
            try {
                    writer = new BufferedWriter(new OutputStreamWriter(
                              new FileOutputStream(scriptName + ".asm"), "utf-8"));
                    for (String binaryInstruction : assemblyScript) {
                            writer.write(binaryInstruction);
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
	}
	
	class ArithmeticParser extends AbstractParser {
		
		public ArithmeticParser(List<String> assemblyScript) {
			super(assemblyScript);
		}
		
		private void subOrAdd(StringBuilder builder, OperationName operation) {
			builder.append("// Performing general sub or add").append(NEW_LINE_CHAR);
			builder.append(AT).append(SP_POINTER).append(NEW_LINE_CHAR);
			builder.append("M=M-1").append(NEW_LINE_CHAR);
			builder.append("A=M-1").append(NEW_LINE_CHAR);
			builder.append("D=M").append(NEW_LINE_CHAR);
			builder.append("A=A+1").append(NEW_LINE_CHAR);
			if (operation == OperationName.ADD) {
				builder.append("D=D+M").append(NEW_LINE_CHAR);
			} else {
				builder.append("D=D-M").append(NEW_LINE_CHAR);
			}
			builder.append("A=A-1").append(NEW_LINE_CHAR);
			builder.append("M=D").append(NEW_LINE_CHAR);
		}
		
		@Override
		public void handleOperation(Command command) {
			
			StringBuilder builder = new StringBuilder();
			
			if (command.isAdd()) {
				
				//@SP
				//M=M-1
				//A=M-1
				//D=M
				//A=A+1
				//D=D+/-M
				subOrAdd(builder, OperationName.ADD);
				
			} else if (command.isSub()) {
				
				subOrAdd(builder, OperationName.SUB);
				
			}
			assemblyScript.add(builder.toString());
		}
		
	}
	
	class MemoryAccessParser extends AbstractParser {
		
		public MemoryAccessParser(List<String> assemblyScript) {
			super(assemblyScript);
		}
		
		private void generalPush(String pointerName, StringBuilder builder, int value) {
			
			//@LCL
			//D=M
			//@value
			//A=A+D
			//D=M
			//@SP
			//A=M
			//M=D
			//@SP
			//M=M+1
			
			builder.append("// Performing general push").append(NEW_LINE_CHAR);
			builder.append(AT).append(pointerName).append(NEW_LINE_CHAR);
			builder.append("D=M").append(NEW_LINE_CHAR);
			builder.append(AT).append(value).append(NEW_LINE_CHAR);
			builder.append("A=A+D").append(NEW_LINE_CHAR);
			builder.append("D=M").append(NEW_LINE_CHAR);
			updateSpPush(builder);
		}
		
		private void generalPop(String pointerName, StringBuilder builder, int value) {
			
			//@SP
			//DM=M-1
			//A=D
			//D=M
			
			//@R13
			//M=D
			
			//@LCL
			//D=M
			//@value
			//A=A+D
			//
			
			builder.append("// Performing general pop").append(NEW_LINE_CHAR);
			
			builder.append(AT).append(pointerName).append(NEW_LINE_CHAR);
			builder.append("D=M").append(NEW_LINE_CHAR);
			builder.append(AT).append(value).append(NEW_LINE_CHAR);
			builder.append("D=A+D").append(NEW_LINE_CHAR);
			builder.append(R13).append(NEW_LINE_CHAR);
			builder.append("M=D").append(NEW_LINE_CHAR);
			
			updateSpPopAndSetD(builder);
			
			builder.append(R13).append(NEW_LINE_CHAR);
			builder.append("A=M").append(NEW_LINE_CHAR);
			builder.append("M=D").append(NEW_LINE_CHAR);
		}
		
		private void updateSpPush(StringBuilder builder) {
			builder.append(AT).append(SP_POINTER).append(NEW_LINE_CHAR);
			builder.append("M=M+1").append(NEW_LINE_CHAR);
			builder.append("A=M-1").append(NEW_LINE_CHAR);
			builder.append("M=D").append(NEW_LINE_CHAR);
		}
		
		private void updateSpPopAndSetD(StringBuilder builder) {
			builder.append(AT).append(SP_POINTER).append(NEW_LINE_CHAR);
			builder.append("AM=M-1").append(NEW_LINE_CHAR);
			builder.append("D=M").append(NEW_LINE_CHAR);
		}
		
		@Override
		public void handleOperation(Command command) {
			
			StringBuilder builder = new StringBuilder();
			
			if (command.isPush()) {
				switch (command.getMemorySegmentType()) {
				
					case LOCAL: 
						generalPush(LCL_POINTER, builder, command.getValue()); break;
						
					case TEMP:
						int registerNum = TEMP_SEGMENT_FIRST_REG + command.getValue();
						builder.append("// Performing temp push").append(NEW_LINE_CHAR);
						builder.append("@").append(registerNum).append(NEW_LINE_CHAR);
						builder.append("D=M").append(NEW_LINE_CHAR);
						updateSpPush(builder);
						
						break;
					
					case ARG: 
						generalPush(ARG_POINTER, builder, command.getValue()); break;
						
					case THIS: 
						generalPush(THIS_POINTER, builder, command.getValue()); break;
						
					case THAT:
						generalPush(THAT_POINTER, builder, command.getValue()); break;
					
					case CONST:
						builder.append("// Performing constant push").append(NEW_LINE_CHAR);
						builder.append(AT).append(command.getValue()).append(NEW_LINE_CHAR);
						builder.append("D=A").append(NEW_LINE_CHAR);
						updateSpPush(builder);
						
						break;
					
					case POINTER:
						builder.append("// Performing pointer push").append(NEW_LINE_CHAR);
						
						builder.append(AT);
						if (command.getValue() == 0) {
							builder.append(THIS_POINTER);
						} else {
							builder.append(THAT_POINTER);
						}
						
						builder.append(NEW_LINE_CHAR);
						builder.append("D=M").append(NEW_LINE_CHAR);
						updateSpPush(builder);
						
						break;
						
					case STATIC:
						builder.append("// Performing static push").append(NEW_LINE_CHAR);
//						builder.append(scriptName).append(NEW_LINE_CHAR);
						
						break;
						
					default: 
						throw new RuntimeException("Unsupported segment type for current functionality");
				}
			} else if (command.isPop()) {
				switch (command.getMemorySegmentType()) {
				
					case LOCAL:
						generalPop(LCL_POINTER, builder, command.value); break;
						
					case ARG:
						generalPop(ARG_POINTER, builder, command.value); break;
						
					case THIS:
						generalPop(THIS_POINTER, builder, command.value); break;
						
					case THAT:
						generalPop(THAT_POINTER, builder, command.value); break;
						
					case TEMP:
						builder.append("// Performing temp pop").append(NEW_LINE_CHAR);
						updateSpPopAndSetD(builder);
						
						int registerNum = TEMP_SEGMENT_FIRST_REG + command.getValue();
						builder.append(AT).append(registerNum).append(NEW_LINE_CHAR);
						builder.append("M=D").append(NEW_LINE_CHAR);
						
						break;
					
					case POINTER:
						builder.append("// Performing pointer pop").append(NEW_LINE_CHAR);
						updateSpPopAndSetD(builder);
						
						builder.append(AT);
						if (command.getValue() == 0) {
							builder.append(THIS_POINTER);
						} else {
							builder.append(THAT_POINTER);
						}
						builder.append(NEW_LINE_CHAR);
						
						builder.append("M=D").append(NEW_LINE_CHAR);
						
						break;
						
					default:
						throw new RuntimeException("Unsupported segment type");
				
				}
			} else {
				throw new RuntimeException("Unknown operation name!");
			}
			
			assemblyScript.add(builder.toString());
			
		}
		
	}
	
	abstract class AbstractParser {
		
		protected List<String> assemblyScript;
		
		public AbstractParser(List<String> assemblyScript) {
			this.assemblyScript = assemblyScript;
		}
		
		public abstract void handleOperation(Command command);
	}
	
	class Command {
		
		private OperationName operation;
		private MemorySegmentType memorySegmentType;
		private int value;
		
		public Command(String command) {
			String[] parsedCommand = command.split(" ");
			
			operation = OperationName.getOperationNameByString(parsedCommand[0]);
			
			if (isPop() || isPush()) {
				memorySegmentType = MemorySegmentType.getMemorySegmentByString(parsedCommand[1]);
				value = Integer.valueOf(parsedCommand[2]);
			}
			
		}

		public OperationName getOperation() {
			return operation;
		}

		public MemorySegmentType getMemorySegmentType() {
			return memorySegmentType;
		}

		public int getValue() {
			return value;
		}
		
		public boolean isPop() {
			return operation == OperationName.POP;
		}
		
		public boolean isPush() {
			return operation == OperationName.PUSH;
		}
		
		public boolean isAdd() {
			return operation == OperationName.ADD;
		}
		
		public boolean isSub() {
			return operation == OperationName.SUB;
		}
		
	}
	
	enum MemorySegmentType {
		LOCAL("local"), TEMP("temp"), ARG("argument"), THIS("this"), THAT("that"), CONST("constant"), STATIC("static"), POINTER("pointer");
		
		private String type;
		
		public String getType() {
			return type;
		}
		
		private MemorySegmentType(String type) {
			this.type = type;
		}
		
		public static MemorySegmentType getMemorySegmentByString(String segment) {
			for (MemorySegmentType value : MemorySegmentType.values()) {
				if (value.getType().equals(segment)) {
					return value;
				}
			}
			return null;
		}
	}
	
	enum OperationName {
		ADD("add"), SUB("sub"), POP("pop"), PUSH("push");
		
		private String name;
		
		public String getName() {
			return name;
		}
		
		private OperationName(String name) {
			this.name = name;
		}
		
		public static OperationName getOperationNameByString(String name) {
			for (OperationName value : OperationName.values()) {
				if (value.getName().equals(name)) {
					return value;
				}
			}
			return null;
		}
	}
		
}
