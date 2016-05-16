package opensource.zeocompanion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import com.obscuredPreferences.ObscuredPrefs;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import opensource.zeocompanion.ZeoCompanionApplication;

// this thread will directly email an attachment to all destination email addressed
// from a configured email account; debug information about the send is captured
// to assist the end-user in getting their emails sent
public class DirectEmailerThread extends Thread {
    // member variables
    Context mContext = null;
    boolean mIsAutomatic = false;
    String mToAddressOverride = null;
    String mSubject = null;
    String mBody = null;
    File mAttachment = null;
    DirectEmailerThreadResponse mCallback = null;
    String mCallbackString = null;
    boolean mPostToOutbox = true;

    // member constants and other static content
    private static final String _CTAG = "DEU";

    // callback interface for the thread to report back email sending results
    public interface DirectEmailerThreadResponse {
        public void emailResults(String callbackString, boolean successFail, String message);
    }

    // thread context:  Activity
    // constructor
    public DirectEmailerThread(Context context) { mContext = context; }

    // thread context:  Activity
    // configure the email subject, body, and attachment
    public int configure(String subject, String body, File attachment, boolean isAutomatic) {
        mSubject = subject;
        mBody = body;
        mAttachment = attachment;
        mIsAutomatic = isAutomatic;

        // validate whether proper Settings have been made
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!prefs.contains("email_enable") || !prefs.contains("email_smtp_a") ||
                !prefs.contains("email_smtp_port") || !prefs.contains("email_dest_a")) { return -1; }
        boolean authNeeded = prefs.getBoolean("email_smtp_authNeeded", false);
        if (authNeeded) {
            if (!prefs.contains("email_smtp_b") || !prefs.contains("email_smtp_c")) { return -1; }
        }
        return 0;
    }

    // thread context:  Activity
    // configure the to-email-address rather than sending to all email addresses;
    // this is only called via the Outbox to resend a failed email
    public void configureToAddress(String toAddress) {
        mToAddressOverride = toAddress;
    }

    // thread context:  Activity
    // optionally set an email results callback from the UI
    public void setResultsCallback(DirectEmailerThreadResponse callback, String callbackString, boolean postToOutbox) {
        mCallback = callback;
        mCallbackString = callbackString;
        mPostToOutbox = postToOutbox;
    }

    // thread context:  DirectEmailerThread
    // perform the emailing process
    public void run() {
        // load and verify all email Settings
        Thread.setDefaultUncaughtExceptionHandler(ZeoCompanionApplication.mMasterAbortHandler); // set the master abort handler for this thread
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean enabled = prefs.getBoolean("email_enable", false);
        if (!enabled) { errorResult("Direct enail disabled in Settings"); }

        if (!prefs.contains("email_smtp_a") || !prefs.contains("email_smtp_port") || !prefs.contains("email_dest_a")) {
            errorResultWithOutbox("Missing Settings", null);
        }
        boolean authNeeded = prefs.getBoolean("email_smtp_authNeeded", false);
        if (authNeeded) {
            if (!prefs.contains("email_smtp_b") || !prefs.contains("email_smtp_c")) {
                errorResultWithOutbox("Missing Auth Settings", null);
            }
        }

        String serverPort = prefs.getString("email_smtp_port", "0");
        String serverSecurity = prefs.getString("email_smtp_security", "None");
        String serverAddr = "";
        if (prefs.contains("email_smtp_a")) {
            serverAddr = ObscuredPrefs.decryptString(prefs.getString("email_smtp_a", ""));      // need to use these .contains() to ensure the getString does not create an unencrypted default
        }
        String str1 = "";
        if (prefs.contains("email_smtp_b")) {
            str1 = ObscuredPrefs.decryptString(prefs.getString("email_smtp_b", ""));
        }
        final String u = str1;
        String str2 = "";
        if (prefs.contains("email_smtp_c")) {
            str2 = ObscuredPrefs.decryptString(prefs.getString("email_smtp_c", ""));
        }
        final String p = str2;
        String d1 = "";
        if (prefs.contains("email_dest_a")) {
            d1 = ObscuredPrefs.decryptString(prefs.getString("email_dest_a", ""));
        }
        String d2 = null;
        if (prefs.contains("email_dest_b")) {
            d2 = ObscuredPrefs.decryptString(prefs.getString("email_dest_b", ""));
        }
        String d3 = null;
        if (prefs.contains("email_dest_c")) {
            d3 = ObscuredPrefs.decryptString(prefs.getString("email_dest_c", ""));
        }

        // create the email properties for the email subsystem
        System.setProperty("mail.mime.encodefilename","false");
        System.setProperty("mail.mime.encodeparameters","false");
        System.setProperty("mail.mime.foldtext","false");
        Properties emailProps = new Properties();
        emailProps.put("mail.smtp.host",serverAddr);
        emailProps.put("mail.smtp.port", serverPort);
        if (serverSecurity.equals("TLS")) { emailProps.put("mail.smtp.starttls.enable", "true"); }
        else if (serverSecurity.equals("SSL")) { emailProps.put("mail.smtp.ssl.enable","true"); }
        if (authNeeded) { emailProps.put("mail.smtp.auth","true"); }
        else { emailProps.put("mail.smtp.auth","false"); }

        // create an email session using the properties; this is the interaction with the server
        Session session = Session.getInstance(emailProps, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(u, p); }
        });
        //session.setDebug(true); // used just for debugging

        // send the email(s)
        int r = 0;
        if (mToAddressOverride != null) {
            if (!mToAddressOverride.isEmpty()) {
                r = r + createAndSendMessage(session, mToAddressOverride);  // note this is only invoked by the Outbox and it will handle deleting of the Attachment file
            }
        } else {
            if (d1 != null) {
                if (!d1.isEmpty()) { r = r + createAndSendMessage(session, d1); }
            }
            if (d2 != null) {
                if (!d2.isEmpty()) { r = r + createAndSendMessage(session, d2); }
            }
            if (d3 != null) {
                if (!d3.isEmpty()) { r = r + createAndSendMessage(session, d3); }
            }
            if (mAttachment != null) { mAttachment.delete(); }
        }
        if (r == 0) { successResult(); }
    }

    // thread context:  DirectEmailerThread
    // send one copy of the message to one specified email address
    private int createAndSendMessage(Session session, String toAddress) {
        boolean prepared = false;
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(toAddress));
            msg.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toAddress));
            msg.setSubject(mSubject);
            msg.setSentDate(new Date(System.currentTimeMillis()));

            if (mAttachment == null) {
                msg.setText(mBody);
            } else {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(mBody, "text/plain");
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(mAttachment.getAbsoluteFile());
                attachPart.setFileName( mAttachment.getName());
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachPart);
                msg.setContent(multipart);
            }
            prepared = true;
        } catch (MessagingException e) {
            Log.d(_CTAG + ".createSendEmail", "Preparation failed for " + toAddress + ": " + e.toString());
            e.printStackTrace();
            errorResultWithOutbox("Email preparation failed for " + toAddress, toAddress, "Email prep failed: "+parseForShort(e.toString()), e.toString());
            return -1;
        } catch (IOException e) {
            Log.d(_CTAG + ".createSendEmail", "Attachment IO failed for " + toAddress + ": " + e.toString());
            e.printStackTrace();
            errorResultWithOutbox("Attachment IO failed for " + toAddress, toAddress, "Attachment IO failed: "+parseForShort(e.toString()), e.toString());
            return -1;
        }

        if (prepared) {
            try {
                Transport.send(msg);
            } catch (MessagingException e) {
                Log.d(_CTAG + ".createSendEmail", "Email failed to send for " + toAddress + ": " + e.toString());
                e.printStackTrace();
                errorResultWithOutbox("Failed to send for " + toAddress, toAddress, "Failed to send: "+parseForShort(e.toString()), e.toString());
                return -1;
            }
        }
        return 0;
    }

    // thread context:  DirectEmailerThread
    // get the essential summary error message
    private String parseForShort(String errorMessage) {
        /* formats observed (however messages from destination servers are custom):
            Auth Enabled not set:
                "com.sun.mail.smtp.SMTPSendFailedException: 550 5.1.0 Authentication required"
            Auth userid or password incorrect
                "javax.mail.AuthenticationFailedException: 535 5.7.0 ...authentication rejected"
            Invalid destination email address (rejected by destination server)
                "com.sun.mail.smtp.SMTPSendFailedException: 550 5.1.0 xxx_custom_message_xxx.
                nested exception is:
                com.sun.mail.smtp.SMTPSenderFailedException: 550 5.1.0 xxx_custom_message_xxx"
            Incorrect server address or port address
                "com.sun.mail.util.MailConnectException: Couldn't connect to host, port: xxx, xxx; timeout -1;
                nested exception is:
                java.net.ConnectException: failed to connect to xxx (port xxx): connect failed: ETIMEDOUT (Connection timed out)"
            SSL enabled yet rejected due to older version
                "javax.mail.MessagingException: Could not connect to SMTP host: xxx, port: xxx;
                nested exception is:
                javax.net.ssl.SSLHandshakeException: javax.net.ssl.SSLProtocolException: SSL handshake aborted: ssl=0x78d47660: Failure in SSL library, usually a protocol error
                error:1408F10B:SSL routines:SSL3_GET_RECORD:wrong version number (external/openssl/ssl/s3_pkt.c:337 0x72401cf8:0x00000000)"
         */
        int startPos = 0;
        int endPos = errorMessage.length();
        int pos = errorMessage.indexOf("\n");
        if (pos >= 0) { endPos = pos; }
        pos = errorMessage.indexOf("Exception: ");
        if (pos >= 0 && pos + 11 < endPos) { startPos = pos + 11; }
        return errorMessage.substring(startPos, endPos);
    }

    // thread context:  DirectEmailerThread
    private void successResult() {
        sendToast("All emails successfully sent");
        if (mCallback != null) { mCallback.emailResults(mCallbackString, true, "All emails successfully sent"); }
    }

    // thread context:  DirectEmailerThread
    private void errorResult(String message) {
        sendToast("EMAIL NOT SENT: "+message);
        if (mCallback != null) { mCallback.emailResults(mCallbackString, false, message); }
        else if (mIsAutomatic) { ZeoCompanionApplication.postAlert("NOTICE: Automatic Email(s) have failed"); }
    }

    // thread context:  DirectEmailerThread
    private void errorResultWithOutbox(String message, String toAddress) {
        if (mPostToOutbox) { ZeoCompanionApplication.mEmailOutbox.postToOutbox(toAddress, mSubject, mBody, mAttachment, message, null); }
        sendToast("EMAIL NOT SENT: "+message);
        if (mCallback != null) { mCallback.emailResults(mCallbackString, false, message); }
        else if (mPostToOutbox && mIsAutomatic) { ZeoCompanionApplication.postAlert("NOTICE: Automatic Email(s) have failed; see the Outbox"); }
    }

    // thread context:  DirectEmailerThread
    private void errorResultWithOutbox(String message, String toAddress, String shortError, String longError) {
        if (mPostToOutbox) { ZeoCompanionApplication.mEmailOutbox.postToOutbox(toAddress, mSubject, mBody, mAttachment, shortError, longError); }
        sendToast("EMAIL NOT SENT: "+message);
        if (mCallback != null) { mCallback.emailResults(mCallbackString, false, longError); }
        else if (mPostToOutbox && mIsAutomatic) { ZeoCompanionApplication.postAlert("NOTICE: Automatic Email(s) have failed; see the Outbox"); }
    }

    // thread context:  DirectEmailerThread
    private void sendToast(String message) {
        Message msg = new Message();
        msg.what = ZeoCompanionApplication.MESSAGE_APP_SEND_TOAST;
        msg.obj = message;
        ZeoCompanionApplication.mAppHandler.sendMessage(msg);
    }
}
