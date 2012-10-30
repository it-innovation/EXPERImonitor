/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.wegov.prototype.web.resources.headsup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author mhm
 */
public class FileUtils {
	public static void copyFile(String srcFileName, String destFileName) throws FileNotFoundException, IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			File configFile = new File(srcFileName);
			in = new FileInputStream(configFile);
			File outFile = new File(destFileName);
			System.out.println("Creating " + outFile.getAbsolutePath());
			outFile.createNewFile();
			out = new FileOutputStream(outFile);
			int c;

			while ((c = in.read()) != -1) {
			out.write(c);
			}
		} finally {
			if (in != null) {
			in.close();
			}
			if (out != null) {
			out.close();
			}
		}
	}

	public static void writeStringToFile(String srcString, String destFileName) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(destFileName);
		out.println(srcString);
		out.close();
	}

	private static String readFileToString(FileReader fileReader) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(fileReader);
		char[] buf = new char[1024];
		int numRead=0;
		while((numRead=reader.read(buf)) != -1){
		    String readData = String.valueOf(buf, 0, numRead);
		    fileData.append(readData);
		    buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
	
	public static String readFileToString(File file) throws IOException {
		return readFileToString(new FileReader(file));
	}
	
	public static String readFileToString(String dir, String name) throws FileNotFoundException, IOException {
		return readFileToString(new FileReader(dir + File.separator + name));
	}
}
