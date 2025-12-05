package bencheikherij.grp4.gestioncontact;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import bencheikherij.grp4.gestioncontact.databinding.ActivityMapsBinding;
// afficher la carte, choisir une position, ajouter un marqueur et enregistrer dans la base de données
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    // Active coordinates
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if coordinates were sent from a notification
        if (getIntent() != null) {
            try {
                String latStr = getIntent().getStringExtra("latitude");
                String lonStr = getIntent().getStringExtra("longitude");

                if (latStr != null && lonStr != null) {
                    latitude = Double.parseDouble(latStr);
                    longitude = Double.parseDouble(lonStr);
                }

            } catch (Exception e) {
                Log.e("MapsActivity", "Error parsing coordinates");
            }
        }

        // Load map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Allow user to tap anywhere on the map to add a marker
        enableAddPositionByTap();

        // If coordinates were passed → use them
        if (latitude != 0.0 && longitude != 0.0) {
            LatLng received = new LatLng(latitude, longitude);

            mMap.addMarker(new MarkerOptions()
                    .position(received)
                    .title("Position reçue"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(received, 15));
            return;
        }

        // Otherwise → Get current device location
        loadCurrentLocation();
    }

    /**
      Tap on the map to add a marker and save it
     */
    private void enableAddPositionByTap() {

        mMap.setOnMapClickListener(tappedLatLng -> {

            // Update the active coordinates
            latitude = tappedLatLng.latitude;
            longitude = tappedLatLng.longitude;

            // Clear old markers and add new one
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(tappedLatLng)
                    .title("Nouvelle position sélectionnée"));

            // Show dialog to save
            showAddPositionDialog();
        });
    }

    /**
     * Load current GPS location
     */
    private void loadCurrentLocation() {

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                // Default location → Tunis
                latitude = 36.8065;
                longitude = 10.1815;
            }

            LatLng myPos = new LatLng(latitude, longitude);

            mMap.addMarker(new MarkerOptions()
                    .position(myPos)
                    .title("Position actuelle"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
        });
    }

    /**
     * Dialog: Pseudo + Numéro
     */
    private void showAddPositionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter position");

        // Inputs
        EditText inputPseudo = new EditText(this);
        inputPseudo.setHint("Pseudo");
        inputPseudo.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText inputNumero = new EditText(this);
        inputNumero.setHint("Numéro");
        inputNumero.setInputType(InputType.TYPE_CLASS_PHONE);

        // Layout vertical
        androidx.appcompat.widget.LinearLayoutCompat layout =
                new androidx.appcompat.widget.LinearLayoutCompat(this);
        layout.setOrientation(androidx.appcompat.widget.LinearLayoutCompat.VERTICAL);

        layout.addView(inputPseudo);
        layout.addView(inputNumero);
        builder.setView(layout);

        // Save button
        builder.setPositiveButton("Ajouter", (dialog, which) -> {

            String pseudo = inputPseudo.getText().toString().trim();
            String numero = inputNumero.getText().toString().trim();

            if (pseudo.isEmpty() || numero.isEmpty()) {
                Toast.makeText(this, "Champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute async saving
            new AddPositionTask(
                    this,
                    pseudo,
                    numero,
                    String.valueOf(latitude),
                    String.valueOf(longitude)
            ).execute();
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * AsyncTask to send position to backend
     */
    private static class AddPositionTask extends AsyncTask<Void, Void, Boolean> {

        final String pseudo, numero, latitude, longitude;
        final FragmentActivity activity;

        AddPositionTask(FragmentActivity activity, String p, String n, String lat, String lon) {
            this.activity = activity;
            pseudo = p;
            numero = n;
            latitude = lat;
            longitude = lon;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONParser parser = new JSONParser();

                HashMap<String, String> params = new HashMap<>();
                params.put("pseudo", pseudo);
                params.put("numero", numero);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                parser.makeHttpRequest(Config.URL_Add_Position, "POST", params);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                Toast.makeText(
                        activity,
                        "Position enregistrée avec succès ",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                        activity,
                        "Erreur lors de l’enregistrement ",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
