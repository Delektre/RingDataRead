package fi.delektre.ringdataread;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by t2r on 11/03/18.
 */

public class DataRow {
    private int id;
    private int ledRed, ledGreen, ledBlue, ledNIR, ledYellow, temp1, temp2, steps, timeValue;
    private float pos_lat, pos_long, pos_alt;
    private int syncstatus, measureId;

    public int getId() {
        return id;
    }

    public int getLedRed() {
        return ledRed;
    }

    public int getLedGreen() {
        return ledGreen;
    }

    public int getLedBlue() {
        return ledBlue;
    }

    public int getLedNIR() {
        return ledNIR;
    }

    public int getLedYellow() {
        return ledYellow;
    }

    public int getTemp1() {
        return temp1;
    }

    public int getTemp2() {
        return temp2;
    }

    public int getSteps() {
        return steps;
    }

    public int getTimeValue() {
        return timeValue;
    }

    public float getPositionLatitude() {
        return pos_lat;
    }

    public float getPositionLongitude() {
        return pos_long;
    }

    public float getPositionAltitude() {
        return pos_alt;
    }

    public int getSyncStatus() {
        return syncstatus;
    }

    public int getMeasureId() {
        return measureId;
    }


    public DataRow(int timeValue, int id, int red, int green, int blue, int nir, int yellow, int temp1, int temp2, int steps, int measid) {
        this.timeValue = timeValue;
        this.id = id;
        this.ledRed = red;
        this.ledGreen = green;
        this.ledBlue = blue;
        this.ledNIR = nir;
        this.ledYellow = yellow;
        this.temp1 = temp1;
        this.temp2 = temp2;
        this.steps = steps;
        this.measureId = measid;
    }
/*
    public DataRow(Cursor cursor) {
        this.ledRed = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_RED));
        this.ledGreen = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_GREEN));
        this.ledBlue = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_BLUE));
        this.ledNIR = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_NIR));
        this.ledYellow = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_YELLOW));
        this.temp1 = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_TEMP1));
        this.temp2 = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_TEMP2));
        this.steps = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_STEPS));
        this.timeValue = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_TIME));
        this.pos_lat = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_LAT));
        this.pos_long = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_LONG));
        this.pos_alt = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_ALT));
        this.syncstatus = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_SYNC));
        this.measureId = cursor.getInt(cursor.getColumnIndex(LocalDataContract.COL_NAME_MID));
    }

    public GraphViewEntry[] getGraphViewEntries() {
        GraphViewEntry[] data = new GraphViewEntry[]{
                new GraphViewEntry(GraphView.ENTRY_INDEX_LED_RED, ledRed),
                new GraphViewEntry(GraphView.ENTRY_INDEX_LED_GREEN, ledGreen),
                new GraphViewEntry(GraphView.ENTRY_INDEX_LED_BLUE, ledBlue),
                new GraphViewEntry(GraphView.ENTRY_INDEX_LED_YELLOW, ledYellow),
                new GraphViewEntry(GraphView.ENTRY_INDEX_LED_NIR, ledNIR),
                new GraphViewEntry(GraphView.ENTRY_INDEX_TEMP1, temp1),
                new GraphViewEntry(GraphView.ENTRY_INDEX_TEMP2, temp2),
                new GraphViewEntry(GraphView.ENTRY_INDEX_STEPS, steps)
        };
        return data;
    }
*/
    public void setSyncStatus(int status) {
        this.syncstatus = status;
    }

    public void setPosition(float latitude, float longitude, float altitude) {
        this.pos_lat = latitude;
        this.pos_long = longitude;
        this.pos_alt = altitude;
    }

    @Override
    public String toString() {
        return "DataRow [id=" + id + ", red=" + ledRed + ", green=" + ledGreen +
                ", blue=" + ledBlue + ", nir=" + ledNIR + ", yellow=" + ledYellow +
                ", temp1=" + temp1 + ", temp2=" + temp2 + ", steps=" + steps +
                ", time=" + timeValue + ", lat=" + pos_lat + ", long=" + pos_long +
                ", altitude=" + pos_alt + ", syncstat=" + syncstatus + ", measureId=" + measureId + "]";
    }


    public String getCSV() {
        return id + "; " + ledRed + "; " + ledGreen + "; " + ledBlue + "; " +
                ledNIR + "; " + ledYellow + "; " + temp1 + "; " + temp2 + "; " +
                steps + "; " + timeValue + "; " + pos_lat + "; " + pos_long + "; " +
                pos_alt + "; " + syncstatus + "; " + measureId;
    }

    public static String getCSVHeader() {
        return "\"id\"; \"red\"; \"green\"; \"blue\"; \"nir\"; \"yellow\"; \"temp1\"; \"temp2\"; \"steps\"; " +
                "\"time\"; \"latitude\"; \"longitude\"; \"altitude\"; \"sync\"; \"measureId\"";
    }
    /*
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(LocalDataContract.COL_NAME_RED, ledRed);
        values.put(LocalDataContract.COL_NAME_GREEN, ledGreen);
        values.put(LocalDataContract.COL_NAME_BLUE, ledBlue);
        values.put(LocalDataContract.COL_NAME_NIR, ledNIR);
        values.put(LocalDataContract.COL_NAME_YELLOW, ledYellow);
        values.put(LocalDataContract.COL_NAME_TEMP1, temp1);
        values.put(LocalDataContract.COL_NAME_TEMP2, temp2);
        values.put(LocalDataContract.COL_NAME_TIME, timeValue);
        values.put(LocalDataContract.COL_NAME_STEPS, steps);
        values.put(LocalDataContract.COL_NAME_SYNC, syncstatus);
        values.put(LocalDataContract.COL_NAME_LAT, pos_lat);
        values.put(LocalDataContract.COL_NAME_LONG, pos_long);
        values.put(LocalDataContract.COL_NAME_ALT, pos_alt);
        values.put(LocalDataContract.COL_NAME_MID, measureId);
        return values;
    }
    */
}
