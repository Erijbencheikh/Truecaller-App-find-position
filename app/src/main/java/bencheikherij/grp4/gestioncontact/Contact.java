package bencheikherij.grp4.gestioncontact;

public class Contact {
    public int id;         // id dans la BD (AUTO_INCREMENT)
    public String nom;
    public String pseudo;
    public String numero;


    public Contact() {}

    // sans id (pour insertion)
    public Contact(String nom, String pseudo, String numero) {
        this.nom = nom;
        this.pseudo = pseudo;
        this.numero = numero;

    }

    // avec id (pour lecture depuis DB)
    public Contact(int id, String nom, String pseudo, String numero) {
        this.id = id;
        this.nom = nom;
        this.pseudo = pseudo;
        this.numero = numero;

    }

    @Override
    public String toString() {
        return nom + " (" + pseudo + ") - " + numero;
    }
}
