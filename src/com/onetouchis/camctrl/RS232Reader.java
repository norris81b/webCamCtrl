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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.onetouchis.camctrl.util.Utils;

/**
 * This class listens for and notifies it's listener when data has been received
 * from the socket.
 * 
 * @author bradnorris
 * 
 */
public class RS232Reader extends Thread {

    private Logger logger = Logger.getLogger(RS232Reader.class.getName());

    /** stream for receiving data from the RS232 device */
    private BufferedInputStream inputStream;

    private Thread readThread;

    private boolean running = true;

    private RS232ReaderListener listener;

    /** queue for receiving input from the RS232 device */
    protected LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

    /**
     * Listens for data from the socket and notifies the listener
     * 
     * @param serialSocket
     *            serial response data will come from this socket
     * @param listener
     *            will be notified when data is received from the socket
     */
    public RS232Reader(Socket serialSocket, RS232ReaderListener listener) {

        this.listener = listener;
        try {
            inputStream = new BufferedInputStream(serialSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        readThread = new Thread(this);
        readThread.start();

    }

    /**
     * Stops the listening thread that is normally blocked while waiting for
     * input.
     */
    public void stopService() {
        running = false;
        readThread.interrupt();
    }

    /**
     * Thread that reads from the input stream of the socket. Normally this
     * thread is blocked waiting for input. Data is sent to the listener.
     */
    @Override
    public void run() {
        while (running) {
            try {
                byte[] readBuffer = new byte[20];

                try {
                    int len = 0;

                    len = inputStream.read(readBuffer);

                    if (len > 0) {
                        byte conditionedData[] = new byte[len];

                        // We have had problems with the data being padded, so
                        // we
                        // use a separate byte array just to make sure the data
                        // is not tainted.
                        for (int i = 0; i < len; i++) {
                            conditionedData[i] = readBuffer[i];
                        }

                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("received input: -- "
                                    + Utils.byteArrayToHexString(conditionedData)
                                    + " ---");
                        }
                        listener.handleEvent(conditionedData);

                    }
                } catch (IOException e) {
                    if (!running) {
                        logger.info("Stopping inbound queue");
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Exception: ", ex);
                break;
            }
        }
    }
}
