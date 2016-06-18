package opensource.zeocompanion.views;

// defines a record in a dataset of Zeo App sleep session data;
// this is a common dataset for the various time/trend-based graphs
public class SleepDatasetRec {
    // constants
    private static final int MAXFIELDS = 9;
    private static final int MAXWORKING = 2;

    // record's fields
    public long rTimestamp = 0L;
    public double[] rDataArray = new double[MAXFIELDS];
    public double rWorkingArray[] = new double[MAXWORKING];

    // constructor
    public SleepDatasetRec(long timestamp, double timeToZMin, double totalSleepMin, double remMin, double awakeMin, double lightMin, double deepMin, int awakeningsQty, int zq_score) {
        rTimestamp = timestamp;
        rDataArray[0] = timeToZMin;
        rDataArray[1] = totalSleepMin;
        rDataArray[2] = awakeMin;
        rDataArray[3] = remMin;
        rDataArray[4] = lightMin;
        rDataArray[5] = deepMin;
        rDataArray[6] = awakeningsQty;
        rDataArray[7] = zq_score;
        rDataArray[8] = timeToZMin + totalSleepMin +  awakeMin;
        rWorkingArray[0] = 0.0;
        rWorkingArray[1] = 0.0;
    }
}
