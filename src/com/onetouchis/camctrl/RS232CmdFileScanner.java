/*
    RS232Camera - Controls PTZ features of a camera via Socket and RS232
    Copyright (C) 2014  One Touch Integrated Systems, LLC

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package com.onetouchis.camctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads the command data from a resource file into a collection of 
 * command data. See <code>RS232CmdData</code>
 * Expected format of command is one per line as follows (cmds do not include '<' or '>'):
 * <mandatory command name (defined in RS232CmdData)>,<mandatory 1 byte command>,
 *      <optional n byte argument>,<optional 1 byte expected response>,<1 byte completion code>,
 *      <delay before next cmd>
 * Example 1: HOME_POSITION_MOVE,39,,B1
 * In the above example, the HOME_POSITION_MOVE command is defined as having a cmd of 0x39, 
 * no parameters, an expected response of 0xB1, no completion code, using the default delay.
 * Example 2: SET_PRESET_POSITION,11,00..09,B1,B4
 * In the above example the SET_PRESET_POSITION command is defined as having a cmd of 0x11,
 * parameter that must fall between 0x00 and 0x09, an expected response of 0xB1, and a completion
 * code of 0xB4 using the default delay
 * @author bradnorris
 *
 */
public class RS232CmdFileScanner {

    /** default delay time */
    private static int DEFAULT_TIME_BEFORE_NEXT_CMD = -750;

    /**
     * parse each line to create an RS32CmdData from the comma separated values
     * 
     * @param scanner
     *            source containing command data, typically a file
     * @return
     */
    private List<RS232CmdData> readData(Scanner scanner) {
        String nextLine = null;
        List<RS232CmdData> cmdList = new LinkedList<RS232CmdData>();
        while (scanner.hasNext()) {
            try {
                nextLine = scanner.nextLine();
                String[] lineParts = nextLine.split(",");
                RS232CmdData cmdData = new RS232CmdData();
                // first field is the name
                cmdData.setName(lineParts[0]);
                int part = 0;
                // second field is the one byte command
                if (lineParts[1] != null && !("".equals(lineParts[1]))) {
                    try {
                        part = Integer.parseInt(lineParts[1], 16);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    cmdData.setCode((byte) part);
                } else {
                    System.out.println("Warning new code for " + lineParts[0]);
                }

                byte[] argBytes = null;
                // third field is the arguments
                if (lineParts.length >= 3) {
                    if (lineParts[2].length() > 0) {
                        int dotsIndex = lineParts[2].indexOf("..");
                        if (dotsIndex > 0) {
                            int min = Integer.parseInt(
                                    lineParts[2].substring(0, dotsIndex), 16);
                            cmdData.setArgumentMin(min);
                            int max = Integer.parseInt(
                                    lineParts[2].substring(dotsIndex + 2), 16);
                            cmdData.setArgumentMax(max);
                        } else {
                            argBytes = getHexBytes(lineParts[2]);
                            cmdData.setArgument(argBytes);
                        }
                    }
                    // fourth field is the expected response
                    if (lineParts.length >= 4) {
                        argBytes = getHexBytes(lineParts[3]);
                        cmdData.setResponse(argBytes);
                        // fifth field is the completion code
                        if (lineParts.length >= 5) {
                            argBytes = getHexBytes(lineParts[4]);
                            cmdData.setCompletionCode(argBytes[0]);
                            // sixth field is the delay before the next cmd
                            if ((lineParts.length >= 6)
                                    && !"".equals(lineParts[5])) {
                                cmdData.setNextCmdDelay(-Integer
                                        .parseInt(lineParts[5]));
                            } else {
                                cmdData.setNextCmdDelay(DEFAULT_TIME_BEFORE_NEXT_CMD);
                            }
                        }
                    }
                }
                cmdList.add(cmdData);
            } catch (Exception e) {
                System.out.println("Unable to process line: --" + nextLine
                        + "-- " + e.getMessage());
            }
        }

        return cmdList;

    }

    /**
     * Read in commands from a stream and create a list of command data
     * 
     * @param stream
     *            input source with command data
     * @return parsed list of commands
     * @throws FileNotFoundException
     */
    public List<RS232CmdData> readDataFile(InputStream stream)
            throws FileNotFoundException {
        Scanner scanner = new Scanner(stream);
        return readData(scanner);
    }

    /**
     * Read in commands from a file and create a list of command data
     * 
     * @param file
     *            input source with command data
     * @return parsed list of commands
     * @throws FileNotFoundException
     */
    public List<RS232CmdData> readDataFile(File file)
            throws FileNotFoundException {

        Scanner scanner = new Scanner(file);
        return readData(scanner);
    }

    /**
     * Converts a hex string into the associated byte array
     * 
     * @param str
     *            hex formatted string
     * @return actual hex values represented by the string
     */
    private byte[] getHexBytes(String str) {
        byte[] hexBytes = null;
        int arg = Integer.parseInt(str, 16);
        if (arg > 255) {
            hexBytes = new byte[2];
            hexBytes[0] = (byte) (arg % 255);
            hexBytes[1] = (byte) (arg / 255);
        } else {
            hexBytes = new byte[1];
            hexBytes[0] = (byte) arg;
        }
        return hexBytes;
    }

}
