package bencheikherij.grp4.gestioncontact;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
//afficher la liste de toutes les positions enregistrées sur ton serveur
public class PositionsActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    private ListView lvPositions;
    private ArrayList<Position> data = new ArrayList<>();
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Liste des positions");

        // Handle back arrow tap
        toolbar.setNavigationOnClickListener(v -> finish());
        lvPositions = findViewById(R.id.lv_positions);
        // Start fetching positions immediately
        new DownloadTask().execute();
    }

    private class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(PositionsActivity.this);
            builder.setTitle("Téléchargement");
            builder.setMessage("Veuillez patienter...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONParser jParser = new JSONParser();
            JSONObject response = jParser.makeHttpRequest(Config.URL_GetAll_Position, "GET", null);
            Log.d("PositionsActivity", "Server raw response: " + response);

            if (response == null) {
                Log.e("PositionsActivity", "Response is null. Check URL or PHP script!");
                return false;
            }

            Log.d("PositionsActivity", "Server response: " + response.toString());

            try {
                int success = response.optInt("success", 0);
                if (success != 1) {
                    Log.e("PositionsActivity", "Server returned success=0");
                    return false;
                }

                data.clear();
                JSONArray positions = response.optJSONArray("positions");
                if (positions == null) {
                    Log.e("PositionsActivity", "No positions array in JSON");
                    return false;
                }

                for (int i = 0; i < positions.length(); i++) {
                    JSONObject obj = positions.optJSONObject(i);
                    if (obj == null) continue;

                    // Read everything as string first
                    // Get idposition as string first
                    String idStr = obj.optString("idposition", "0");
                    int id = 0;
                    try {
                        id = Integer.parseInt(idStr); // safely convert to int
                    } catch (NumberFormatException e) {
                        Log.e("PositionsActivity", "Invalid idposition: " + idStr);
                    }

// Get other attributes safely
                    String pseudo = obj.optString("pseudo", "");
                    String numero = obj.optString("numero", "");
                    String longitude = obj.optString("longitude", "");
                    String latitude = obj.optString("latitude", "");

// Add to list
                    data.add(new Position(id, pseudo, numero, longitude, latitude));
                }

                return true;

            } catch (Exception e) {
                Log.e("PositionsActivity", "Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }

            if (success) {
                PositionAdapter adapter = new PositionAdapter(PositionsActivity.this, data);
                lvPositions.setAdapter(adapter);
            } else {
                Toast.makeText(PositionsActivity.this,
                        "Échec du chargement des positions", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
