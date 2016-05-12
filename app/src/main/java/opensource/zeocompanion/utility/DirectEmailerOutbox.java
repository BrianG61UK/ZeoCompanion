package opensource.zeocompanion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.SharingActivity;

// maintains the direct email outbox; outbox only contains pending emails that failed to send
public class DirectEmailerOutbox {
    // member variables
    Context mContext = null;
    long mSequenceNumber = 0;

    // member constants and other static content
    private static final String _CTAG = "OBU";

    // class that defines the record used in interacting with the outbox
    public class OutboxEntry {
        public String rFilePath = null;
        public long rTimestamp = 0;
        public boolean rToAll = false;
        public String rToAddress = null;
        public String rSubject = null;
        public String rBody = null;
        public String rAttachmentPath = null;
        public String rShortErrorMessage = null;
        public String rLongErrorMessage = null;

        // the following is a control that is not stored in the record on-disk
        public boolean mResent = false;
    }

    // constructor
    public DirectEmailerOutbox(Context context) { mContext = context; }

    // called daily by the AlarmManager to check for automatic export via Direct Email
    public void dailyCheck() {
        // determine if the Daily Check should continue
        Log.d(_CTAG + ".dailyCheck", "Daily check triggered");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean enabled = prefs.getBoolean("email_auto_enable", false);
        if (!enabled) { return; }

        // select any new records since the lastmost highest timestamp (plus one second)
        long timestampLastAutoExported = prefs.getLong("email_auto_timestamp_last_exported", 0) + 1000;
        ArrayList<JournalDataCoordinator.IntegratedHistoryRec> iRecs = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecsFromDate(iRecs, timestampLastAutoExported);
        if (iRecs.isEmpty()) {
            // nothing new to export
            Log.d(_CTAG+".dailyCheck","Daily check found nothing new to export");
            return;
        }
        boolean sendCSV = prefs.getBoolean("email_auto_send_csv", true);
        boolean sendImage = prefs.getBoolean("email_auto_send_image", false);

        // send a CSV via Direct Email
        String subject = "ZeoCompanion CSV auto export";
        String body = subject + "; see attachment.";
        if (sendCSV) {
            CSVexporter theExporter1 = new CSVexporter(mContext);
            CSVexporter.ReturnResults exportResults = theExporter1.createFileFromData(iRecs, CSVexporter.SHARE_WHAT_CSV_EXCEL);
            if (exportResults.rTheExportFile == null || !exportResults.rAnErrorMessage.isEmpty()) {
                postToOutbox(null, subject, body, exportResults.rTheExportFile, exportResults.rAnErrorMessage, null);
            } else {
                DirectEmailerThread de = new DirectEmailerThread(mContext);
                de.setName("DirectEmailerThread via " + _CTAG + ".dailyCheck");
                de.configure(subject, body, exportResults.rTheExportFile, true);
                de.start();
            }
        }

        // send an Image via Direct Email
        subject = "ZeoCompanion Image auto export";
        body = subject + "; see attachment.";
        if (sendImage) {
            ImageExporter theExporter2 = new ImageExporter(mContext);
            for (JournalDataCoordinator.IntegratedHistoryRec iRec: iRecs) {
                ImageExporter.ReturnResults exportResults = theExporter2.createFileOneRec(iRec, ImageExporter.SHARE_WHAT_IMAGE_STANDARD);
                if (exportResults.rTheExportFile == null || !exportResults.rAnErrorMessage.isEmpty()) {
                    postToOutbox(null, subject, body, exportResults.rTheExportFile, exportResults.rAnErrorMessage, null);
                } else {
                    DirectEmailerThread de = new DirectEmailerThread(mContext);
                    de.setName("DirectEmailerThread via " + _CTAG + ".dailyCheck");
                    de.configure(subject, body, exportResults.rTheExportFile, true);
                    de.start();
                }
            }
        }

        // determine the highest timestamp of the exported records
        long lastTimestamp = 0;
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: iRecs) {
            if (iRec.mTimestamp > lastTimestamp) { lastTimestamp = iRec.mTimestamp; }
        }

        // remember the highest timestamp of the exported records
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("email_auto_timestamp_last_exported", lastTimestamp);
        editor.commit();

        // though disputed, assist garbage collection by explicitly and entirely destroying the contents and subcontents of each selected IntegratedHistoryRec
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: iRecs) { iRec.destroy(); }
        iRecs.clear();
    }

    // Thread context: this is normally called by the DirectEmailerThread
    // add a failed email to the outbox with error message and preserve the attachment (if any)
    public void postToOutbox(String toAddress, String subject, String body, File attachmentSource, String errorMessageShort, String errorMessageDetailed) {
        int r = ZeoCompanionApplication.checkExternalStorage();
        if (r != 0) { return; }

        // create the directory path to our outbox subdirectory in external storage
        File outboxFilesDir = new File(ZeoCompanionApplication.mBaseExtStorageDir + File.separator + "outbox");
        outboxFilesDir.mkdirs();

        // copy the attachment into the outbox
        File destination = null;
        if (attachmentSource != null) {
            String newName = attachmentSource.getName();
            int p = newName.lastIndexOf(".");
            if (p < 0) { newName = newName + "_" +  mSequenceNumber; }
            else {
                String ext = newName.substring(p);
                newName = newName.substring(0, p) + "_" + mSequenceNumber + ext;
            }
            destination = new File(outboxFilesDir.getAbsolutePath() + File.separator + newName);
            try {
                FileUtils.copyFile(attachmentSource, destination);
            } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".postToOutbox", e, "Failed to copy Attachment into Outbox ("+attachmentSource.getAbsoluteFile()+"): " + destination.getAbsoluteFile()); // automatically posts a Log.e
            }
        }

        // create the outbox header file itself
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String str = "Outbox_"+mSequenceNumber+"_"+ df.format(new Date()) + ".txt";
        mSequenceNumber++;
        File theOutboxFile = new File(outboxFilesDir.getAbsolutePath() + File.separator + str);

        // build the Outbox record
        OutboxEntry rec = new OutboxEntry();
        rec.rFilePath = theOutboxFile.getAbsolutePath();
        rec.rSubject = subject;
        rec.rBody = body;
        rec.rShortErrorMessage = errorMessageShort;
        rec.rLongErrorMessage = errorMessageDetailed;
        rec.rTimestamp = System.currentTimeMillis();
        if (destination == null) { rec.rAttachmentPath = null; }
        else { rec.rAttachmentPath = destination.getAbsolutePath(); }
        if (toAddress == null) { rec.rToAll = true; }
        else { rec.rToAddress = toAddress; }

        // write the record to disk in its file
        writeOutboxFile(theOutboxFile, rec);

        // inform the Main Activity so it can changes its menus; must be done via messaging
        if (MainActivity.instance != null) {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_MENU;
            MainActivity.instance.mHandler.sendMessage(msg);
        }
    }

    // Thread context: this is normally called by the DirectEmailerThread, though it can be called via an Activity
    // write the contents of the Outbox record to its file
    private void writeOutboxFile(File theFile, OutboxEntry rec) {
        try {
            theFile.createNewFile();
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".writeOutboxFile", e, "Cannot create Outbox file: "+theFile.getAbsolutePath());   // automatically posts a Log.e
            return;
        }

        try {
            FileOutputStream f = new FileOutputStream(theFile);
            OutputStreamWriter wrt = new OutputStreamWriter(f);
            wrt.append(rec.rTimestamp+"\n");
            if (rec.rToAll || rec.rToAddress == null) { wrt.append("$ALL$\n"); }
            else { wrt.append(rec.rToAddress+"\n"); }
            wrt.append(rec.rSubject+"\n");
            wrt.append(rec.rBody+"\n");
            if (rec.rAttachmentPath != null) { wrt.append(rec.rAttachmentPath+"\n"); }
            else { wrt.append("$NONE$\n"); }
            if (rec.rShortErrorMessage != null) { wrt.append(rec.rShortErrorMessage+"\n"); }
            if (rec.rLongErrorMessage != null) { wrt.append(rec.rLongErrorMessage); }
            wrt.flush();
            wrt.close();
            f.close();
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".writeOutboxFile", e, "Cannot write to Outbox file: " + theFile.getAbsolutePath()); // automatically posts a Log.e
        }
    }

    // read the contents of an Outbox record via its absolute path
    public OutboxEntry readOutboxFile(String theFilePath) {
        File theFile = new File(theFilePath);
        return readOutboxFile(theFile);
    }

    // read the contents of an Outbox file via a File definition
    private OutboxEntry readOutboxFile(File theFile) {
        try {
            Reader rdr = new FileReader(theFile);
            BufferedReader br = new BufferedReader(rdr);
            OutboxEntry newRec = new OutboxEntry();
            newRec.rFilePath = theFile.getAbsolutePath();
            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                switch (row) {
                    case 1:
                        newRec.rTimestamp = Long.parseLong(line);
                        break;
                    case 2:
                        if (line.equals("$ALL$")) { newRec.rToAll = true; }
                        else { newRec.rToAddress = line;  }
                        break;
                    case 3:
                        newRec.rSubject = line;
                        break;
                    case 4:
                        newRec.rBody = line;
                        break;
                    case 5:
                        if (!line.equals("$NONE$")) { newRec.rAttachmentPath = line; }
                        break;
                    case 6:
                        newRec.rShortErrorMessage = line;
                        break;
                    case 7:
                        newRec.rLongErrorMessage = line;
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                        newRec.rLongErrorMessage = newRec.rLongErrorMessage + "\n" + line;
                        break;
                }
                row++;
            }
            br.close();
            rdr.close();
            return newRec;
        }
        catch (IOException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".readOutboxFile", e, "Cannot read Outbox file: " + theFile.getAbsolutePath());  // automatically posts a Log.e
        }
        return null;
    }

    // get the number of entries in the Outbox
    public int getQtyEntries() {
        int r = ZeoCompanionApplication.checkExternalStorage();
        if (r != 0) { return 0; }

        // create the directory path to our outbox subdirectory in external storage
        File outboxFilesDir = new File(ZeoCompanionApplication.mBaseExtStorageDir + File.separator + "outbox");
        outboxFilesDir.mkdirs();

        // step through all the files
        int qty = 0;
        File[] files = outboxFilesDir.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isFile()) {
                    if (inFile.getName().startsWith("Outbox_")) { qty++; }
                }
            }
        }
        return qty;
    }

    // get an ArrayList of everything in the outbox
    public String getAllOutboxEntries(ArrayList<OutboxEntry> theArray) {
        int r = ZeoCompanionApplication.checkExternalStorage();
        if (r == -2) { return "Permission for App to write to external storage has not been granted; please grant the permission";  }
        else if (r != 0) { return "No writable external storage is available"; }

        // create the directory path to our outbox subdirectory in external storage
        File outboxFilesDir = new File(ZeoCompanionApplication.mBaseExtStorageDir + File.separator + "outbox");
        outboxFilesDir.mkdirs();

        // step through all the files
        File[] files = outboxFilesDir.listFiles();
        for (File inFile : files) {
            if (inFile.isFile()) {
                if (inFile.getName().startsWith("Outbox_")) {
                    OutboxEntry rec = readOutboxFile(inFile);
                    if (rec != null) { theArray.add(rec); }
                }
            }
        }
        return null;
    }

    // delete an Outbox file (and an attachment file if present)
    public void deleteOutboxEntry(OutboxEntry theRec) {
        File theFile = new File(theRec.rFilePath);
        theFile.delete();
        if (theRec.rAttachmentPath != null) {
            if (!theRec.rAttachmentPath.isEmpty()) {
                File theAttachmentFile = new File(theRec.rAttachmentPath);
                theAttachmentFile.delete();
            }
        }
    }

    // change to To Address within an Outbox file
    public void changeToAddress(String theFilePath, String newToAddress) {
        File theFile = new File(theFilePath);
        OutboxEntry rec = readOutboxFile(theFile);
        if (!rec.rToAll) {
            rec.rToAddress = newToAddress;
            writeOutboxFile(theFile, rec);
        }
    }
}
