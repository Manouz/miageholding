package nguessanabigail.adjeaude.miageholding;
// Classe Personnel pour l'affichage dans la ListView

public class Personnel {

        private int id;
        private String nom;
        private String prenom;
        private String contact;
        private String email;
        private String fonction;
        private String service;

        public Personnel(int id, String nom, String prenom, String contact, String email, String fonction, String service) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.contact = contact;
            this.email = email;
            this.fonction = fonction;
            this.service = service;
        }

        // Getters
        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public String getFonction() { return fonction; }
        public String getService() { return service; }


}
