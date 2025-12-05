package bencheikherij.grp4.gestioncontact;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.view.GravityCompat;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class Acceuil extends AppCompatActivity implements ContactAdapter.OnAction {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvContacts;
    private SearchView searchView;
    private ContactAdapter adapter;
    private DBHelper db;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private static final int REQ_CALL = 1001;
    private String pendingCallNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceuil);

        // Setup Drawer + Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String username = getIntent().getStringExtra("User");
        if (username == null) username = "Utilisateur";
        getSupportActionBar().setTitle("Contacts de " + username);
        // je pense hedhom not working
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Database + RecyclerView
        db = new DBHelper(this);
        rvContacts = findViewById(R.id.rv_contacts_acc);
        searchView = findViewById(R.id.edsearch_acc);

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(new ArrayList<>(), this);
        rvContacts.setAdapter(adapter);

        loadContacts();

        // Recherche
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) { adapter.getFilter().filter(s); return true; }
            @Override public boolean onQueryTextChange(String s) { adapter.getFilter().filter(s); return true; }
        });

        // Navigation drawer actions
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Just close the drawer and stay on Accueil
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, Ajout.class));
            } else if (id == R.id.nav_positions) {
                startActivity(new Intent(this, PositionsActivity.class));

            } else if (id == R.id.nav_save_position) {
                // Open MapsActivity to show current position
                Intent i = new Intent(this, MapsActivity.class);
                startActivity(i);
            } else if (id == R.id.nav_logout) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_close) {
                finishAffinity();
            }

            return true;
        });
    }

    private void loadContacts() {
        ArrayList<Contact> list = db.getAllContacts();
        adapter.updateData(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    // Les mêmes méthodes de ton ancien Affichage.java
    @Override
    public void onCall(Contact c) {
        String number = c.numero;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(i);
        } else {
            // mémorise le numéro, demande permission
            pendingCallNumber = number;
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
        }
    }
    @Override
    public void onMessage(Contact c) {  String number = c.numero;

        if (number == null || number.isEmpty()) {
            Toast.makeText(this, "Aucun numéro trouvé pour ce contact", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Vérifie la permission avant d’envoyer le SMS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, "Find Friends : Envoyer moi votre position", null, null);

                Toast.makeText(this, "Message envoyé à " + number, Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 200);
                Toast.makeText(this, "Permission SMS nécessaire pour envoyer le message", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l’envoi du SMS : " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } }

    public void onEdit(Contact c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_contact, null);
        // layoutInflater:transformer un fichier de mise en page XML en objets View
        builder.setView(view);

        EditText edNom = view.findViewById(R.id.ed_nom_edit);
        EditText edPseudo = view.findViewById(R.id.ed_pseudo_edit);
        EditText edNum = view.findViewById(R.id.ed_num_edit);
        Button btnUpdate = view.findViewById(R.id.btn_update_contact);

        // Prefill existing contact data
        edNom.setText(c.nom);
        edPseudo.setText(c.pseudo);
        edNum.setText(c.numero);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnUpdate.setOnClickListener(v -> {
            String nom = edNom.getText().toString().trim();
            String pseudo = edPseudo.getText().toString().trim();
            String num = edNum.getText().toString().trim();

            if (nom.isEmpty() || num.isEmpty()) {
                Toast.makeText(this, "Nom et numéro obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            DBHelper db = new DBHelper(this);
            c.nom = nom;
            c.pseudo = pseudo;
            c.numero = num;
            db.updateContact(c);

            Toast.makeText(this, "Contact mis à jour", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            loadContacts(); // Refresh the RecyclerView
        });
    }

    @Override
    public void onDelete(Contact c) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous supprimer " + c.nom + " ?")
                .setPositiveButton("Oui", (d, w) -> {
                    db.deleteContact(c.id);

                    //  On supprime le contact dans l'adaptateur directement
                    adapter.removeContact(c);

                    Toast.makeText(this, "Supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
