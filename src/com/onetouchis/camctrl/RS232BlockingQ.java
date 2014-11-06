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

import java.net.Socket;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This class wraps a linked blocking queue to send subsequent commands after a
 * response has been received from a previous command. An extra delay is also
 * included after the response is received. This class listens for responses to
 * commands to determine if it's ok to send the next.
 * 
 * @author bradnorris
 * 
 */
public class RS232BlockingQ implements RS232ReaderListener {

    /** time after response from previous command and next command can be sent */
    private static int WAIT_TIME_AFTER_RESPONSE = -50;

    private Logger logger = Logger.getLogger(RS232BlockingQ.class.getName());

    /** main queue this class wraps */
    private LinkedBlockingQueue<RS232CmdData> queue = new LinkedBlockingQueue<RS232CmdData>();

    /** response from camera to a command */
    private RS232CmdData cmdResponse;

    /** listens for responses from a socket */
    private RS232Reader reader;

    /** minimum time between commands */
    private int delay = 200;

    /** minimum time after reponse from previous commands has been received */
    private int waitAfterResponse = 0;

    /** last time a command was release from the queue (and sent to the camera) */
    private long lastTakeTime = 0;

    private boolean okToSend = true;

    /** response indicated a successful command was processed; basic command */
    private boolean receivedCmdAck = false;

    /**
     * response indicated a successful command was processed; cmd with
     * completion code
     */
    private boolean receivedCompletionCode = false;

    private Object lock = new Object();

    /** command responses are forwarded to this listener */
    private RS232ResponseListener responseListener;

    /**
     * This constructor sets the internal socket listener, then creates the
     * socket reader with the socket that is passed in.
     * 
     * @param socket
     *            network to be used for socket commands
     * @param responseListener
     *            listener that will received responses
     */
    public RS232BlockingQ(Socket socket, RS232ResponseListener responseListener) {
        this.responseListener = responseListener;
        reader = new RS232Reader(socket, this);
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * All commands should be enqueued using this method.
     * 
     * @param cmdData
     *            a command to enqueue and eventually send to the camera
     */
    public void offer(RS232CmdData cmdData) {
        logger.fine("Adding to queue: " + cmdData);
        queue.offer(cmdData);
    }

    /**
     * This implementation releases commands from the queue by checking two
     * conditions, first the response from a previous command must have been
     * received. Second, the minimum time since the previous command's response
     * was received must be exceeded.
     * 
     * @return
     * @throws InterruptedException
     */
    public RS232CmdData take() throws InterruptedException {
        RS232CmdData cmdData = null;

        long readyTime = getNextSendTime();
        logger.finest("Time before next cmd:  " + readyTime);
        while (!okToSend || readyTime < 0) {
            try {
                synchronized (lock) {
                    if (readyTime < 0) {
                        logger.finest("waiting for " + readyTime + " ms");
                        lock.wait(-readyTime);
                    } else if (!okToSend) {
                        logger.finest("Waiting for ok");
                        lock.wait(1000);
                        okToSend = true;
                        logger.finest("It's ok now");
                    }
                }

            } catch (InterruptedException e) {
                logger.warning("Queue interrupted");
            }
            readyTime = getNextSendTime();
        }

        cmdData = queue.take();
        lastTakeTime = System.currentTimeMillis();
        logger.fine("Queue returning: " + cmdData);

        okToSend = false;
        receivedCmdAck = false;
        receivedCompletionCode = false;
        cmdResponse = cmdData;
        return cmdData;
    }

    /**
     * The next send time is computed by taking the current system time and
     * subtracting the sum of the time of the last command and the delay between
     * commands time. If it's less than zero, we need to wait. last send time =
     * 10 delay = 2 if current time is 11, 11 - (10+2) = -1, can't send yet if
     * current time is 13, 13 - (10+2) = 1, ok to send
     * 
     * If the wait after response time is non zero, we have received a response
     * and must wait the minimum amount of time before sending another cmd.
     * 
     * @return
     */
    private long getNextSendTime() {
        long readyTime = 0;
        if (waitAfterResponse < 0) {
            readyTime = waitAfterResponse;
            waitAfterResponse = 0;
        } else {
            readyTime = System.currentTimeMillis() - (lastTakeTime + delay);
        }
        return readyTime;
    }

    public void stopService() {
        if (reader != null) {
            reader.stopService();
            synchronized (lock) {
                lock.notifyAll();
            }
        }

    }

    /**
     * Callback method when a response is received by the RS232 reader
     * 
     * @param message
     *            the raw response
     */
    @Override
    public void handleEvent(byte[] message) {

        RS232CmdData response = new RS232CmdData();
        if (message.length >= 1) {
            response.setCode(message[0]);
        }

        if (message.length > 1) {
            byte[] args = new byte[message.length - 1];
            System.arraycopy(message, 1, args, 0, message.length - 1);
            response.setArgument(args);
        }

        byte[] expectedResp = new byte[1];
        byte completionCode;
        if (cmdResponse != null) {
            expectedResp = cmdResponse.getResponse();
            completionCode = cmdResponse.getCompletionCode();
        } else {
            expectedResp = message;
            completionCode = (byte) 0xBB;
        }

        if (Arrays.equals(expectedResp, message)) {
            logger.fine("Received ACK from last Command");
            receivedCmdAck = true;
        }

        if ((message.length == 1 && message[0] == completionCode)
                || (completionCode == 0)) {
            receivedCompletionCode = true;
        }

        if (receivedCmdAck && receivedCompletionCode) {
            cmdResponse = null;
            waitAfterResponse = WAIT_TIME_AFTER_RESPONSE;
            response.setName(RS232CmdData.RESPONSE_SUCESS);
            okToSend = true;
            logger.fine("Received response and completion code for last cmd");
        }

        if (message[0] == 0xB4) {
            logger.warning("Received NACK from last Command");
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                logger.finest("Interrupted from NACK wait");
            }
            response.setName(RS232CmdData.RESPONSE_FAIL);
            okToSend = true;
        }

        if (okToSend) {
            responseListener.handleResponse(response);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

}
