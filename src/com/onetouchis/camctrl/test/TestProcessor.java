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
package com.onetouchis.camctrl.test;

import javax.servlet.ServletContext;

import com.onetouchis.camctrl.IProcessor;
import com.onetouchis.camctrl.util.Utils;

/**
 * Test class for testing user interactions. Lets a developer work on
 * the GUI's without having a back end socket or comm port setup. It
 * just prints the parameter values when called.
 * @author bradnorris
 *
 */
public class TestProcessor implements IProcessor {

	@Override
	public void sendDataCommand(String cmdStr) {
		System.out.println("Testing send data command: " + cmdStr);

	}

	@Override
	public void sendDataCommand(String cmdStr, byte[] arguments) {
		System.out.println("Testing send data command: " + cmdStr + 
		        "; with args: " + Utils.byteArrayToHexString(arguments));
	}

	@Override
	public void close() {
		System.out.println("Closed");
	}

	@Override
	public void initialize(String ipAddress, int portNumber, String commPort) throws Exception {
		System.out.println("Testing initializing with ipAddress: " + ipAddress + 
				"; port " + portNumber + "; and comm port: " + commPort);
		
	}

	@Override
	public void setContext(ServletContext context) {
		System.out.println("Testing setting context: " + context);
		
	}

}
