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

import com.onetouchis.camctrl.util.Utils;

/**
 * This class defines all commands, parameters, and responses. Each
 * implementation should add the command/response information into a commands
 * file that is read in at runtime to define the command data. There should be
 * an instance of this class for each supported command listed below.
 * 
 * @author bradnorris
 * 
 */
public class RS232CmdData {

    public static final String IDENTIFIER = "IDENTIFIER";
    public static final String VIDEO_ON = "VIDEO_ON";
    public static final String VIDEO_OFF = "VIDEO_OFF";
    public static final String AUTO_FOCUS = "AUTO_FOCUS";
    public static final String MANUAL_FOCUS = "MANUAL_FOCUS";
    public static final String SHUTTER_SPEED = "SHUTTER_SPEED";
    public static final String MANUAL_WHITE_BALANCE = "MANUAL_WHITE_BALANCE";
    public static final String AUTO_WHITE_BALANCE = "AUTO_WHITE_BALANCE";
    public static final String INITIAL_PARAM = "INITIAL_PARAM";
    public static final String PRESET_STORE = "PRESET_STORE";
    public static final String SET_PRESET_POSITION = "SET_PRESET_POSITION";
    public static final String READ_PRESET_POSITION = "READ_PRESET_POSITION";
    public static final String DOC_POSITION_STORE = "DOC_POSITION_STORE";
    public static final String SET_DOC_POSITION = "SET_DOC_POSITION";
    public static final String READ_DOC_POSITION = "READ_DOC_POSITION";
    public static final String PAN_LEFT = "PAN_LEFT";
    public static final String PAN_RIGHT = "PAN_RIGHT";
    public static final String TILT_UP = "TILT_UP";
    public static final String TILT_DOWN = "TILT_DOWN";
    public static final String ZOOM_WIDE = "ZOOM_WIDE";
    public static final String ZOOM_TELE = "ZOOM_TELE";
    public static final String FOCUS_FAR = "FOCUS_FAR";
    public static final String FOCUS_NEAR = "FOCUS_NEAR";
    public static final String PRESET_MOVE = "PRESET_MOVE";
    public static final String HOME_POSITION_DETECT = "HOME_POSITION_DETECT";
    public static final String HOME_POSITION_MOVE = "HOME_POSITION_MOVE";
    public static final String DOC_POSITION_MOVE = "DOC_POSITION_MOVE";
    public static final String ABSOLUTE_COORD_MOVE = "ABSOLUTE_COORD_MOVE";
    public static final String RELATIVE_COORD_MOVE = "RELATIVE_COORD_MOVE";
    public static final String PAN_LEFT_START = "PAN_LEFT_START";
    public static final String DIRECT_PAN_SPEED_SETTING = "DIRECT_PAN_SPEED_SETTING";
    public static final String PAN_SPEED_INCREASE = "PAN_SPEED_INCREASE";
    public static final String PAN_SPEED_DECREASE = "PAN_SPEED_DECREASE";
    public static final String DIRECT_TILT_SPEED_SETTING = "DIRECT_TILT_SPEED_SETTING";
    public static final String TILT_SPEED_INCREASE = "TILT_SPEED_INCREASE";
    public static final String TILT_SPEED_DECREASE = "TILT_SPEED_DECREASE";
    public static final String DIRECT_ZOOM_SPEED_SETTING = "DIRECT_ZOOM_SPEED_SETTING";
    public static final String ZOOM_SPEED_INCREASE = "ZOOM_SPEED_INCREASE";
    public static final String ZOOM_SPEED_DECREASE = "ZOOM_SPEED_DECREASE";
    public static final String DIRECT_FOCUS_SPEED_SETTING = "DIRECT_FOCUS_SPEED_SETTING";
    public static final String TILT_UP_START = "TILT_UP_START";
    public static final String PAN_TILT_STOP = "PAN_TILT_STOP";
    public static final String ZOOM_WIDE_START = "ZOOM_WIDE_START";
    public static final String ZOOM_TELE_START = "ZOOM_TELE_START";
    public static final String ZOOM_STOP = "ZOOM_STOP";
    public static final String PAN_RIGHT_START = "PAN_RIGHT_START";
    public static final String READ_DIRECT_PAN_SPEED = "READ_DIRECT_PAN_SPEED";
    public static final String READ_DIRECT_TILT_SPEED = "READ_DIRECT_TILT_SPEED";
    public static final String READ_DIRECT_ZOOM_SPEED = "READ_DIRECT_ZOOM_SPEED";
    public static final String READ_DIRECT_FOCUS_SPEED = "READ_DIRECT_FOCUS_SPEED";
    public static final String TILT_DOWN_START = "TILT_DOWN_START";
    public static final String CPU_SOFTWARE_RESET = "CPU_SOFTWARE_RESET";
    public static final String FOCUS_FAR_START = "FOCUS_FAR_START";
    public static final String FOCUS_NEAR_START = "FOCUS_NEAR_START";
    public static final String FOCUS_STOP = "FOCUS_STOP";
    public static final String SERIAL_SPEED = "SERIAL_SPEED";
    public static final String BACK_LIGHT_SETTING = "BACK_LIGHT_SETTING";
    public static final String WHITE_BALANCE_HOLD = "WHITE_BALANCE_HOLD";
    public static final String POWER_SAVE_ON = "POWER_SAVE_ON";
    public static final String POWER_SAVE_OFF = "POWER_SAVE_OFF";
    public static final String LED_CONTROL = "LED_CONTROL";
    public static final String MOTION_DETECT_ON_OFF = "MOTION_DETECT_ON_OFF";
    public static final String ZOOM_SPEED_SETTING = "ZOOM_SPEED_SETTING";
    public static final String REMOTE_CONTROL_ON_OFF = "REMOTE_CONTROL_ON_OFF";
    public static final String PAN_DIR_REVERSE = "PAN_DIR_REVERSE";
    public static final String PAN_DIR_NORMAL = "PAN_DIR_NORMAL";
    public static final String CAMERA_MODE_CHANGE = "CAMERA_MODE_CHANGE";
    public static final String CUSTOM_CODE_0 = "CUSTOM_CODE_0";
    public static final String CUSTOM_CODE_1 = "CUSTOM_CODE_1";
    public static final String PAN_SPEED_SETTING = "PAN_SPEED_SETTING";
    public static final String TILT_SPEED_SETTING = "TILT_SPEED_SETTING";
    public static final String READ_STATUS_OF_MOTION = "READ_STATUS_OF_MOTION";
    public static final String READ_CAMERA_STATUS = "READ_CAMERA_STATUS";
    public static final String READ_CUSTOM_CODE_0 = "READ_CUSTOM_CODE_0";
    public static final String READ_CUSTOM_CODE_1 = "READ_CUSTOM_CODE_1";
    public static final String READ_ABSOLUTE_COORD = "READ_ABSOLUTE_COORD";
    public static final String READ_PREVIOUS_COMMAND = "READ_PREVIOUS_COMMAND";
    public static final String READ_BACK_LIGHT = "READ_BACK_LIGHT";
    public static final String READ_WHITE_BALANCE = "READ_WHITE_BALANCE";
    public static final String READ_PAN_TILT_SPEED = "READ_PAN_TILT_SPEED";
    public static final String READ_ZOOM_SPEED = "READ_ZOOM_SPEED";
    public static final String READ_MODEL_NAME = "READ_MODEL_NAME";
    public static final String READ_MOTION_DETECT = "READ_MOTION_DETECT";
    public static final String PRESET_BUTTON_1 = "PRESET_BUTTON_1";
    public static final String PRESET_BUTTON_2 = "PRESET_BUTTON_2";
    public static final String PRESET_BUTTON_3 = "PRESET_BUTTON_3";
    public static final String PRESET_BUTTON_4 = "PRESET_BUTTON_4";
    public static final String PRESET_BUTTON_5 = "PRESET_BUTTON_5";
    public static final String PRESET_BUTTON_6 = "PRESET_BUTTON_6";
    public static final String PRESET_BUTTON_7 = "PRESET_BUTTON_7";
    public static final String PRESET_BUTTON_8 = "PRESET_BUTTON_8";
    public static final String PRESET_BUTTON_9 = "PRESET_BUTTON_9";
    public static final String PRESET_BUTTON_10 = "PRESET_BUTTON_10";
    public static final String SET_PRESET_1 = "SET_PRESET_1";
    public static final String SET_PRESET_2 = "SET_PRESET_2";
    public static final String SET_PRESET_3 = "SET_PRESET_3";
    public static final String SET_PRESET_4 = "SET_PRESET_4";
    public static final String SET_PRESET_5 = "SET_PRESET_5";
    public static final String SET_PRESET_6 = "SET_PRESET_6";
    public static final String SET_PRESET_7 = "SET_PRESET_7";
    public static final String SET_PRESET_8 = "SET_PRESET_8";
    public static final String SET_PRESET_9 = "SET_PRESET_9";
    public static final String SET_PRESET_10 = "SET_PRESET_10";
    public static final String RESPONSE_SUCESS = "RESPONSE_SUCESS";
    public static final String RESPONSE_FAIL = "RESPONSE_FAIL";
    public static final String TV_INIT = "TV_INIT";
    public static final String TV_INPUT_1 = "TV_INPUT_1";
    public static final String TV_POWER_ON = "TV_POWER_ON";
    public static final String TV_POWER_OFF = "TV_POWER_OFF";

    /** name from static strings listed previously in this class */
    private String name;

    /** single byte that is the actual command to the camera */
    private byte code;

    /** additional command data / arguments */
    private byte[] argument;

    /** expected response */
    private byte[] response;
    
    /** minimum value for the argument */
    private int argumentMin;

    /** maximum value for the argument */
    private int argumentMax;
    
    /** code indicating command is complete */
    private byte completionCode;

    /** time before next command can be sent after this one */
    private int nextCmdDelay;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public byte[] getArgument() {
        return argument;
    }

    public void setArgument(byte[] argument) {
        this.argument = argument;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public byte getCompletionCode() {
        return completionCode;
    }

    public void setCompletionCode(byte completionCode) {
        this.completionCode = completionCode;
    }
   
    public int getArgumentMin() {
        return argumentMin;
    }

    public void setArgumentMin(int argumentMin) {
        this.argumentMin = argumentMin;
    }

    public int getArgumentMax() {
        return argumentMax;
    }

    public void setArgumentMax(int argumentMax) {
        this.argumentMax = argumentMax;
    }

    public int getNextCmdDelay() {
        return nextCmdDelay;
    }

    public void setNextCmdDelay(int nextCmdDelay) {
        this.nextCmdDelay = nextCmdDelay;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        byte[] byteCode = new byte[1];
        byteCode[0] = code;
        buffer.append("Cmd:: " + name + ": code: "
                + Utils.byteArrayToHexString(byteCode));

        if (argument != null) {
            buffer.append("; argurments: "
                    + Utils.byteArrayToHexString(argument));
        } else {
            buffer.append("; no arguments");
        }

        if (!RESPONSE_SUCESS.equals(name) && !RESPONSE_FAIL.equals(name)) {
            if (response != null) {
                buffer.append("; response: "
                        + Utils.byteArrayToHexString(response));
            } else {
                buffer.append("; no response");
            }
        }

        return buffer.toString();
    }

}
