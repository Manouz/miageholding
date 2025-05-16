package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Vehicule {
    private final IntegerProperty id;
    private final StringProperty marque;
    private final StringProperty modele;
    private final StringProperty immatriculation;
    private final StringProperty numeroChassis;
    private final IntegerProperty nbPlaces;
    private final StringProperty energie;
    private final ObjectProperty<LocalDate> dateAcquisition;
    private final ObjectProperty<LocalDate> dateAmortissement;
    private final ObjectProperty<LocalDate> dateMiseEnService;
    private final IntegerProperty puissance;
    private final StringProperty couleur;
    private final IntegerProperty prix;
    private final StringProperty etat;
    private final ObjectProperty<LocalDate> dateEtat;

    public Vehicule(int id, String marque, String modele, String immatriculation,
                    int nbPlaces, String energie, String etat, LocalDate dateAcquisition,
                    int puissance, String couleur, int prix) {
        this.id = new SimpleIntegerProperty(id);
        this.marque = new SimpleStringProperty(marque);
        this.modele = new SimpleStringProperty(modele);
        this.immatriculation = new SimpleStringProperty(immatriculation);
        this.numeroChassis = new SimpleStringProperty("");
        this.nbPlaces = new SimpleIntegerProperty(nbPlaces);
        this.energie = new SimpleStringProperty(energie);
        this.dateAcquisition = new SimpleObjectProperty<>(dateAcquisition);
        this.dateAmortissement = new SimpleObjectProperty<>();
        this.dateMiseEnService = new SimpleObjectProperty<>();
        this.puissance = new SimpleIntegerProperty(puissance);
        this.couleur = new SimpleStringProperty(couleur);
        this.prix = new SimpleIntegerProperty(prix);
        this.etat = new SimpleStringProperty(etat);
        this.dateEtat = new SimpleObjectProperty<>();
    }

    // Getters
    public int getId() { return id.get(); }
    public String getMarque() { return marque.get(); }
    public String getModele() { return modele.get(); }
    public String getImmatriculation() { return immatriculation.get(); }
    public String getNumeroChassis() { return numeroChassis.get(); }
    public int getNbPlaces() { return nbPlaces.get(); }
    public String getEnergie() { return energie.get(); }
    public LocalDate getDateAcquisition() { return dateAcquisition.get(); }
    public LocalDate getDateAmortissement() { return dateAmortissement.get(); }
    public LocalDate getDateMiseEnService() { return dateMiseEnService.get(); }
    public int getPuissance() { return puissance.get(); }
    public String getCouleur() { return couleur.get(); }
    public int getPrix() { return prix.get(); }
    public String getEtat() { return etat.get(); }
    public LocalDate getDateEtat() { return dateEtat.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty marqueProperty() { return marque; }
    public StringProperty modeleProperty() { return modele; }
    public StringProperty immatriculationProperty() { return immatriculation; }
    public StringProperty numeroChassiProperty() { return numeroChassis; }
    public IntegerProperty nbPlacesProperty() { return nbPlaces; }
    public StringProperty energieProperty() { return energie; }
    public ObjectProperty<LocalDate> dateAcquisitionProperty() { return dateAcquisition; }
    public ObjectProperty<LocalDate> dateAmortissementProperty() { return dateAmortissement; }
    public ObjectProperty<LocalDate> dateMiseEnServiceProperty() { return dateMiseEnService; }
    public IntegerProperty puissanceProperty() { return puissance; }
    public StringProperty couleurProperty() { return couleur; }
    public IntegerProperty prixProperty() { return prix; }
    public StringProperty etatProperty() { return etat; }
    public ObjectProperty<LocalDate> dateEtatProperty() { return dateEtat; }

    // Setters
    public void setNumeroChassis(String numeroChassis) { this.numeroChassis.set(numeroChassis); }
    public void setDateAmortissement(LocalDate dateAmortissement) { this.dateAmortissement.set(dateAmortissement); }
    public void setDateMiseEnService(LocalDate dateMiseEnService) { this.dateMiseEnService.set(dateMiseEnService); }
    public void setDateEtat(LocalDate dateEtat) { this.dateEtat.set(dateEtat); }
}