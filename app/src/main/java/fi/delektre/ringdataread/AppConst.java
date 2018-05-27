package fi.delektre.ringdataread;


import java.util.UUID;

/**
 * Created by t2r on 08/03/18.
 */

public class AppConst {
    public final String KEY_MODEL = "key_model";
    public final String KEY_STATUS = "key_status";
    public final String KEY_TITLE = "key_title";
    public final String KEY_ID = "key_id";
    public final String KEY_IS_BOOLEAN = "key_boolean";
    public final String KEY_MESSENGER = "key_messenger";
    public final String KEY_ERROR = "key_error";

    public final String ACTION_CONNECT = "connect_req";

    public final String KEY_PREF = "pref_name";

    public final String APPLICATION_HOST_SELECTION = "APPLICATION_HOST_SELECTION";

    public final String APPLICATION_PREF_CHANGED = "pref_changed";
    public final String BLE_ERROR_OCCURED = "ble_error";

    public final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 69;

    public final static String RING_CMD_RESET = "reset-ring";
    public final static String RING_CMD_CALIBRATE = "calibrate-ring";
    public final static String RING_CMD_NOP = "no-operation";
    public final static String RING_CMD_REBOOT = "reboot-ring";
    public final static String RING_CMD_SETDATE = "set-time";
    public final static String RING_CMD_START = "start-recording";


    public static final String RING_SERVICE = "19B10000-E8F2-537E-4F6C-D104768A1214";
    public static final String DATA_CHARACTERISTIC = "19B10005-E8F2-537E-4F6C-D104768A1214";
    public static final String CMD_CHARACTERISTIC = "19B10010-E8F2-537E-4F6C-D104768A1214";
    public static final String LED_CHARACTERISTIC = "19B10001-E8F2-537E-4F6C-D104768A1214";
        /*
        const val TEMP1_CHARACTERISTIC = "19B10020-E8F2-537E-4F6C-D104768A1214"
        const val STEP_CHARACTERISTIC = "19B10010-E8F2-537E-4F6C-D104768A1214"
        const val OPTO_CHARACTERISTIC = "19B10030-E8F2-537E-4F6C-D104768A1214"
*/
    //const val STEP_TIME_CHARACTERISTIC = "19B10003-E8F2-537E-4F6C-D104768A1214"
    //const val MEASURE_TIME_CHARACTERISTIC = "19B10005-E8F2-537E-4F6C-D104768A1214"


    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_RING_SERVICE = UUID.fromString(RING_SERVICE);
    public static final UUID UUID_DATA_CHARACTERISTIC = UUID.fromString(DATA_CHARACTERISTIC);
    public static final UUID UUID_LED_CHARACTERISTIC = UUID.fromString(LED_CHARACTERISTIC);
    public static final UUID UUID_CMD_CHARACTERISTIC = UUID.fromString(CMD_CHARACTERISTIC);
        /*
              val UUID_TEMP1_CHARACTERISTIC: UUID = UUID.fromString(TEMP1_CHARACTERISTIC)
              val UUID_STEP_CHARACTERISTIC: UUID = UUID.fromString(STEP_CHARACTERISTIC)

              val UUID_OPTO_CHARACTERISTIC: UUID = UUID.fromString(OPTO_CHARACTERISTIC)
      */
    //val UUID_STEP_TIME_CHARACTERISTIC: UUID = UUID.fromString(STEP_TIME_CHARACTERISTIC)
    //val UUID_DATA_TIME_CHARACTERISTIC: UUID = UUID.fromString(MEASURE_TIME_CHARACTERISTIC)

    public final String BLE_MSG_MEASURE_VALUE = "value_measure";
    public final String BLE_MSG_MEASURE_TIME = "time_measure";
    public final String BLE_MSG_STEP_VALUE = "value_step";
    public final String BLE_MSG_STEP_TIME = "time_step";
    public final String BLE_MSG_LED_VALUE = "value_led";

    public final int REQUEST_ENABLE_BT = 42;

    public final String ACTION_GATT_CONNECTED = "fi.delektre.ringa.ACTION_GATT_CONNECTED";
    public final String ACTION_GATT_DISCONNECTED = "fi.delektre.ringa.ACTION_GATT_DISCONNECTED";
    public final String ACTION_GATT_SERVICES_DISCOVERED = "fi.delektre.ringa.ACTION_GATT_SERVICES_DISCOVERED";
    public final String ACTION_GATT_DATA_AVAILABLE = "fi.delektre.ringa.ACTION_GATT_DATA_AVAILABLE";
    public final String EXTRA_DATA = "fi.delektre.ringa.EXTRA_DATA";

    public final int STATE_DISCONNECTED = 0;
    public final int STATE_DISCONNECTING = 1;
    public final int STATE_CONNECTING = 2;
    public final int STATE_CONNECTED = 3;

    public final String DATA_TEMP1 = "Temp1";
    public final String DATA_TEMP2 = "Temp2";
    public final String DATA_LED_RED = "Led_Red";
    public final String DATA_LED_GREEN = "Led_Green";
    public final String DATA_LED_BLUE = "Led_Blue";
    public final String DATA_LED_NIR = "Led_NIR";
    public final String DATA_LED_YELLOW = "Led_Yellow";
    /*
    const val DATA_LED_UVA = "Led_uva"
    const val DATA_LED_UVB = "Led_uvb"
    */
    public final String DATA_LED_ORANGE = "Led_Orange";
    public final String DATA_STEPS = "Steps";

    public final String CALLBACK_REF = "callback_ref";

    public final String KEY_BLE_ADDR = "ble_addr";
    public final String KEY_BLE_NAME = "ble_name";
    public final String KEY_USER_ID = "user_email"; // used to identify user

    public final String KEY_BLE_SERVICE = "key_ble_service";
    public final String KEY_BLE_CHARACTERISTIC = "key_ble_characteristic";

    public final String PKG_TYPE = "intent_type";
    public final String PKG_DATA = "data_package";
    public final String PKG_CTRL = "data_control";

    // indicator led state
    public final String KEY_LED_STATE = "led_state";

    // PWM configuration values
    public final String KEY_PWM_LED_RED = "led_red";
    public final String KEY_PWM_LED_GREEN = "led_green";
    public final String KEY_PWM_LED_BLUE = "led_blue";
    public final String KEY_PWM_LED_YELLOW = "led_yellow";
    public final String KEY_PWM_LED_NIR = "led_nir";
    public final String KEY_PWM_LED_UVA = "led_uva";
    public final String KEY_PWM_LED_UVB = "led_uvb";
    public final String KEY_PWM_LED_ORANGE = "led_orange";
    public final String KEY_ENABLE_SYNC = "enable_sync";
    public final String KEY_ENABLE_LOCATION = "enable_location";
}