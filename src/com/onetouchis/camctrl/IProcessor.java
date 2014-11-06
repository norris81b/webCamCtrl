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

import javax.servlet.ServletContext;

/**
 * An implementation of this interface will be used by the
 * <code>CamCtrlSerlet</code> to process camera commands. The implementation
 * class can sends commands to any camera any way it likes. The actual commands
 * are determined by the a csv file in a "resources" directory (somewhere in the
 * classpath). A default command set is provided in project's
 * resources/commands.csv file.
 * 
 * @author bradnorris
 * 
 */
public interface IProcessor {

    /**
     * Used generically to initialize a socket or a comm port. It is assumed
     * that the ipAddress and portNumber OR the commPort will be used, but
     * that's up to the implementation to decide.
     * 
     * @param ipAddress
     *            host address if using a socket solution to send camera
     *            commands
     * @param portNumber
     *            network port number if using a socket solution to send camera
     *            commands
     * @param commPort
     *            Comm port identifier if using a direct RS232 port solution to
     *            send camera commands
     * @throws Exception
     */
    public void initialize(String ipAddress, int portNumber, String commPort)
            throws Exception;

    /**
     * Used to send a command string to the camera command processor.
     * 
     * @param cmdStr
     *            a single camera command as defined by the commands.cvs file.
     */
    public void sendDataCommand(String cmdStr);

    /**
     * Used to send a command string and a list of arguments to the camera
     * command processor
     * 
     * @param cmdStr
     *            cmdStr a single camera command as defined by the commands.cvs
     *            file.
     * @param arguments
     *            arguments associated with the camera command
     */
    public void sendDataCommand(String cmdStr, byte[] arguments);

    /**
     * Used to pass the servlet context from the servlet to the camera command
     * processor allowing the processor access to resources.
     * 
     * @param context
     *            context of the servlet
     */
    public void setContext(ServletContext context);

    /**
     * Used to stop all running threads, such as a socket listener.
     */
    public void close();
}
