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
 * Classes that are interested in receiving parsed response data should
 * implement this interface. They will be notified when a command response
 * has been parsed.
 * @author bradnorris
 *
 */
public interface RS232ResponseListener {

    /**
     * Callback method when a command response has been parsed.
     * @param response
     */
	public void handleResponse(RS232CmdData response);
}
