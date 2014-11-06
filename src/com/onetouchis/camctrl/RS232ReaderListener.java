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

/**
 * Classes that would like to be notified when data has been received at the
 * socket, should implement this interface and be passed to the
 * <code>RS232Reader</code>.
 * 
 * @author bradnorris
 * 
 */
public interface RS232ReaderListener {

    /**
     * Callback method when data is received from the socket.
     * 
     * @param message
     *            data from socket
     */
    public void handleEvent(byte[] message);
}
