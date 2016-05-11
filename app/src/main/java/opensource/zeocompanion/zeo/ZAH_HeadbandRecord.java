package opensource.zeocompanion.zeo;

import android.database.Cursor;
import com.myzeo.android.api.data.ZeoDataContract;

// ZeoApp Headband record
public class ZAH_HeadbandRecord {
    // member variables
    public long rID = -1;                        // note this IS the master Headband ID
    public long rCreated_timestamp = 0;
    public long rUpdated_timestamp = 0;
    public int rAlgorithm_mode = 0;
    public boolean rBonded_to_device = false;
    public long rClock_offset = 0;
    public boolean rConnected_to_device = false;
    public boolean rDocked = false;
    public boolean rOnHead = false;
    public String rBluetooth_address = null;
    public String rBluetooth_friendly_name = null;
    public String rSoftware_version = null;

    // the following fields are part of the "extended" and full headband record stored in the Zep App database
    public boolean mHasExtended = false;
    public boolean rActive_forced = false;
    public boolean rBluetooth_locked = false;
    public boolean rDemo_mode = false;
    public int rFlash_calibration_updates = 0;
    public int rFlash_saved_data_updates = 0;
    public int rFlash_sleep_backup_updates = 0;
    public String rHardware_version = null;
    public int rLast_alarm_reason = 0;
    public long rLast_battery_died_timestamp = 0;
    public long rLast_bond_timestamp = 0;
    public long rLast_connected_timestamp = 0;
    public long rLast_disconnected_timestamp = 0;
    public long rLast_docked_timestamp = 0;
    public long rLast_factory_reset_timestamp = 0;
    public long rLast_offhead_timestamp = 0;
    public long rLast_onhead_timestamp = 0;
    public long rLast_sensor_use_reset_timestamp = 0;
    public long rLast_unbonded_timestamp = 0;
    public long rLast_undocked_timestamp = 0;
    public String rModel = null;
    public boolean rNeed_clock_offset= false;
    public boolean rNeed_time_sync= false;
    public boolean rRequires_pin = false;
    public long rSensor_used = 0;
    public String rSerial = null;
    public int rVoltage = 0;
    public int rVoltage_status = 0;
    public boolean rWas_charged = false;

    public static final String ACTIVE_FORCED = "active_forced";
    public static final String BLUETOOTH_LOCKED = "bluetooth_locked";
    public static final String DEMO_MODE = "demo_mode";
    public static final String FLASH_CALIB_UPDATES = "flash_calibration_updates";
    public static final String FLASH_SAVED_DATA_UPDATES = "flash_saved_data_updates";
    public static final String FLASH_SLEEP_BACKUP_UPDATES = "flash_sleep_backup_updates";
    public static final String HARDWARE_VERSION = "hardware_version";
    public static final String LAST_ALARM_REASON = "last_alarm_reason";
    public static final String LAST_BATTERY_DIED_TIMESTAMP = "last_battery_died_timestamp";
    public static final String LAST_BOND_TIMESTAMP = "last_bond_timestamp";
    public static final String LAST_CONNECTED_TIMESTAMP = "last_connected_timestamp";
    public static final String LAST_DISCONNECTED_TIMESTAMP = "last_disconneted_timestamp";
    public static final String LAST_DOCKED_TIMESTAMP = "last_docked_timestamp";
    public static final String LAST_FACTORY_RESET_TIMESTAMP = "last_factory_reset_timestamp";
    public static final String LAST_OFFEHAD_TIMESTAMP = "last_offhead_timestamp";
    public static final String LAST_ONHEAD_TIMESTAMP = "last_onhead_timestamp";
    public static final String LAST_SENSOR_USE_RESET_TIMESTAMP = "last_sensor_use_reset";
    public static final String LAST_UNBONDED_TIMESTAMP = "last_unbond_timestamp";
    public static final String LAST_UNDOCKED_TIMESTAMP = "last_undocked_timestamp";
    public static final String MODEL = "model";
    public static final String NEED_CLOCK_OFFSET = "need_clock_offset";
    public static final String NEED_TIME_SYNC = "need_time_sync";
    public static final String REQUIRES_PIN = "requires_pin";
    public static final String SENSOR_USED = "sensor_used";
    public static final String SERIAL_NUMBER = "serial";
    public static final String VOLTAGE = "voltage";
    public static final String VOLTAGE_STATUS = "voltage_status";
    public static final String WAS_CHARGED = "was_charged";


    // member constants and other static content
    public static final String[] ZAH_HEADBANDREC_COLS = new String[] {
            ZeoDataContract.Headband._ID,
            ZeoDataContract.Headband.CREATED_ON,
            ZeoDataContract.Headband.UPDATED_ON,
            ZeoDataContract.Headband.ALGORITHM_MODE,
            ZeoDataContract.Headband.BLUETOOTH_ADDRESS,
            ZeoDataContract.Headband.BLUETOOTH_FRIENDLY_NAME,
            ZeoDataContract.Headband.BONDED,
            ZeoDataContract.Headband.CLOCK_OFFSET,
            ZeoDataContract.Headband.CONNECTED,
            ZeoDataContract.Headband.DOCKED,
            ZeoDataContract.Headband.ON_HEAD,
            ZeoDataContract.Headband.SW_VERSION
    };

    public static final String[] ZAH_HEADBANDREC_EXTENDED_COLS = new String[] {
            ZeoDataContract.Headband._ID,
            ZeoDataContract.Headband.CREATED_ON,
            ZeoDataContract.Headband.UPDATED_ON,
            ZeoDataContract.Headband.ALGORITHM_MODE,
            ZeoDataContract.Headband.BLUETOOTH_ADDRESS,
            ZeoDataContract.Headband.BLUETOOTH_FRIENDLY_NAME,
            ZeoDataContract.Headband.BONDED,
            ZeoDataContract.Headband.CLOCK_OFFSET,
            ZeoDataContract.Headband.CONNECTED,
            ZeoDataContract.Headband.DOCKED,
            ZeoDataContract.Headband.ON_HEAD,
            ZeoDataContract.Headband.SW_VERSION,
            ACTIVE_FORCED,
            BLUETOOTH_LOCKED,
            DEMO_MODE,
            FLASH_CALIB_UPDATES,
            FLASH_SAVED_DATA_UPDATES,
            FLASH_SLEEP_BACKUP_UPDATES,
            HARDWARE_VERSION,
            LAST_ALARM_REASON,
            LAST_BATTERY_DIED_TIMESTAMP,
            LAST_BOND_TIMESTAMP,
            LAST_CONNECTED_TIMESTAMP,
            LAST_DISCONNECTED_TIMESTAMP,
            LAST_DOCKED_TIMESTAMP,
            LAST_FACTORY_RESET_TIMESTAMP,
            LAST_OFFEHAD_TIMESTAMP,
            LAST_ONHEAD_TIMESTAMP,
            LAST_SENSOR_USE_RESET_TIMESTAMP,
            LAST_UNBONDED_TIMESTAMP,
            LAST_UNDOCKED_TIMESTAMP,
            MODEL,
            NEED_CLOCK_OFFSET,
            NEED_TIME_SYNC,
            REQUIRES_PIN,
            SENSOR_USED,
            SERIAL_NUMBER,
            VOLTAGE,
            VOLTAGE_STATUS,
            WAS_CHARGED
    };

    // constructor
    public ZAH_HeadbandRecord(Cursor cursor) {
        rID = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband._ID));
        rCreated_timestamp = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.CREATED_ON));
        rUpdated_timestamp = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.UPDATED_ON));
        rAlgorithm_mode = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.ALGORITHM_MODE));
        rClock_offset = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.Headband.CLOCK_OFFSET));
        rBonded_to_device = (cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.BONDED)) != 0);
        rConnected_to_device = (cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.CONNECTED)) != 0);
        rDocked = (cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.DOCKED)) != 0);
        rOnHead = (cursor.getInt(cursor.getColumnIndex(ZeoDataContract.Headband.ON_HEAD)) != 0);
        rBluetooth_address = cursor.getString(cursor.getColumnIndex(ZeoDataContract.Headband.BLUETOOTH_ADDRESS));
        rBluetooth_friendly_name = cursor.getString(cursor.getColumnIndex(ZeoDataContract.Headband.BLUETOOTH_FRIENDLY_NAME));
        rSoftware_version = cursor.getString(cursor.getColumnIndex(ZeoDataContract.Headband.SW_VERSION));

        // the following fields are accessible regardless of the ZeoDataContract
        int i = cursor.getColumnIndex(HARDWARE_VERSION);
        if (i >= 0) { rHardware_version = cursor.getString(i); mHasExtended = true; }
        else { rHardware_version = null; }
        i = cursor.getColumnIndex(MODEL);
        if (i >= 0) { rModel = cursor.getString(i); mHasExtended = true; }
        else { rModel = null; }
        i = cursor.getColumnIndex(SERIAL_NUMBER);
        if (i >= 0) { rSerial = cursor.getString(i); mHasExtended = true; }
        else { rSerial = null; }

        i = cursor.getColumnIndex(ACTIVE_FORCED);
        if (i >= 0) { rActive_forced = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rActive_forced = false; }
        i = cursor.getColumnIndex(BLUETOOTH_LOCKED);
        if (i >= 0) { rBluetooth_locked = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rBluetooth_locked = false; }
        i = cursor.getColumnIndex(DEMO_MODE);
        if (i >= 0) { rDemo_mode = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rDemo_mode = false; }
        i = cursor.getColumnIndex(NEED_CLOCK_OFFSET);
        if (i >= 0) { rNeed_clock_offset = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rNeed_clock_offset = false; }
        i = cursor.getColumnIndex(NEED_TIME_SYNC);
        if (i >= 0) { rNeed_time_sync = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rNeed_time_sync = false; }
        i = cursor.getColumnIndex(REQUIRES_PIN);
        if (i >= 0) { rRequires_pin = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rRequires_pin = false; }
        i = cursor.getColumnIndex(WAS_CHARGED);
        if (i >= 0) { rWas_charged = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rWas_charged = false; }

        i = cursor.getColumnIndex(LAST_BATTERY_DIED_TIMESTAMP);
        if (i >= 0) { rLast_battery_died_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_battery_died_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_BOND_TIMESTAMP);
        if (i >= 0) { rLast_bond_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_bond_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_CONNECTED_TIMESTAMP);
        if (i >= 0) { rLast_connected_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_connected_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_DISCONNECTED_TIMESTAMP);
        if (i >= 0) { rLast_disconnected_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_disconnected_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_DOCKED_TIMESTAMP);
        if (i >= 0) { rLast_docked_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_docked_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_FACTORY_RESET_TIMESTAMP);
        if (i >= 0) { rLast_factory_reset_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_factory_reset_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_OFFEHAD_TIMESTAMP);
        if (i >= 0) { rLast_offhead_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_offhead_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_ONHEAD_TIMESTAMP);
        if (i >= 0) { rLast_onhead_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_onhead_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_SENSOR_USE_RESET_TIMESTAMP);
        if (i >= 0) { rLast_sensor_use_reset_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_sensor_use_reset_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_UNBONDED_TIMESTAMP);
        if (i >= 0) { rLast_unbonded_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_unbonded_timestamp = 0L; }
        i = cursor.getColumnIndex(LAST_UNDOCKED_TIMESTAMP);
        if (i >= 0) { rLast_undocked_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rLast_undocked_timestamp = 0L; }

        i = cursor.getColumnIndex(FLASH_CALIB_UPDATES);
        if (i >= 0) { rFlash_calibration_updates = cursor.getInt(i); mHasExtended = true; }
        else { rFlash_calibration_updates = 0; }
        i = cursor.getColumnIndex(FLASH_SAVED_DATA_UPDATES);
        if (i >= 0) { rFlash_saved_data_updates = cursor.getInt(i); mHasExtended = true; }
        else { rFlash_saved_data_updates = 0; }
        i = cursor.getColumnIndex(FLASH_SLEEP_BACKUP_UPDATES);
        if (i >= 0) { rFlash_sleep_backup_updates = cursor.getInt(i); mHasExtended = true; }
        else { rFlash_sleep_backup_updates = 0; }
        i = cursor.getColumnIndex(LAST_ALARM_REASON);
        if (i >= 0) { rLast_alarm_reason = cursor.getInt(i); mHasExtended = true; }
        else { rLast_alarm_reason = 0; }
        i = cursor.getColumnIndex(SENSOR_USED);
        if (i >= 0) { rSensor_used = cursor.getLong(i); mHasExtended = true; }
        else { rSensor_used = 0L; }
        i = cursor.getColumnIndex(VOLTAGE);
        if (i >= 0) { rVoltage = cursor.getInt(i); mHasExtended = true; }
        else { rVoltage = 0; }
        i = cursor.getColumnIndex(VOLTAGE_STATUS);
        if (i >= 0) { rVoltage_status = cursor.getInt(i); mHasExtended = true; }
        else { rVoltage_status = 0; }
    }

    // destructor to assist garbage collection when held in large multi-nested ListArrays
    public void destroy() {
        rBluetooth_address = null;
        rBluetooth_friendly_name = null;
        rSoftware_version = null;
        rHardware_version = null;
        rModel = null;
        rSerial = null;
    }
}
