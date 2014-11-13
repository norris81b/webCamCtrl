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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onetouchis.camctrl.util.Utils;

/**
 * This servlet accepts web requests for camera commands. Once a command is
 * complete, it is forwarded to a camera processor.
 * 
 * @author bradnorris
 * 
 */
public class CamCtrlServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(CamCtrlServlet.class
            .getName());

    private static final long serialVersionUID = 1L;

    private static final int MAX_PRESETS = 9;

    /** default ip address when using a network based processor */
    private static final String DEFAULT_RS232_HOST = "192.168.1.148";

    /** default port when using a network based processor */
    private static final int DEFAULT_CAM_PORT = 3002;

    /** default port name when using COMM based processor */
    private static final String DEFAULT_COMM_PORT = "/dev/ttyS0";

    /** internal thread for scanning preset positions */
    private static ScanRunner scanThread;

    /** handles the processing of the camera commands */
    private static IProcessor processor;

    /** user wants to change a preset; next cmd will have the new preset info */
    private static boolean setPreset = false;

    private static boolean initialized = false;

    /** when scanning presets, this is how long to stay at each position */
    private static long presetScanInterval = 5000;

    private Gson gson = new Gson();

    private JsonParser parser = new JsonParser();

    /**
     * Creates the processor that will handle camera commands.
     */
    public void setupProcessor() {
        try {
            presetScanInterval = Long.getLong("wcc.preset.interval",
                    presetScanInterval);
            String rs232Host = System.getProperty("wcc.rs232.net.host",
                    DEFAULT_RS232_HOST);
            int rs232IpPort = Integer.getInteger("wcc.rs232.net.port",
                    DEFAULT_CAM_PORT);
            String processorClass = System.getProperty("wcc.rs232.processor",
                    "com.onetouchis.camctrl.RS232NetProcessor");
            logger.info("Creating IProcessor class: " + processorClass);
            Class<?> procClass = Class.forName(processorClass);
            processor = (IProcessor) procClass.newInstance();
            String rs232CommPort = System.getProperty("wcc.rs232.comm.port",
                    DEFAULT_COMM_PORT);
            processor.setContext(getServletContext());
            processor.initialize(rs232Host, rs232IpPort, rs232CommPort);
            initialized = true;
        } catch (Exception e) {
            logger.error("Unable to create RS232NetProcessor. ", e);
        }
    }

    /**
     * Handle Json formatted Camera Commands here.
     * 
     * @param request
     *            Http request from client
     * @param response
     *            Http response to client
     */
    public void processRequest(HttpServletRequest request,
            HttpServletResponse response) {

        PrintWriter out;
        response.setContentType("text/json");
        
        String userAgent = request.getHeader("User-Agent");
        logger.debug("Received a command with user agent in header: " + userAgent);

        try {
            if (!initialized) {
                setupProcessor();
            }

            out = response.getWriter();
            String json = request.getParameter("json");

            JsonElement element = parser.parse(json);
            logger.debug("JSON object is a " + element.getClass().getName());
            String cmd = "unknown";
            String args = "";
            JsonObject result = new JsonObject();
            if (element instanceof JsonObject) {
                JsonObject obj = (JsonObject) element;
                cmd = obj.get("command").getAsString();
                if (cmd.contains("::")) {
                    String tempCmd[] = cmd.split("::");
                    cmd = tempCmd[0];
                    args = tempCmd[1];
                }
                logger.info("Command: " + cmd);
                if ("PRESETS".equals(cmd)) {
                    // client is asking for all of the preset information
                    String presetsFile = System.getProperty("jboss.home.dir")
                            + File.separator + "bin" + File.separator
                            + "presets.json";
                    logger.info("Loading presets from: " + presetsFile);
                    String jsonStr = Utils.loadFile(presetsFile);
                    logger.debug("Returning json string: " + jsonStr);
                    result.addProperty("PRESET_DATA", jsonStr);
                } else if (!setPreset && cmd.matches("[0-9]+")) {
                    // client wants to move to a particular preset position
                    byte[] cmdArray = cmd.getBytes();
                    cmdArray[0] = (byte) (cmdArray[0] - (byte) 48);
                    logger.info("Sending preset move: " + cmd);
                    processor.sendDataCommand("PRESET_MOVE", cmdArray);
                } else if (setPreset && cmd.matches("[0-9]+")) {
                    // client is updating the position of a preset
                    // String[] cmdParts = cmd.split("::");
                    byte[] cmdArray = cmd.getBytes();
                    cmdArray[0] = (byte) (cmdArray[0] - (byte) 48);
                    String jsonStr = updatePresetData(cmdArray[0], args);
                    result.addProperty("PRESET_DATA", jsonStr);
                    setPreset = false;
                    logger.info("Setting preset: " + cmdArray);
                    processor.sendDataCommand("PRESET_STORE", cmdArray);
                } else if ("PRESET_STORE".equals(cmd)) {
                    // client is about to update a preset's position
                    setPreset = true;
                } else if ("SCAN_PRESETS".equals(cmd)) {
                    try {
                        if ("ON".equals(args)) {
                            logger.info("starting scanning");
                            if (scanThread != null)
                            {
                                scanThread.destroy();
                            }
                            scanThread = new ScanRunner();
                            Thread t = new Thread(scanThread);
                            t.start();
                        } else if ("OFF".equals(args)) {
                            logger.info("stopping the scan thread");
                            if (scanThread != null) {
                                scanThread.destroy();
                                scanThread = null;
                            }
                        } else {
                            logger.warn("Received scan cmd with no args: "
                                    + cmd);
                        }
                    } catch (Exception e) {
                        logger.warn("Unable to process scan command: " + cmd, e);
                    }
                } else {
                    // all other commands are sent directly to the processor
                    logger.info("Sending cmd: " + cmd);
                    processor.sendDataCommand(cmd);
                }
            }

            result.addProperty("message", "JSON command is " + cmd);

            String jsonResult = gson.toJson(result);
            out.println(jsonResult);
            out.flush();
            out.close();

        } catch (Exception e) {
            logger.warn("Exception processing http request ", e);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     * @throws ServletException
     *             if a servlet-specific error occurs
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     * @throws ServletException
     *             if a servlet-specific error occurs
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * 
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Web Camera Controller";
    }

    @Override
    public void destroy() {
        if (scanThread != null) {
            logger.info("Stopping preset scanner: " + scanThread);
            scanThread.destroy();
            scanThread = null;
        }
        logger.info("Shutting down socket");
        processor.close();
    }

    private String updatePresetData(byte presetNum, String text)
            throws IOException {
        int number = (int) presetNum;
        logger.info("Updating preset " + number + " with text " + text);
        return Utils.updatePresetFile(number, text);
    }

    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    /**
     * This internal thread class is used to periodically move from preset to
     * preset automatically when "scanning presets" is active.
     * 
     * @author bradnorris
     * 
     */
    static class ScanRunner implements Runnable {

        private boolean scanning = true;

        private static final Object lock = new Object();

        @Override
        public void run() {
            while (scanning) {
                // continuously loop through all presets
                try {
                    synchronized (lock) {
                        for (int i = 0; i <= MAX_PRESETS && scanning; i++) {
                            byte[] cmdArray = new byte[1];
                            cmdArray[0] = (byte) i;
                            logger.info("Sending preset move: " + i
                                    + " in thread: " + this);
                            processor.sendDataCommand("PRESET_MOVE", cmdArray);
                            logger.info("sleeping");
                            lock.wait(presetScanInterval);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.warn("Exiting scanning function " + e.getMessage());
                }
            }
            logger.info("Exiting scan thread: " + this);
        }

        public void destroy() {
            scanning = false;
            synchronized (lock) {
                logger.info("Notifying scan thread to stop");
                lock.notifyAll();
            }
        }

    }

}
