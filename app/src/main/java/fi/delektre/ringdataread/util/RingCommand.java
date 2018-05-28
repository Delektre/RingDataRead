package fi.delektre.ringdataread.util;


/**
 * Created by t2r on 08/03/18.
 */


import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import fi.delektre.ringdataread.AppConst;
// import fi.delektre.ringa.thering.RingPrefs_;

/**
 * TODO: Rewrite this class to use Factory pattern
 *
 * Created by t2r on 07/03/18.
 */


@EBean
public class RingCommand {
    private final String TAG = "RingCommand";
    public int ledRed = 0;
    public int ledGreen = 0;
    public int ledBlue = 0;
    public int ledYellow = 0;
    public int ledNIR = 0;

    private String mCommand = AppConst.RING_CMD_NOP;
    private byte mCommandId = COMMAND_NONE;

    private final static byte COMMAND_CALIBRATE = 0x12;
    private final static byte COMMAND_LED_CONFIG = 0x02;
    private final static byte COMMAND_RESET = 0x17;
    private final static byte COMMAND_NONE = 127;
    private final static byte COMMAND_REBOOT = 0x22;
    private final static byte COMMAND_START = 0x25;
    private final static byte COMMAND_NOP = 0x9;
    private final static byte COMMAND_ACK = 0x26;


    /*
    @Pref
    RingPrefs_ prefs;
*/
    public String toString() {
        return mCommand;
    }

    /**
     * Set the command code
     *
     * @param cmdCode Command literal identifying BLE command to send
     */
    public void setCommand(String cmdCode) {
        mCommand = cmdCode;
        switch (cmdCode) {
            case AppConst.RING_CMD_CALIBRATE:
                Log.d(TAG, "Command calibrate");
                mCommandId = COMMAND_CALIBRATE;
                break;
            case AppConst.RING_CMD_RESET:
                Log.d(TAG, "Command reset");
                mCommandId = COMMAND_RESET;
                break;
            case AppConst.RING_CMD_REBOOT:
                Log.d(TAG, "Command reboot");
                mCommandId = COMMAND_REBOOT;
                break;
            case AppConst.RING_CMD_START:
                Log.d(TAG, "Command start recording");
                mCommandId = COMMAND_START;
                break;
            case AppConst.RING_CMD_NOP:
                Log.d(TAG, "Command NO-OP");
                mCommandId = COMMAND_NOP;
                break;
            case AppConst.RING_CMD_ACK:
                Log.d(TAG, "Command ACK");
                mCommandId = COMMAND_ACK;
                break;
            default:
                Log.e(TAG, "Illegal command: " + cmdCode);
                mCommandId = COMMAND_NONE;
                mCommand = "N/A";
        }
    }

    /**
     * Check if our command content is ok to send, ie. other than CMD_NONE
     *
     * @return boolean If our command is other than NONE
     */
    public boolean isOk() {
        return mCommandId != COMMAND_NONE;
    }

    /*
    private void readLedConfig() {
        ledRed = prefs.redPwm().get();
        ledGreen = prefs.greenPwm().get();
        ledBlue = prefs.bluePwm().get();
        ledNIR = prefs.nirPwm().get();
        ledYellow = prefs.yellowPwm().get();
    }
    */

    /**
     * Build the BLE command byte array, ready for sending with BLE Driver.
     *
     * @return byte[] Readily compiled array ready for sending
     */
    public byte[] getSequence(int value) {
        byte[] data;
        switch (mCommandId) {
            case COMMAND_REBOOT:
                data = new byte[1];
                data[0] = COMMAND_REBOOT;
                break;
            case COMMAND_RESET:
                data = new byte[1];
                data[0] = COMMAND_RESET;
                break;
            case COMMAND_CALIBRATE:
                data = new byte[1];
                data[0] = COMMAND_CALIBRATE;
                break;
            case COMMAND_ACK:
                data = new byte[2];
                byte arvo = (byte) value;
                data[0] = (byte)(value & 0x00ff);
                data[1] = (byte)((value & 0xff00) >> 8);
                break;
            case COMMAND_LED_CONFIG:
                /**
                 * TODO: Onkohan helpompaa kirjoittaa kaikki komennot samaan characteristikkiin vai
                 * käyttää eri mittaisille eriä?
                 */
                // readLedConfig();
                data = new byte[11];
                data[0] = COMMAND_LED_CONFIG;
                data[1] = (byte) (ledRed & 0x00ff);
                data[2] = (byte) ((ledRed & 0xff00) >> 8);
                data[3] = (byte) (ledGreen & 0x00ff);
                data[4] = (byte) ((ledGreen & 0xff00) >> 8);
                data[5] = (byte) (ledBlue & 0x00ff);
                data[6] = (byte) ((ledBlue & 0xff00) >> 8);
                data[7] = (byte) (ledNIR & 0x00ff);
                data[8] = (byte) ((ledNIR & 0xff00) >> 8);
                data[9] = (byte) (ledYellow & 0x00ff);
                data[10] = (byte) ((ledYellow & 0xff00) >> 8);
                break;
            default:
                data = new byte[1];
                data[0] = COMMAND_NONE;
        }
        return data;
    }
}
