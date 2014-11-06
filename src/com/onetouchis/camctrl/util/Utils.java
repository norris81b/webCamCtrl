package com.onetouchis.camctrl.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import sun.tools.tree.ThisExpression;

import com.google.gson.Gson;
import com.onetouchis.camctrl.PresetData;
import com.sun.tools.javac.code.Attribute.Array;

/**
 * Utility class to assist in the reading and saving preset data and other
 * common tasks.
 * @author bradnorris
 *
 */
public class Utils {
	
	public static Gson gson = new Gson();
	
	private static PresetData[] presets;
	
	private static String fileName;

	/**
	 * Reads in a Json formatted file
	 * @param filename fully qualified file name
	 * @param clazz type of the outer most class
	 * @return
	 * @throws Exception
	 */
	public static <T> Object loadJsonFromFile(String filename, Class<T> clazz) throws Exception
	{
		fileName = filename;
		File f = new File(filename);
		
		Object obj = gson.fromJson(new FileReader(f), clazz);
		
		return obj;
	}
	
	/**
	 * Loads a Json formatted presets file into the presets class attribute and
	 * returns the raw json string.
	 * @param filename fully qualified file name
	 * @return Json formatted string
	 * @throws Exception
	 */
	public static String loadFile(String filename) throws Exception
	{
		fileName = filename;
		File f = new File(filename);
		Scanner s = new Scanner(f);
		StringBuilder sb = new StringBuilder();
		while (s.hasNextLine())
		{
			sb.append(s.nextLine());
		}
		
		String fileData = sb.toString();
		PresetData[] pd = new PresetData[10];
		presets = gson.fromJson(fileData, pd.getClass());
		return fileData;
	}
	
	/**
	 * Updates the preset specified and saves the updated info to the presets file.
	 * @param number preset number
	 * @param text new text for the given preset number
	 * @return updated json string
	 * @throws IOException
	 */
	public static String updatePresetFile(int number, String text) throws IOException {
		for (int i = 0; i < 10; i++) {
			if (presets[i].getNumber() == number) {
				presets[i].setText(text);
			}
		}
		return savePresetsToFile();
	}
	
	/**
	 * Saves the presets class attribute to the file from which it was populated.
	 * @return
	 * @throws IOException
	 */
	public static String savePresetsToFile() throws IOException {
		String jsonStr = gson.toJson(presets);
		FileWriter fw = new FileWriter(fileName);
		fw.write(jsonStr);
		fw.close();
		return jsonStr;
	}

	/**
     * Converts a byte array to a string containing the hex values
     * 
     * @param b
     *            the byte array to convert
     * @return string containing hex values
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }
}
