package com.bluefletch.internal.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.urbanairship.UAirship;
import com.urbanairship.actions.ActionUtils;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.LandingPageAction;
import com.urbanairship.actions.OpenExternalUrlAction;
import com.urbanairship.push.PushManager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class IntentReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("Received intent: %s", intent);
        String action = intent.getAction();

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {

            int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

            Timber.i("Received push notification. Alert: %s [NotificationID=%s]",
                    intent.getStringExtra(PushManager.EXTRA_ALERT), id);

            logPushExtras(intent);

        } else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {

            Timber.i("User clicked notification. Message: %s", intent.getStringExtra(PushManager.EXTRA_ALERT));

            logPushExtras(intent);

            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.setClass(UAirship.shared().getApplicationContext(), MainActivity.class);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            UAirship.shared().getApplicationContext().startActivity(launch);

        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            Timber.i("Registration complete. APID: %s.  Valid: %s",
                    intent.getStringExtra(PushManager.EXTRA_APID),
                    intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));
        }
    }

    /**
     * Log the values sent in the payload's "extra" dictionary.
     *
     * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
     */
    private void logPushExtras(Intent intent) {
        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {

            //ignore standard extra keys (GCM + UA)
            List<String> ignoredKeys = Arrays.asList(
                    "collapse_key",//GCM collapse key
                    "from",//GCM sender
                    PushManager.EXTRA_NOTIFICATION_ID,//int id of generated notification (ACTION_PUSH_RECEIVED only)
                    PushManager.EXTRA_PUSH_ID,//internal UA push id
                    PushManager.EXTRA_ALERT);//ignore alert
            if (ignoredKeys.contains(key)) {
                continue;
            }
            Timber.i("Push Notification Extra: [%s : %s]", key, intent.getStringExtra(key));
        }
    }
}