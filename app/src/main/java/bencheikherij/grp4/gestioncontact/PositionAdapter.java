package bencheikherij.grp4.gestioncontact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

import bencheikherij.grp4.gestioncontact.Position;
// afficher les positions dans une ListView
public class PositionAdapter extends ArrayAdapter<Position> {
    private Context context;
    private List<Position> positions;

    public PositionAdapter(@NonNull Context context, @NonNull List<Position> positions) {
        super(context, 0, positions);
        this.context = context;
        this.positions = positions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.position_item, parent, false);
        }
        Position p = positions.get(position);
        TextView tvPseudo = convertView.findViewById(R.id.tvPseudo);
        TextView tvNumero = convertView.findViewById(R.id.tvNumero);
        TextView tvCoordinates = convertView.findViewById(R.id.tvCoordinates);

        tvPseudo.setText(p.pseudo);
        tvNumero.setText("Num: " + p.numero);
        tvCoordinates.setText("Lat: " + p.latitude + ", Lon: " + p.longitude);

        return convertView;
    }
}
