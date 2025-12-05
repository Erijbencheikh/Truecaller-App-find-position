package bencheikherij.grp4.gestioncontact;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class SignupActivity extends AppCompatActivity {
    EditText eduser, edpass, edconfirm;
    Button btnCreate, btnBack;
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DBHelper(this);
        eduser = findViewById(R.id.ed_username);
        edpass = findViewById(R.id.ed_password);
        edconfirm = findViewById(R.id.ed_confirm);
        btnCreate = findViewById(R.id.btn_create);
        btnBack = findViewById(R.id.btn_back);

        btnCreate.setOnClickListener(v -> {
            String u = eduser.getText().toString().trim();
            String p = edpass.getText().toString().trim();
            String c = edconfirm.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else if (!p.equals(c)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            } else if (db.checkUsernameExists(u)) {
                Toast.makeText(this, "Nom d'utilisateur déjà existant", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = db.registerUser(u, p);
                if (success) {
                    Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(v -> finish()); // l’activité actuelle se ferme (revient à l’écran précédent).
    }
}
