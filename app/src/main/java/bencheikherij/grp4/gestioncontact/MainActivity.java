package bencheikherij.grp4.gestioncontact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    EditText ednom, edmp;
    Button btnval, btnqte;
    DBHelper db;
    SharedPreferences prefs;  // pour stocker des paires clé/valeur persistantes.
    SharedPreferences.Editor editor; //Déclaration de l’éditeur qui permet d’écrire/modifier les valeurs dans les SharedPreferences
    CheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ednom = findViewById(R.id.ednom_auth);
        edmp = findViewById(R.id.edmp_auth);
        btnval = findViewById(R.id.btnval_auth);
        btnqte = findViewById(R.id.btnqte_auth);
        db = new DBHelper(this);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        editor = prefs.edit(); //Récupère l’éditeur associé à ces préférences

        cbRemember = findViewById(R.id.cb_remember);

        boolean isRemembered = prefs.getBoolean("remember", false);
        if (isRemembered) {
            String nom = prefs.getString("username", "");
            String mp = prefs.getString("password", "");

            // Vérifie si le compte est encore valide dans la base
            if (db.checkUser(nom, mp)) {
                Intent i = new Intent(MainActivity.this, Acceuil.class);
                i.putExtra("User", nom);
                startActivity(i);
                finish(); // empêche de revenir à l’écran de login
                return; // quitte onCreate pour éviter de relancer le reste
            }
        }


        // Quitter
        btnqte.setOnClickListener(v -> finish());

        // Valider (Login)
        btnval.setOnClickListener(v -> {
            String nom = ednom.getText().toString().trim();
            String mp = edmp.getText().toString().trim();

            if (nom.isEmpty() || mp.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else if (db.checkUser(nom, mp)) {
                // Si l’utilisateur veut se souvenir de sa connexion
                if (cbRemember.isChecked()) {
                    editor.putBoolean("remember", true);
                    editor.putString("username", nom);
                    editor.putString("password", mp);
                    editor.apply();
                } else {
                    editor.clear(); // efface les anciennes données
                    editor.apply();
                }

                Intent i = new Intent(MainActivity.this, Acceuil.class);
                i.putExtra("User", nom);
                startActivity(i);
                Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });


        // Redirection vers inscription
        findViewById(R.id.tv_signup).setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(i);
        });
        // Request runtime permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        2); // use a different request code to distinguish
            }
        }
        ActivityCompat.requestPermissions(this,
                new  String[]{
                        //tab3a android
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        // Manifest.permission.POST_NOTIFICATIONS

                },
                1);




    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);


        if (requestCode == 1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                        grantResults[3] == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
            } else {
                finish();
            }
        }
    }


}


