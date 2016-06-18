package opensource.zeocompanion.views;

// defines a record in a dataset of integrated sleep session data that splits out each recorded attribute-value;
// this is a common dataset for the various attribute-based graphs
public class AttrValsSleepDatasetRec {
    // constants
    private static final int MAXFIELDS = 9;
    private static final int MAXWORKING = 2;

    // record's fields
    public String rAttributeDisplayName = null; // can always be null if attributes in the definitions database are renamed or deleted
    public String rAttributeShortName = null;
    public String rValueString = null;
    public float rLikertValue = 0.0f;
    public long rTimestamp = 0L;
    public double rDataArray[] = new double[MAXFIELDS];
    public double rWorkingArray[] = new double[MAXWORKING];

    // constructor
    public AttrValsSleepDatasetRec(String attribute, float likert, String valueString, long timestamp, double timeToZMin, double totalSleepMin, double remMin, double awakeMin, double lightMin, double deepMin, int awakeningsQty, int zq_score) {
        rAttributeShortName = attribute;
        rAttributeDisplayName = null;
        rValueString = valueString;
        rLikertValue = likert;
        rTimestamp = timestamp;
        rDataArray[0] = timeToZMin;
        rDataArray[1] = totalSleepMin;
        rDataArray[2] = awakeMin;
        rDataArray[3] = remMin;
        rDataArray[4] = lightMin;
        rDataArray[5] = deepMin;
        rDataArray[6] = awakeningsQty;
        rDataArray[7] = zq_score;
        rDataArray[8] = timeToZMin + totalSleepMin +  awakeMin; // total sleep session duration
        rWorkingArray[0] = 0.0;
        rWorkingArray[1] = 0.0;
    }
}
