package bencheikherij.grp4.gestioncontact;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> implements Filterable {
    public interface OnAction {
        void onCall(Contact c);
        void onEdit(Contact c);
        void onDelete(Contact c);
        void onMessage(Contact c);
    }
    private List<Contact> originalList; // li jayin mel BD
    private List<Contact> filteredList; // liste visible à l’écran (modifiée lors de la recherche)
    private OnAction listener; //référence vers Affichage (qui va gérer les clics)

    public ContactAdapter(List<Contact> list, OnAction listener) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
        this.listener = listener;
    }
    // Crée la vue d’un contact à partir du layout  view_contact.xml
     // Retourne un ViewHolder (VH) contenant les références des widgets
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_contact, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(VH holder, int position) {  //kol mabch nscroli ken aandi 4 raw 4+2 =6
        //Remplit les champs visuels (nom, pseudo, image) pour chaque contact affiché.
        Contact c = filteredList.get(position);
        holder.tvNom.setText(c.nom);
        holder.tvPseudo.setText(c.pseudo);
        holder.ivPhoto.setImageResource(R.drawable.image1);
        holder.tvNumero.setText(c.numero); // instead of static text
        holder.btnCall.setOnClickListener(v -> listener.onCall(c));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(c));
        holder.btnMessage.setOnClickListener(v -> listener.onMessage(c));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }


    //Actualise la liste de contacts ( après une suppression ou ajout)
    public void updateData(List<Contact> newList) {
        this.originalList = newList;
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged(); //met à jour l’écran
    }

    // Filter by nom, pseudo or numero
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint == null ? "" : constraint.toString().toLowerCase().trim();
                FilterResults res = new FilterResults();
                //Si le champ de recherche est vide → on affiche tous les contacts.
                //Sinon, on affiche uniquement ceux dont le nom, pseudo ou numéro contient le texte recherché.
                if (q.isEmpty()) {
                    res.values = new ArrayList<>(originalList);
                    res.count = originalList.size();
                } else {
                    List<Contact> filtered = new ArrayList<>();
                    for (Contact c : originalList) {
                        if ((c.nom != null && c.nom.toLowerCase().contains(q)) ||
                                (c.pseudo != null && c.pseudo.toLowerCase().contains(q)) ||
                                (c.numero != null && c.numero.toLowerCase().contains(q))) {
                            filtered.add(c);
                        }
                    }
                    res.values = filtered;
                    res.count = filtered.size();
                }
                return res;
            }


            //Met à jour la liste affichée à l’écran après filtrage
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Contact>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    public void removeContact(Contact contact) {
        originalList.remove(contact);
        filteredList.remove(contact);
        notifyDataSetChanged();
    }


    //Stocke les références des vues d’un contact (évite de refaire findViewById à chaque fois : améliore les performances).
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvNom, tvPseudo, tvNumero;
        ImageButton btnCall, btnEdit, btnDelete, btnMessage;

        VH(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            tvNom = itemView.findViewById(R.id.tv_nom);
            tvPseudo = itemView.findViewById(R.id.tv_pseudo);
            tvNumero = itemView.findViewById(R.id.tv_numero);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnMessage = itemView.findViewById(R.id.btn_message);
        }
    }
}
