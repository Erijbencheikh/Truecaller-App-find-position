package bencheikherij.grp4.gestioncontact;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnSuccessListener;
//Service Android pour envoyer automatiquement ta position GPS par SMS
public class MyGpsLocationService extends Service {
    public MyGpsLocationService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        //lina mara bark
    }
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String sender=intent.getStringExtra("sender");
        // Localisation
        Log.e("success","sucess listner");
        //recuperation position gps

        FusedLocationProviderClient mClient = LocationServices.getFusedLocationProviderClient(this);
        mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location!=null){
                    double longitude=location.getLongitude();
                    double latitude=location.getLatitude();
                    SmsManager manager = SmsManager.getDefault();
                    manager.sendTextMessage(sender,
                            null,
                            "Find Friends : ma position est #" + longitude + "#" + latitude,
                            null,
                            null);

                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}