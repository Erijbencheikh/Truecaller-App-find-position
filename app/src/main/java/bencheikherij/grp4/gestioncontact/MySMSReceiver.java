package bencheikherij.grp4.gestioncontact;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
// recevoir les SMS entrants et déclencher des actions
public class MySMSReceiver extends BroadcastReceiver {
    private static long lastProcessedTime = 0;
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0) return;

        SmsMessage[] messages = new SmsMessage[pdus.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            sb.append(messages[i].getMessageBody());
        }

        String messageBody = sb.toString();
        String phoneNumber = messages[0].getDisplayOriginatingAddress();
        // Add this at the very top of the onReceive() after getting phoneNumber
//        SharedPreferences prefs = context.getSharedPreferences("findfriends", MODE_PRIVATE);
//        boolean replied = prefs.getBoolean(phoneNumber, false);
//        if(!replied){
//            Intent i = new Intent(context, MyGpsLocationService.class);
//            i.putExtra("sender", phoneNumber);
//            context.startService(i);
//            prefs.edit().putBoolean(phoneNumber, true).apply(); // mark as replied
//        }

//        SharedPreferences prefs = context.getSharedPreferences("findfriends", MODE_PRIVATE);
//        long lastTime = prefs.getLong(phoneNumber + "_lastTime", 0);
//        long now = System.currentTimeMillis();
//        if (now - lastTime > 2000) { // 2 seconds
//            Intent i = new Intent(context, MyGpsLocationService.class);
//            i.putExtra("sender", phoneNumber);
//            context.startService(i);
//            prefs.edit().putLong(phoneNumber + "_lastTime", now).apply();
//        }


        Log.e("FindFriends", "Received SMS: " + messageBody);

        // Avoid duplicate processing
      //  long now = System.currentTimeMillis();
       // if (now - lastProcessedTime < 2000) return; // ignore duplicates within 2 sec
      //  lastProcessedTime = now;

        SharedPreferences prefs = context.getSharedPreferences("findfriends", MODE_PRIVATE);
        long lastTime = prefs.getLong(phoneNumber + "_lastTime", 0);
        long now = System.currentTimeMillis();
        // Only process "Envoyer moi votre position" message and avoid duplicates
        if (messageBody.contains("Find Friends : Envoyer moi votre position")) {
            if (now - lastTime > 2000) { // 2-second throttle
                Intent serviceIntent = new Intent(context, MyGpsLocationService.class);
                serviceIntent.putExtra("sender", phoneNumber);
                context.startService(serviceIntent);
                prefs.edit().putLong(phoneNumber + "_lastTime", now).apply();
            }
        }

        if (messageBody.startsWith("Find Friends : ma position est")) {
            String[] t = messageBody.split("#");
            if (t.length < 3) return; // safety check
            String longitude = t[1];
            String latitude = t[2];

            final String CHANNEL_ID = "findFriends_ChannelID";

            // Create the notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel canal = new NotificationChannel(
                        CHANNEL_ID,
                        "Canal pour Find Friend",
                        NotificationManager.IMPORTANCE_HIGH
                );
                NotificationManager manager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(canal);
            }

            // Build notification
            Intent mapIntent = new Intent(context, MapsActivity.class);
            mapIntent.putExtra("longitude", longitude);
            mapIntent.putExtra("latitude", latitude);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pi = PendingIntent.getActivity(
                    context, 0, mapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder mynotif = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("POSITION REÇUE")
                    .setContentText("Appuyez pour voir la position sur la carte.")
                    .setSmallIcon(android.R.drawable.ic_dialog_map)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pi)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(0, mynotif.build());
        }
    }
}
