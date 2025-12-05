package bencheikherij.grp4.gestioncontact;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
public class Ajout extends AppCompatActivity {

    private EditText ednom_ajout, edpseudo_ajout, ednum_ajout;
    private Button btnajout, btnannuler;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DBHelper(this);

        ednom_ajout = findViewById(R.id.ednom_ajout);
        edpseudo_ajout = findViewById(R.id.edpseu_ajout);
        ednum_ajout = findViewById(R.id.ednum_ajout);
        btnajout = findViewById(R.id.btnajou_ajout);
        btnannuler = findViewById(R.id.btnannuler_ajout);
        btnannuler.setOnClickListener(v -> finish());
        btnajout.setOnClickListener(v -> {
            String nom = ednom_ajout.getText().toString().trim();
            String pseudo = edpseudo_ajout.getText().toString().trim();
            String num = ednum_ajout.getText().toString().trim();

            if (nom.isEmpty() || num.isEmpty()) {
                Toast.makeText(Ajout.this, "Nom et numéro obligatoires", Toast.LENGTH_SHORT).show();
                return;
            } else {
                // add new contact
                Contact c = new Contact(nom, pseudo, num);
                db.addContact(c);
                Toast.makeText(Ajout.this, "Ajouté", Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK);
            finish();
        });
    }

}