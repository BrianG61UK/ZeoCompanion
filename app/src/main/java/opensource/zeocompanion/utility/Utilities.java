package opensource.zeocompanion.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import java.io.File;
import java.io.FilenameFilter;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;

// class for common utilities
public class Utilities {
    // create a hex string of a byte array for debug display purposes
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int start, int length) {
        if (start < 0) { start = 0; }
        if (length < 0) { length = bytes.length; }
        else if (length > bytes.length) { length = bytes.length; }
        if (start > bytes.length) { start = bytes.length; }

        char[] hexChars = new char[length * 2];
        int j = start;
        while (j < length) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            j++;
        }
        return new String(hexChars);
    }

    // create a standard duration string of a timestamp in minutes
    public static String showTimeInterval(double minutes, boolean includeTenths) {
        if (minutes < 60) {
            if (includeTenths) { return String.format("%.1fm", minutes); }
            else { return String.format("%.0fm", minutes); }
        }
        double hours =  Math.floor(minutes / 60.0);
        minutes = minutes - (hours * 60.0);
        if (includeTenths) { return String.format("%.0fh:%.1f", hours, minutes); }
        return String.format("%.0fh:%.0f", hours, minutes);
    }

    // show an alert dialog with only an Okay (aka dismiss button
    public static void showAlertDialog(Context context, String title, String message, String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.App_AlertDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(buttonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // show a Yes/No dialog, normally for Confirmation dialogs;
    // the callbackAction and two callbackStrings are provided solely for use of the caller
    public interface ShowYesNoDialogInterface {
        public abstract void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String callbackString2);
    }
    public static void showYesNoDialog(Context context, String title, String message, String buttonYesText, String buttonNoText, final ShowYesNoDialogInterface callback, final int callbackAction, final String callbackString1, final String callbackString2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.App_AlertDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
        if (buttonYesText != null) {
            if (!buttonYesText.isEmpty()) {
                builder.setPositiveButton(buttonYesText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.onYesNoDialogDone(true, callbackAction, callbackString1, callbackString2);
                        dialog.cancel();
                    }
                });
            }
        }
        if (buttonNoText != null) {
            if (!buttonNoText.isEmpty()) {
                builder.setNegativeButton(buttonNoText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.onYesNoDialogDone(false, callbackAction, callbackString1, callbackString2);
                        dialog.cancel();
                    }
                });
            }
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    // show a simple file chooser dialog; only shows files within one directory (no subdirectories);
    // only a cancel button and a list of selectable file names within the directory
    public interface ShowFileSelectDialogInterface {
        public abstract void showFileSelectDialogChosenFile(String theFileName, String sourceDir);
    }
    public static void showFileSelectDialog(Context context, String message, final File sourceDir, final ShowFileSelectDialogInterface callback) {
        // get source folder files
        sourceDir.mkdirs();
        final String fileList[] = sourceDir.list(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".db");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.App_AlertDialogTheme);
        builder.setTitle(message);
        builder.setItems(fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                callback.showFileSelectDialogChosenFile(fileList[which], sourceDir.getAbsolutePath());
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
