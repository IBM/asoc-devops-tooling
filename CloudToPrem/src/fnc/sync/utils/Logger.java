package fnc.sync.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Logger {

	File f = new File("C:\\ProgramData\\IBM\\ASoC2ASE\\logs");
	PrintWriter writer;
	
	public Logger() throws FileNotFoundException {
		this.writer = new PrintWriter("C:\\ProgramData\\IBM\\ASoC2ASE\\logs\\asoc2ase.log");
	}  
	
}
