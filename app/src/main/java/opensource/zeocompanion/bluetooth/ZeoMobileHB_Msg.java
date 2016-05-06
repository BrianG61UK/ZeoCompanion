package opensource.zeocompanion.bluetooth;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import opensource.zeocompanion.utility.Utilities;

public class ZeoMobileHB_Msg {
    public static final short ZEOMOB_HB_MSG_NULL = 0;
    public static final short ZEOMOB_HB_MSG_COMMAND_REQ = 1;
    public static final short ZEOMOB_HB_MSG_ACKNOWLEDGE = 2;
    public static final short ZEOMOB_HB_MSG_STARTING = 3;
    public static final short ZEOMOB_HB_MSG_STATE_CHANGE_REPORT = 4;
    public static final short ZEOMOB_HB_MSG_REPORT_ACCELEROMETER = 5;
    public static final short ZEOMOB_HB_MSG_REPORT_ERROR = 6;
    public static final short ZEOMOB_HB_MSG_REPORT_SLEEP = 7;
    public static final short ZEOMOB_HB_MSG_SET_UP_BLUETOOTH = 8;
    public static final short ZEOMOB_HB_MSG_STATE_REPORT = 9;
    public static final short ZEOMOB_HB_MSG_TIME_QUERY = 10;
    public static final short ZEOMOB_HB_MSG_TIME_REPORT = 11;
    public static final short ZEOMOB_HB_MSG_WAKEUP_NOTIFY = 12;
    public static final short ZEOMOB_HB_MSG_WAKEUP_WINDOW = 13;
    public static final short ZEOMOB_HB_MSG_TEST_FIRST = 150;
    public static final short ZEOMOB_HB_MSG_TEST_ACCEL_QUERY = 151;
    public static final short ZEOMOB_HB_MSG_TEST_ACCEL_REPORT = 152;
    public static final short ZEOMOB_HB_MSG_TEST_ACP_QUERY = 153;
    public static final short ZEOMOB_HB_MSG_TEST_ACP_REPORT = 154;
    public static final short ZEOMOB_HB_MSG_TEST_ANALOG = 155;
    public static final short ZEOMOB_HB_MSG_TEST_BC6_PASSTHROUGH = 156;
    public static final short ZEOMOB_HB_MSG_TEST_BT_ADDRESS_SET = 157;
    public static final short ZEOMOB_HB_MSG_TEST_BUTTON_REPORT = 158;
    public static final short ZEOMOB_HB_MSG_TEST_CAL_DATA_QUERY = 159;
    public static final short ZEOMOB_HB_MSG_TEST_CAL_DATA_REPORT = 160;
    public static final short ZEOMOB_HB_MSG_TEST_FREQ_TRIM_SET = 161;
    public static final short ZEOMOB_HB_MSG_TEST_IMPEDANCE = 162;
    public static final short ZEOMOB_HB_MSG_TEST_LED = 163;
    public static final short ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE = 164;
    public static final short ZEOMOB_HB_MSG_TEST_POWER_OFF = 165;
    public static final short ZEOMOB_HB_MSG_TEST_RESET = 166;
    public static final short ZEOMOB_HB_MSG_TEST_SERIAL_ID_SET = 167;
    public static final short ZEOMOB_HB_MSG_TEST_VOLTAGE_QUERY = 168;
    public static final short ZEOMOB_HB_MSG_TEST_VOLTAGE_REPORT = 169;
    public static final short ZEOMOB_HB_MSG_TEST_WAVEFORM = 170;
    public static final short ZEOMOB_HB_MSG_TEST_WAVEFORM_REPLY = 171;
    public static final short ZEOMOB_HB_MSG_DANGER1 = 173;  // DANGER: sending this message to the headband will brick it

    public static final short ZEOMOB_HB_STCHG_NONE = 0;
    public static final short ZEOMOB_HB_STCHG_ALARM = 1;
    public static final short ZEOMOB_HB_STCHG_ALARM_WIND_END = 2;
    public static final short ZEOMOB_HB_STCHG_ALARM_WIND_START = 3;
    public static final short ZEOMOB_HB_STCHG_DOCKED = 4;
    public static final short ZEOMOB_HB_STCHG_LOW_BATT = 5;
    public static final short ZEOMOB_HB_STCHG_OFF_HEAD = 6;
    public static final short ZEOMOB_HB_STCHG_ON_HEAD = 7;
    public static final short ZEOMOB_HB_STCHG_SLEEP_MODE_CHG = 8;
    public static final short ZEOMOB_HB_STCHG_SLEEP_NIGHT_END = 9;
    public static final short ZEOMOB_HB_STCHG_SLEEP_NIGHT_RESTORED = 10;
    public static final short ZEOMOB_HB_STCHG_SLEEP_NIGHT_SAVED = 11;
    public static final short ZEOMOB_HB_STCHG_SLEEP_ONSET = 12;
    public static final short ZEOMOB_HB_STCHG_SLEEP_NIGHT_START = 13;
    public static final short ZEOMOB_HB_STCHG_SLEEP_RATING_NEED = 14;
    public static final short ZEOMOB_HB_STCHG_SLEEP_STATE_CHG = 15;
    public static final short ZEOMOB_HB_STCHG_TIME_JUMP = 16;
    public static final short ZEOMOB_HB_STCHG_UNDOCKED = 17;
    public static final short ZEOMOB_HB_STCHG_USER_SLEEP_LOCKED = 18;
    public static final short ZEOMOB_HB_STCHG_USER_SLEEP_OFF = 19;
    public static final short ZEOMOB_HB_STCHG_USER_SLEEP_ON = 20;
    public static final short ZEOMOB_HB_STCHG_USER_SLEEP_RESET = 21;
    public static final short ZEOMOB_HB_STCHG_NUM = 22;

    public static final short ZEOMOB_HB_ERR_NONE = 0;
    public static final short ZEOMOB_HB_ERR_INVALID_NAME = 1;
    public static final short ZEOMOB_HB_ERR_INVALID_PIN = 2;
    public static final short ZEOMOB_HB_ERR_PIN_NEEDED = 3;
    public static final short ZEOMOB_HB_ERR_PIN_NOT_NEEDED = 4;
    public static final short ZEOMOB_HB_ERR_RESET_NEEDED = 5;
    public static final short ZEOMOB_HB_ERR_WRONG_CHANNEL = 6;

    public static final short ZEOMOB_HB_WAKE_REM_TO_NONREM = 0;
    public static final short ZEOMOB_HB_WAKE_NONREM_TO_REM = 1;
    public static final short ZEOMOB_HB_WAKE_ON_WAKE = 2;
    public static final short ZEOMOB_HB_WAKE_RISING_OUT_OF_DEEP = 3;
    public static final short ZEOMOB_HB_WAKE_END_OF_WAKEUP_INTERVAL = 8;

    public static final short ZEOMOB_HB_CMD_NONE = 0;
    public static final short ZEOMOB_HB_CMD_BLUETOOTH_LOCK = 1;
    public static final short ZEOMOB_HB_CMD_BLUETOOTH_UNLOCK = 2;
    public static final short ZEOMOB_HB_CMD_DEMO_MODE_OFF = 3;
    public static final short ZEOMOB_HB_CMD_DEMO_MODE_ON = 4;
    public static final short ZEOMOB_HB_CMD_FACTORY_RESET = 5;
    public static final short ZEOMOB_HB_CMD_QUERY_STATE = 6;
    public static final short ZEOMOB_HB_CMD_RESET_SENSOR_USE = 7;
    public static final short ZEOMOB_HB_CMD_SLEEP_SEND = 8;
    public static final short ZEOMOB_HB_CMD_SLEEP_START = 9;
    public static final short ZEOMOB_HB_CMD_SLEEP_STOP = 10;
    public static final short ZEOMOB_HB_CMD_NMAX = 11;
    public static final short ZEOMOB_HB_CMD_REBOOT = 100;

    public byte[] rBytes = null;
    public String rHeader = "";
    public int rCRC = 0;
    public short rProtVer = 0;
    public short rMsgType = 0;
    public short rActReq = 0;
    public short rSeqNo = 0;
    public int rDataLen = 0;

    public int mRecLen = 0;
    public boolean mWasSent = false;
    public boolean mIsValid = false;
    public boolean mCRCvalid = false;

    public ZeoMobileHB_Msg() {
    }

    public ZeoMobileHB_Msg(int theLen, byte[] theBytes) {
        rBytes = theBytes;
        mRecLen = theLen;
        mIsValid = false;
        if (mRecLen >= 4) {
            rHeader = new String(theBytes,0,4);
            if (!rHeader.equals("HMSG")) { return; }
        }
        if (mRecLen < 12) { return; }

        rCRC = getUInt16(theBytes, 4);
        rProtVer = theBytes[6];
        if (rProtVer != 2) { return; }

        rMsgType = getUInt8(theBytes, 7);
        rActReq = getUInt8(theBytes, 8);
        rSeqNo = getUInt8(theBytes, 9);

        rDataLen = getUInt16(theBytes, 10);
        if (rDataLen + 12 != mRecLen) { return; }

        mIsValid = true;
        mCRCvalid = verifyCRC();
    }

    public String getInvalidReason() {
        if (mRecLen < 12) { return "*TOO SHORT*"; }
        if (!rHeader.equals("HMSG")) { return "*INVALID HDR*"; }
        if (rProtVer != 2) { return "*INVALID PROTVER*"; }
        if (rDataLen + 12 != mRecLen) { return "*INVALID DATALEN*"; }
        return "*INVALID UNKNOWN*";
    }

    public byte[] getCopyOfBytes() {
        byte[] newArray = new byte[mRecLen];
        System.arraycopy(rBytes, 0, newArray, 0, mRecLen);
        return newArray;
    }

    public void setSeqNo(short theSeqNo) {
        rSeqNo = theSeqNo;
        setUInt8(rBytes, 9, theSeqNo);
    }

    public void setAcqReq(boolean setYes) {
        if (setYes) { rActReq = 1; rBytes[8] = 1; }
        else { rActReq = 0; rBytes[8] = 0; }
    }

    public void setTimeQuery() {
        if (rMsgType != ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TIME_QUERY) { return; }
        if (mRecLen < 20) { return; }
        long currentTime = System.currentTimeMillis();

        long currentSec = currentTime / 1000L;
        if (mRecLen >= 16) {
            rBytes[15] = (byte) ((currentSec & 0xFF000000) >> 24);
            rBytes[14] = (byte) ((currentSec & 0x00FF0000) >> 16);
        }
        if (mRecLen >= 14) {
            rBytes[13] = (byte) ((currentSec & 0x0000FF00) >> 8);
            rBytes[12] = (byte) (currentSec & 0x000000FF);
        }

        long currentMS = currentTime % 1000L;
        if (mRecLen >= 20) {
            rBytes[19] = (byte) ((currentMS & 0xFF000000) >> 24);
            rBytes[18] = (byte) ((currentMS & 0x00FF0000) >> 16);
        }
        if (mRecLen >= 18) {
            rBytes[17] = (byte) ((currentMS & 0x0000FF00) >> 8);
            rBytes[16] = (byte) (currentMS & 0x000000FF);
        }
    }

    public String getDateTimeString(int bufPos, int fieldLen, boolean showDate, boolean showTime) {
        long sec = 0;
        long millisec = 0;

        if (fieldLen >= 4) {
            // there are 32-bit seconds since 1/1/1970 present
            sec = (int)(((0xFF & (int)rBytes[bufPos+3]) << 24) | ((0xFF & (int)rBytes[bufPos+2]) << 16) | ((0xFF & (int)rBytes[bufPos+1]) << 8) | (0xFF & (int)rBytes[bufPos]));
        }
        if (fieldLen == 8) {
            // there are 32-bit milliseconds since 1/1/1970 present
            millisec = (int)(((0xFF & (int)rBytes[bufPos+7]) << 24) | ((0xFF & (int)rBytes[bufPos+6]) << 16) | ((0xFF & (int)rBytes[bufPos+5]) << 8) | (0xFF & (int)rBytes[bufPos+4]));
        }

        String fmtStr = "";
        if (showDate) { fmtStr = fmtStr + "yyyy/MM/dd"; }
        if (showTime) {
            if (showDate) fmtStr = fmtStr + " ";
            fmtStr = fmtStr + "hh:mm:ss";
            if (fieldLen == 8) { fmtStr = fmtStr + ".S"; }
        }

        Date d = new Date(sec * 1000L + millisec);
        SimpleDateFormat df =  new SimpleDateFormat(fmtStr);
        return df.format(d);
    }

    public void addCRC() {
        // ZeoMobile headband uses 16-bit CRC
        // initial state of the CRC is all ones (65535)
        // the first 6 bytes (header and CRC fields) are not included in the CRC calculation
        rCRC = crc16(CRC16_INIT_CCIT, rBytes, 6, mRecLen);
        setUInt16(rBytes, 4, rCRC);
        Log.d("ZeoMsg.addCRC", "CRC calc=" + rCRC);
    }

    public boolean verifyCRC() {
        // ZeoMobile headband uses 16-bit CRC
        // initial state of the CRC is all ones (65535)
        // the first 6 bytes (header and CRC fields) are not included in the CRC calculation

        int newCRC1 = crc16(CRC16_INIT_CCIT, rBytes, 6, mRecLen);
        if (newCRC1 != rCRC) { Log.d("ZeoMsg.verifyCRC", "CRC mismatch: orig="+rCRC+" != calc="+newCRC1); }
        if (newCRC1 != rCRC) { return false; }
        return true;
    }

    public static final int CRC16_INIT_CCIT = 65535;
    private static final int[] CRC16_TABLE = { 0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302, 37689, 33560, 45947, 41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, 42346, 46411, 34088, 38153, 58862, 62927, 50604, 54669, 13907, 9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681, 34616, 63455, 59390, 55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789, 59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640, 14899, 10770, 56317, 52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879, 19684, 23749, 11298, 15363, 3168, 7233, 60846, 64911, 52716, 56781, 44330, 48395, 36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761, 3696, 65439, 61374, 57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453, 61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979, 46042, 49981, 54044, 58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270, 46570, 42443, 38312, 34185, 62830, 58703, 54572, 50445, 13538, 9411, 5280, 1153, 29798, 25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358, 50973, 55100, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368, 35305, 47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093, 56156, 60223, 64286, 35833, 39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801, 6864, 10931, 14994, 64814, 60687, 56684, 52557, 48554, 44427, 40424, 36297, 31782, 27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085, 57212, 44955, 49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920 };

    public int crc16(int seed, byte[] buffer, int startByte, int endByte)
    {
        int i = seed;
        for (int pos = startByte; pos < endByte; pos++)
        {
            int k = buffer[pos];
            i = i << 8 & 0xFFFF ^ CRC16_TABLE[(i >> 8 ^ k & 0xFF)];
        }
        return i;
    }

    public void makeCommand(short command) {
        // java spec specifies that all new byte elements will be pre-set to zero
        byte[] newBytes = new byte[16];
        rHeader = "HMSG";
        newBytes[0] = 0x48;
        newBytes[1] = 0x4d;
        newBytes[2] = 0x53;
        newBytes[3] = 0x47;
        rProtVer = 2;
        newBytes[6] = 2;
        rMsgType = ZEOMOB_HB_MSG_COMMAND_REQ;
        setUInt8(newBytes, 7, ZEOMOB_HB_MSG_COMMAND_REQ);
        rDataLen = 4;
        newBytes[10] = 4;
        setUInt8(newBytes, 12, command);
        rBytes = newBytes;
        mRecLen = 16;
        mIsValid = true;
        mCRCvalid = false;
    }

    public void makeTimeQuery() {
        // java spec specifies that all new byte elements will be pre-set to zero
        byte[] newBytes = new byte[20];
        rHeader = "HMSG";
        newBytes[0] = 0x48;
        newBytes[1] = 0x4d;
        newBytes[2] = 0x53;
        newBytes[3] = 0x47;
        rProtVer = 2;
        newBytes[6] = 2;
        rMsgType = ZEOMOB_HB_MSG_TIME_QUERY;
        setUInt8(newBytes, 7, ZEOMOB_HB_MSG_TIME_QUERY);
        rDataLen = 8;
        newBytes[10] = 8;
        // note the actual time value will be inserted just before this message is sent
        rBytes = newBytes;
        mRecLen = 20;
        mIsValid = true;
        mCRCvalid = false;
    }

    public void makeTestLEDON(boolean setLED_on) {
        // java spec specifies that all new byte elements will be pre-set to zero
        byte[] newBytes = new byte[16];
        rHeader = "HMSG";
        newBytes[0] = 0x48;
        newBytes[1] = 0x4d;
        newBytes[2] = 0x53;
        newBytes[3] = 0x47;
        rProtVer = 2;
        newBytes[6] = 2;
        rMsgType = ZEOMOB_HB_MSG_TEST_LED;
        setUInt8(newBytes, 7, ZEOMOB_HB_MSG_TEST_LED);
        rDataLen = 4;
        newBytes[10] = 4;
        if (setLED_on) { newBytes[12] = 1; }
        rBytes = newBytes;
        mRecLen = 16;
        mIsValid = true;
        mCRCvalid = false;
    }

    public void makePCBTestMode(boolean setPCBTestMode) {
        // java spec specifies that all new byte elements will be pre-set to zero
        byte[] newBytes = new byte[16];
        rHeader = "HMSG";
        newBytes[0] = 0x48;
        newBytes[1] = 0x4d;
        newBytes[2] = 0x53;
        newBytes[3] = 0x47;
        rProtVer = 2;
        newBytes[6] = 2;
        rMsgType = ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE;
        setUInt8(newBytes, 7, ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE);
        rDataLen = 4;
        newBytes[10] = 4;
        if (setPCBTestMode) { newBytes[12] = 1; }
        rBytes = newBytes;
        mRecLen = 16;
        mIsValid = true;
        mCRCvalid = false;
    }

    public void makeTestQuery(short msgNo, short protVer) {
        // java spec specifies that all new byte elements will be pre-set to zero
        byte[] newBytes = new byte[16];
        rHeader = "HMSG";
        newBytes[0] = 0x48;
        newBytes[1] = 0x4d;
        newBytes[2] = 0x53;
        newBytes[3] = 0x47;
        rProtVer = protVer;
        newBytes[6] = (byte)protVer;
        rMsgType = msgNo;
        setUInt8(newBytes, 7, msgNo);
        rDataLen = 4;
        newBytes[10] = 4;
        rBytes = newBytes;
        mRecLen = 16;
        mIsValid = true;
        mCRCvalid = false;
    }

    public String getMessageTypeString() {
        switch (rMsgType) {
            case ZEOMOB_HB_MSG_NULL:
                return "Null";
            case ZEOMOB_HB_MSG_COMMAND_REQ:
                return "Command";
            case ZEOMOB_HB_MSG_ACKNOWLEDGE:
                return "Acknowledge";
            case ZEOMOB_HB_MSG_STARTING:
                return "Starting";
            case ZEOMOB_HB_MSG_STATE_CHANGE_REPORT:
                return "Change State Report";
            case ZEOMOB_HB_MSG_REPORT_ACCELEROMETER:
                return "Accelerometer Report";
            case ZEOMOB_HB_MSG_REPORT_ERROR:
                return "Error Report";
            case ZEOMOB_HB_MSG_REPORT_SLEEP:
                return "Sleep Report";
            case ZEOMOB_HB_MSG_SET_UP_BLUETOOTH:
                return "Setup Bluetooth";
            case ZEOMOB_HB_MSG_STATE_REPORT:
                return "State Report";
            case ZEOMOB_HB_MSG_TIME_QUERY:
                return "Time Query";
            case ZEOMOB_HB_MSG_TIME_REPORT:
                return "Time Report";
            case ZEOMOB_HB_MSG_WAKEUP_NOTIFY:
                return "Wakeup Notify";
            case ZEOMOB_HB_MSG_WAKEUP_WINDOW:
                return "Wakeup Window";
            case ZEOMOB_HB_MSG_TEST_FIRST:
                return "Test First";
            case ZEOMOB_HB_MSG_TEST_ACCEL_QUERY:
                return "Test Accel Query";
            case ZEOMOB_HB_MSG_TEST_ACCEL_REPORT:
                return "Test Accel Report";
            case ZEOMOB_HB_MSG_TEST_ACP_QUERY:
                return "Test ACP Query";
            case ZEOMOB_HB_MSG_TEST_ACP_REPORT:
                return "Test ACP Report";
            case ZEOMOB_HB_MSG_TEST_ANALOG:
                return "Test Analog";
            case ZEOMOB_HB_MSG_TEST_BC6_PASSTHROUGH:
                return "Test BC6 Passthru";
            case ZEOMOB_HB_MSG_TEST_BT_ADDRESS_SET:
                return "Test BT Set Address";
            case ZEOMOB_HB_MSG_TEST_BUTTON_REPORT:
                return "Test Button Report";
            case ZEOMOB_HB_MSG_TEST_CAL_DATA_QUERY:
                return "Test Cal Data Query";
            case ZEOMOB_HB_MSG_TEST_CAL_DATA_REPORT:
                return "Test Cal Data Report";
            case ZEOMOB_HB_MSG_TEST_FREQ_TRIM_SET:
                return "Test Freq Trim Set";
            case ZEOMOB_HB_MSG_TEST_IMPEDANCE:
                return "Test Impedance";
            case ZEOMOB_HB_MSG_TEST_LED:
                return "Test LED";
            case ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE:
                return "Test PCB Test Mode";
            case ZEOMOB_HB_MSG_TEST_POWER_OFF:
                return "Test Power Off";
            case ZEOMOB_HB_MSG_TEST_RESET:
                return "Test Reset";
            case ZEOMOB_HB_MSG_TEST_SERIAL_ID_SET:
                return "Test Serial ID Set";
            case ZEOMOB_HB_MSG_TEST_VOLTAGE_QUERY:
                return "Test Voltage Query";
            case ZEOMOB_HB_MSG_TEST_VOLTAGE_REPORT:
                return "Test Voltage Report";
            case ZEOMOB_HB_MSG_TEST_WAVEFORM:
                return "Test Waveform";
            case ZEOMOB_HB_MSG_TEST_WAVEFORM_REPLY:
                return "Test Waveform Reply";
            default:
                return "*UKNOWN* MsgType=" + rMsgType;
        }
    }

    public String getDataString() {
        if (!mIsValid) { return getInvalidReason(); }
        switch (rMsgType) {
            case ZEOMOB_HB_MSG_NULL:
                return "";
            case ZEOMOB_HB_MSG_COMMAND_REQ:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return getCommandString();
            case ZEOMOB_HB_MSG_ACKNOWLEDGE:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                String str = "Prev Seq#=" + getUInt8(rBytes, 12);
                if (mRecLen >= 14) { str = str + "; " + rBytes[13]; }
                if (mRecLen >= 15) { str = str + "," + rBytes[14]; }
                if (mRecLen >= 16) { str = str + "," + rBytes[15]; }
                return str;
            case ZEOMOB_HB_MSG_STARTING:
                return "";  // ?!? sent every 24 hours; not bothered with it as-of-yet
            case ZEOMOB_HB_MSG_STATE_CHANGE_REPORT:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return getStateChangeString();
            case ZEOMOB_HB_MSG_REPORT_ACCELEROMETER:
                return "";  // ?!? this message is not being sent by the headband
            case ZEOMOB_HB_MSG_REPORT_ERROR:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return getErrorReportString();
            case ZEOMOB_HB_MSG_REPORT_SLEEP:
                if (mRecLen != 1156) return "*WRONG RECLEN* ="+mRecLen+" s/b 1156";
                return getSleepReportString();
            case ZEOMOB_HB_MSG_SET_UP_BLUETOOTH:
                return "";  // ?!? have not experimented with the pairing process
            case ZEOMOB_HB_MSG_STATE_REPORT:
                if (mRecLen != 28) return "*WRONG RECLEN* ="+mRecLen+" s/b 28";
                return getStateReportString();
            case ZEOMOB_HB_MSG_TIME_QUERY:
                if (mRecLen != 20) return "*WRONG RECLEN* ="+mRecLen+" s/b 20";
                return getDateTimeString(12, 8, true, true);
            case ZEOMOB_HB_MSG_TIME_REPORT:
                if (mRecLen != 24) return "*WRONG RECLEN* ="+mRecLen+" s/b 24";
                String str1 = getDateTimeString(12, 8, true, true);
                if (rBytes[20] != 0) { str1 = str1 + ", +"; }
                else { str1 = str1 + ", -"; }
                str1 = str1 + "Offset, ";
                if (rBytes[21] != 0) { str1 = str1 + "+"; }
                else { str1 = str1 + "-"; }
                str1 = str1 + "Negative, for query seq# " + getUInt8(rBytes, 22);
                return str1;
            case ZEOMOB_HB_MSG_WAKEUP_NOTIFY:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return getWakeupNotifyString();
            case ZEOMOB_HB_MSG_WAKEUP_WINDOW:
                if (mRecLen != 24) return "*WRONG RECLEN* ="+mRecLen+" s/b 24";
                String str2 = "When sent="+getDateTimeString(12, 4, true, true);
                str2 = str2 + ", Begins=" + getDateTimeString(16, 4, false, true);
                str2 = str2 + ", Ends=" + getDateTimeString(20, 4, false, true);
                return str2;
            case ZEOMOB_HB_MSG_TEST_FIRST:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "";
            case ZEOMOB_HB_MSG_TEST_ACCEL_QUERY:
                return "";
            case ZEOMOB_HB_MSG_TEST_ACCEL_REPORT:
                return "";
            case ZEOMOB_HB_MSG_TEST_ACP_QUERY:
                return "";
            case ZEOMOB_HB_MSG_TEST_ACP_REPORT:
                return "";
            case ZEOMOB_HB_MSG_TEST_ANALOG:
                return "";
            case ZEOMOB_HB_MSG_TEST_BC6_PASSTHROUGH:
                return "";
            case ZEOMOB_HB_MSG_TEST_BT_ADDRESS_SET:
                return "";
            case ZEOMOB_HB_MSG_TEST_BUTTON_REPORT:
                return "";
            case ZEOMOB_HB_MSG_TEST_CAL_DATA_QUERY:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "";
            case ZEOMOB_HB_MSG_TEST_CAL_DATA_REPORT:
                if (mRecLen != 40) return "*WRONG RECLEN* ="+mRecLen+" s/b 40";
                return "Unknown";
            case ZEOMOB_HB_MSG_TEST_FREQ_TRIM_SET:
                return "";
            case ZEOMOB_HB_MSG_TEST_IMPEDANCE:
                return "";
            case ZEOMOB_HB_MSG_TEST_LED:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "Enable=" + rBytes[12];
            case ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "Enable=" + rBytes[12];
            case ZEOMOB_HB_MSG_TEST_POWER_OFF:
                return "";
            case ZEOMOB_HB_MSG_TEST_RESET:
                return "";
            case ZEOMOB_HB_MSG_TEST_SERIAL_ID_SET:
                return "";
            case ZEOMOB_HB_MSG_TEST_VOLTAGE_QUERY:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "";
            case ZEOMOB_HB_MSG_TEST_VOLTAGE_REPORT:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "Unknown=" + rBytes[12] + "," + rBytes[13] + "," + rBytes[14] + "," + rBytes[15];
            case ZEOMOB_HB_MSG_TEST_WAVEFORM:
                return "";
            case ZEOMOB_HB_MSG_TEST_WAVEFORM_REPLY:
                if (mRecLen != 16) return "*WRONG RECLEN* ="+mRecLen+" s/b 16";
                return "Unknown=" + rBytes[12] + "," + rBytes[13] + "," + rBytes[14] + "," + rBytes[15];
            default:
                return "";
        }
    }

    public String getCommandString() {
        if (rMsgType != ZEOMOB_HB_MSG_COMMAND_REQ) return "*WRONG MSGTYPE*";
        if (mRecLen < 13) return "*REC TOO SHORT*";
        switch (rBytes[12]) {
            case ZEOMOB_HB_CMD_NONE:
                return "None";
            case ZEOMOB_HB_CMD_BLUETOOTH_LOCK:
                return "Set Bluetooth Lock";
            case ZEOMOB_HB_CMD_BLUETOOTH_UNLOCK:
                return "Set Bluetooth Unlock";
            case ZEOMOB_HB_CMD_DEMO_MODE_OFF:
                return "Set Demo Mode Off";
            case ZEOMOB_HB_CMD_DEMO_MODE_ON:
                return "Set Demo Mode On";
            case ZEOMOB_HB_CMD_FACTORY_RESET:
                return "Factory Reset";
            case ZEOMOB_HB_CMD_QUERY_STATE:
                return "Request State Report";
            case ZEOMOB_HB_CMD_RESET_SENSOR_USE:
                return "Reset Sensor Use";
            case ZEOMOB_HB_CMD_SLEEP_SEND:
                return "Request Sleep Report";
            case ZEOMOB_HB_CMD_SLEEP_START:
                return "Set Sleep Start";
            case ZEOMOB_HB_CMD_SLEEP_STOP:
                return "Set Sleep Stop";
            case ZEOMOB_HB_CMD_NMAX:
                return "NMAX";
            case ZEOMOB_HB_CMD_REBOOT:
                return "Reboot Headband";
            default:
                return "*UKNOWN* CmdNo=" + getUInt8(rBytes, 12);
        }
    }

    public String getStateChangeString() {
        if (rMsgType != ZEOMOB_HB_MSG_STATE_CHANGE_REPORT) return "*WRONG MSGTYPE*";
        if (mRecLen < 13) return "*REC TOO SHORT*";
        switch (rBytes[12]) {
            case ZEOMOB_HB_STCHG_NONE:
                return "None";
            case ZEOMOB_HB_STCHG_ALARM:
                return "Alarm";
            case ZEOMOB_HB_STCHG_ALARM_WIND_END:
                return "Alarm Window Ended";
            case ZEOMOB_HB_STCHG_ALARM_WIND_START:
                return "Alarm Window Started";
            case ZEOMOB_HB_STCHG_DOCKED:
                return "Docked";
            case ZEOMOB_HB_STCHG_LOW_BATT:
                return "Low Battery";
            case ZEOMOB_HB_STCHG_OFF_HEAD:
                return "Off Head";
            case ZEOMOB_HB_STCHG_ON_HEAD:
                return "On Head";
            case ZEOMOB_HB_STCHG_SLEEP_MODE_CHG:
                return "Sleep Mode Changed";
            case ZEOMOB_HB_STCHG_SLEEP_NIGHT_END:
                return "Sleep Night Ended";
            case ZEOMOB_HB_STCHG_SLEEP_NIGHT_RESTORED:
                return "Sleep Night Restored";
            case ZEOMOB_HB_STCHG_SLEEP_NIGHT_SAVED:
                return "Sleep Night Saved";
            case ZEOMOB_HB_STCHG_SLEEP_ONSET:
                return "Sleep Onset";
            case ZEOMOB_HB_STCHG_SLEEP_NIGHT_START:
                return "Sleep Night Started";
            case ZEOMOB_HB_STCHG_SLEEP_RATING_NEED:
                return "Sleep Rating Needed";
            case ZEOMOB_HB_STCHG_SLEEP_STATE_CHG:
                return "Sleep State Change";
            case ZEOMOB_HB_STCHG_TIME_JUMP:
                return "Time Jump";
            case ZEOMOB_HB_STCHG_UNDOCKED:
                return "Undocked";
            case ZEOMOB_HB_STCHG_USER_SLEEP_LOCKED:
                return "User Sleep Locked";
            case ZEOMOB_HB_STCHG_USER_SLEEP_OFF:
                return "User Sleep Off";
            case ZEOMOB_HB_STCHG_USER_SLEEP_ON:
                return "User Sleep On";
            case ZEOMOB_HB_STCHG_USER_SLEEP_RESET:
                return "User Sleep Reset";
            case ZEOMOB_HB_STCHG_NUM:
                return "Num";
            default:
                return "*UKNOWN* StateNo=" + getUInt8(rBytes, 12);
        }
    }

    public String getErrorReportString() {
        if (rMsgType != ZEOMOB_HB_MSG_REPORT_ERROR) return "*WRONG MSGTYPE*";
        if (mRecLen < 13) return "*REC TOO SHORT*";
        switch (rBytes[12]) {
            case ZEOMOB_HB_ERR_NONE:
                return "None";
            case ZEOMOB_HB_ERR_INVALID_NAME:
                return "Invalid BT Name";
            case ZEOMOB_HB_ERR_INVALID_PIN:
                return "Invalid BT PIN";
            case ZEOMOB_HB_ERR_PIN_NEEDED:
                return "BT PIN Needed";
            case ZEOMOB_HB_ERR_PIN_NOT_NEEDED:
                return "BT PIN Not Needed";
            case ZEOMOB_HB_ERR_RESET_NEEDED:
                return "Reset Needed";
            case ZEOMOB_HB_ERR_WRONG_CHANNEL:
                return "BT Wrong Channel";
            default:
                return "*UKNOWN* ErrorNo=" + getUInt8(rBytes, 12);
        }
    }

    public String getWakeupNotifyString() {
        if (rMsgType != ZEOMOB_HB_MSG_WAKEUP_NOTIFY) return "*WRONG MSGTYPE*";
        if (mRecLen < 13) return "*REC TOO SHORT*";
        switch (rBytes[12]) {
            case ZEOMOB_HB_WAKE_REM_TO_NONREM:
                return "REM to nonREM";
            case ZEOMOB_HB_WAKE_NONREM_TO_REM:
                return "nonREM to REM";
            case ZEOMOB_HB_WAKE_ON_WAKE:
                return "User Awoke";
            case ZEOMOB_HB_WAKE_RISING_OUT_OF_DEEP:
                return "Rising Out Of Deep";
            case ZEOMOB_HB_WAKE_END_OF_WAKEUP_INTERVAL:
                return "End Of Wakeup Interval";
            default:
                return "*UKNOWN* WakeupNo=" + getUInt8(rBytes, 12);
        }
    }

    public String getStateReportString() {
        if (rMsgType != ZEOMOB_HB_MSG_STATE_REPORT) return "*WRONG MSGTYPE*";
        if (mRecLen < 28) return "*WRONG RECLEN* ="+mRecLen+" s/b 28";
        String str = "";
        if (rBytes[12] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "Active, ";
        if (rBytes[13] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "BTlock, ";
        if (rBytes[14] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "Demo, ";
        if (rBytes[15] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "Docked, ";
        if (rBytes[16] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "OnHead, ";
        //if (rBytes[17] != 0) { str = str + "+"; } else { str = str + "-"; }
        //str = str + "ReqPIN, ";
        if (rBytes[18] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "Chrgd, ";
        if (rBytes[19] != 0) { str = str + "+"; } else { str = str + "-"; }
        str = str + "Query, ";
        double volts = (double)getUInt8(rBytes, 20) / 100.0;
        str = str + "BatVolt=" + volts +", Bat=";
        switch (rBytes[21]) {
            case 0:
                str = str + "None";
                break;
            case 1:
                str = str + "Charged";
                break;
            case 2:
                str = str + "Charging";
                break;
            case 3:
                str = str + "OnBat";
                break;
            case 4:
                str = str + "TooLow";
                break;
            case 5:
                str = str + "nMax";
                break;
            default:
                str = str + "*UNKNOWN*";
                break;
        }
        str = str + ", Alarm=";
        switch (rBytes[22]) {
            case 0:
                str = str + "None";
                break;
            case 1:
                str = str + "OutOfDeep";
                break;
            case 2:
                str = str + "-REM2REM";
                break;
            case 3:
                str = str + "REM2-REM";
                break;
            case 4:
                str = str + "Awake";
                break;
            default:
                str = str + "*UNKNOWN*";
                break;
        }
        str = str + ", Mode=";
        switch (rBytes[23]) {
            case 0:
                str = str + "Idle";
                break;
            case 1:
                str = str + "Start";
                break;
            case 2:
                str = str + "Record";
                break;
            case 3:
                str = str + "End";
                break;
            default:
                str = str + "*UNKNOWN*";
                break;
        }

        return str;
    }

    public String getSleepReportString() {
        if (rMsgType != ZEOMOB_HB_MSG_REPORT_SLEEP) return "*WRONG MSGTYPE*";
        if (mRecLen != 1156) return "*WRONG RECLEN* ="+mRecLen+" s/b 1156";
        String str = "";
        if (rBytes[12] != 0 | rBytes[13] != 0 | rBytes[14] != 0 | rBytes[15] != 0) {
            str = str + "Record=" + getDateTimeString(12, 4, true, true);
        }
        if (rBytes[184] != 0 | rBytes[185] != 0 | rBytes[186] != 0 | rBytes[187] != 0) {
            str = str + ", Start=" + getDateTimeString(184, 4, false, true);
        }
        if (rBytes[60] != 0 | rBytes[61] != 0 | rBytes[62] != 0 | rBytes[63] != 0) {
            str = str + ", End=" + getDateTimeString(60, 4, false, true);
        }

        str = str + "; TimeIn(z/ts={a,r,l,d})=";
        str = str + getUInt16(rBytes, 26) + "/" + getUInt16(rBytes, 28) + "={" + getUInt16(rBytes, 24) + "," + getUInt16(rBytes, 22) + "," + getUInt16(rBytes, 20) + "," + getUInt16(rBytes, 18) +"}";

        str = str + "; ZQ=" + getUInt16(rBytes, 30);
        return str;
    }

    public short getUInt8(byte[] buffer, int pos) {
        short val = (short)((int)buffer[pos] & 0x7F);
        if (((int)buffer[pos] & 0x80) != 0) { val = (short)(val + 128); }
        return val;
    }

    public void setUInt8(byte[] buffer, int pos, short val) {
        buffer[pos] = (byte)(val & 0xFF);
    }

    public int getUInt16(byte[] buffer, int pos) {
        int val = ((int)buffer[pos] & 0xFF);
        val = val |  (((int)buffer[pos+1] & 0x7F) << 8);
        if (((int)buffer[pos+1] & 0x80) != 0) { val = val + 32768; }
        return val;
    }

    public void setUInt16(byte[] buffer, int pos, int val) {
        val = val & 0xFFFF;
        buffer[pos+1] = (byte)((val & 0xFF00) >> 8);
        buffer[pos]   =  (byte)(val & 0x00FF);
    }
}
