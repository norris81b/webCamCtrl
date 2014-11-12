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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.onetouchis.camctrl.util.Utils;

/**
 * This class forwards commands to a network socket. The command is mapped to
 * the associated camera command and put into a queue to be send when the camera
 * is ready. Intended use is to forward commands to a program such as "ser2net"
 * that will send the command out a serial port.
 * 
 * @author bradnorris
 * 
 */
public class RS232NetProcessor extends Thread implements IProcessor,
        RS232ResponseListener {

    private static Logger logger = Logger.getLogger(RS232NetProcessor.class
            .getName());

    private Map<String, RS232CmdData> nameToCmdMap = new HashMap<String, RS232CmdData>();

    /**
     * Host address of the remote socket connected to an RS232 port
     */
    private String remoteIpAddress;
    
    /**
     * Port id of the remote socket connected to an RS232 port
     */
    private int remotePortNumber;
    /**
     * selected socket that is associated with a serial port for
     * sending/receiving data
     */
    private Socket serialSocket;

    /** stream to RS232 device */
    private OutputStream outputStream;

    private Thread sendThread;

    private boolean running;

    private boolean portFound = false;

    private ServletContext context;

    /** queue for sending commands to the RS232 device */
    protected RS232BlockingQ queue;

    public RS232NetProcessor() throws Exception {
    }

    /**
     * Creates the network socket to send commands, loads up the commands, 
     * starts the queue for sending commands, then sends an identifier cmd.
     * @param remoteIpAddress host that will receive the cmds
     * @param remotePortNumber network port on the host that will receive cmds
     * @param commPort not used.
     */
    @Override
    public void initialize(String remoteIpAddress, int remotePortNumber, String commPort)
            throws Exception {

        // save the remote host and port in case we need to reconnect the socket
        this.remoteIpAddress = remoteIpAddress;
        this.remotePortNumber = remotePortNumber;
        
        loadCommands();
        
        setupSocket();

    }
  
    /**
     * Resets the socket connection by closing it if it's open, then connecting
     * @throws Exception
     */
    private void setupSocket() throws Exception
    {
        logger.info("Attempting to connect to host " + remoteIpAddress + " on port "
                + remotePortNumber);
        if (serialSocket != null)
        {
            serialSocket.close();
        }
        serialSocket = new Socket(remoteIpAddress, remotePortNumber);
        outputStream = serialSocket.getOutputStream();

        running = true;
        queue = new RS232BlockingQ(serialSocket, this);
        sendThread = new Thread(this);
        sendThread.start();

        RS232CmdData identifier = nameToCmdMap.get("IDENTIFIER");
        queue.offer(identifier);
        Thread.sleep(5000);

    }

    /**
     * Loads commands from a csv file in the resources directory and creates
     * a map command names to command data.
     * @throws FileNotFoundException
     */
    public void loadCommands() throws FileNotFoundException {
        File resourceDir = new File("resources");
        if (!resourceDir.exists()) {
            String commandsFile = "/resources/commands.csv";
            logger.info("Loading commands from: " + commandsFile);
            InputStream is = context.getResourceAsStream(commandsFile);

            if (is == null) {
                throw new FileNotFoundException(commandsFile);
            }
            logger.info("Reading cmds from inside jar file");
            RS232CmdFileScanner scanner = new RS232CmdFileScanner();
            List<RS232CmdData> commands = scanner.readDataFile(is);
            for (RS232CmdData data : commands) {
                nameToCmdMap.put(data.getName(), data);
            }
        } else {
            File[] files = resourceDir.listFiles();

            for (File file : files) {
                if (file.getName().endsWith("csv")) {
                    System.out.println("Reading cmds from: " + file.getName());
                    RS232CmdFileScanner scanner = new RS232CmdFileScanner();
                    List<RS232CmdData> commands = scanner.readDataFile(file);
                    for (RS232CmdData data : commands) {
                        nameToCmdMap.put(data.getName(), data);
                    }
                }
            }
        }

    }

    /**
     * Returns the command code for a given command
     * @param commandName the name of the command
     * @return
     */
    public byte getCommand(String commandName) {
        return nameToCmdMap.get(commandName).getCode();
    }

    /**
     * Adds the command data associated with the cmdStr to
     * the cms queue.
     * @param cmdStr name of the command
     */
    @Override
    public void sendDataCommand(String cmdStr) {
        RS232CmdData cmdData = nameToCmdMap.get(cmdStr);
        if (cmdData != null) {
            queue.offer(cmdData);
        } else {
            logger.warning("Could not find command: " + cmdStr);
        }
    }

    /**
     * Adds a new command data object from the cmdStr and args to
     * the cms queue.
     * @param cmdStr name of the command
     */
    @Override
    public void sendDataCommand(String cmdStr, byte[] arguments) {
        RS232CmdData cmdData = nameToCmdMap.get(cmdStr);
        if (cmdData != null) {
            cmdData.setArgument(arguments);
            queue.offer(cmdData);
        } else {
            logger.warning("Could not find command: " + cmdStr);
        }
    }

    @Override
    public void close() {
        try {
            running = false;
            if (serialSocket != null) {
                serialSocket.close();
            }
            if (queue != null) {
                queue.stopService();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception closing socket: ", e);
        }
    }

    public void run() {

        boolean connectionFailed = false;
        while (running) {
            try {
                RS232CmdData cmdData = null;
                if (queue == null) {
                    logger.warning("Waiting for queue to reset");
                    Thread.sleep(1000);
                }
                cmdData = queue.take();
                if (cmdData != null) {
                    byte code = cmdData.getCode();
                    byte[] arg = cmdData.getArgument();
                    byte[] cmd = null;
                    if (arg != null) {
                        cmd = new byte[arg.length + 1];
                        cmd[0] = code;
                        for (int i = 1; i < cmd.length; i++) {
                            cmd[i] = arg[i - 1];
                        }
                    } else {
                        cmd = new byte[1];
                        cmd[0] = code;
                    }
                    logger.info("Sending: --" + Utils.byteArrayToHexString(cmd)
                            + "--");
                    outputStream.write(cmd);
                }
            } catch (InterruptedException ie) {
                // ignore
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception sending cmd: ", e);
                logger.log(Level.WARNING, "Caused by: " + e.getCause());
                connectionFailed = true;
            }
            
            if (connectionFailed) {
                logger.warning("Retrying socket: " + remoteIpAddress + ":" + remotePortNumber);
                try {
                    close();
                    setupSocket();
                    connectionFailed = false;
                } catch (Exception e1) {
                    logger.log(Level.WARNING, "Unable to reconnect socket", e1);
                }

            }
        }
    }

    public void stopMessage() {
        running = false;
        if (sendThread != null) {
            sendThread.interrupt();
        }
    }

    public void setDelay(int delay) {
        queue.setDelay(delay);
    }

    @Override
    public void handleResponse(RS232CmdData response) {

        logger.info("Received response: " + response + "");
        if (RS232CmdData.RESPONSE_SUCESS.equals(response.getName())
                && portFound == false) {
            logger.info("Found a camera!");
            portFound = true;
        }

    }

    @Override
    public void setContext(ServletContext context) {
        this.context = context;
    }

}
