package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Affectation {
    private final IntegerProperty id;
    private final IntegerProperty vehiculeId;
    private final IntegerProperty personnelId;
    private final IntegerProperty missionId;
    private final StringProperty typeAffectation;
    private final ObjectProperty<LocalDateTime> dateAffectation;
    private final ObjectProperty<LocalDateTime> dateRetourPrevue;
    private final ObjectProperty<LocalDateTime> dateRetourReelle;
    private final StringProperty statut;
    private final StringProperty commentaire;
    private final StringProperty agentLogistique;

    // Informations supplémentaires pour l'affichage
    private final StringProperty vehiculeInfo;
    private final StringProperty personnelInfo;
    private final StringProperty missionInfo;

    public Affectation(int id, int vehiculeId, Integer personnelId, Integer missionId,
                       String typeAffectation, LocalDateTime dateAffectation,
                       LocalDateTime dateRetourPrevue, LocalDateTime dateRetourReelle,
                       String statut, String commentaire, String agentLogistique) {
        this.id = new SimpleIntegerProperty(id);
        this.vehiculeId = new SimpleIntegerProperty(vehiculeId);
        this.personnelId = personnelId != null ? new SimpleIntegerProperty(personnelId) : null;
        this.missionId = missionId != null ? new SimpleIntegerProperty(missionId) : null;
        this.typeAffectation = new SimpleStringProperty(typeAffectation);
        this.dateAffectation = new SimpleObjectProperty<>(dateAffectation);
        this.dateRetourPrevue = new SimpleObjectProperty<>(dateRetourPrevue);
        this.dateRetourReelle = new SimpleObjectProperty<>(dateRetourReelle);
        this.statut = new SimpleStringProperty(statut);
        this.commentaire = new SimpleStringProperty(commentaire);
        this.agentLogistique = new SimpleStringProperty(agentLogistique);

        this.vehiculeInfo = new SimpleStringProperty("");
        this.personnelInfo = new SimpleStringProperty("");
        this.missionInfo = new SimpleStringProperty("");
    }

    // Getters
    public int getId() { return id.get(); }
    public int getVehiculeId() { return vehiculeId.get(); }
    public Integer getPersonnelId() { return personnelId != null ? personnelId.get() : null; }
    public Integer getMissionId() { return missionId != null ? missionId.get() : null; }
    public String getTypeAffectation() { return typeAffectation.get(); }
    public LocalDateTime getDateAffectation() { return dateAffectation.get(); }
    public LocalDateTime getDateRetourPrevue() { return dateRetourPrevue.get(); }
    public LocalDateTime getDateRetourReelle() { return dateRetourReelle.get(); }
    public String getStatut() { return statut.get(); }
    public String getCommentaire() { return commentaire.get(); }
    public String getAgentLogistique() { return agentLogistique.get(); }
    public String getVehiculeInfo() { return vehiculeInfo.get(); }
    public String getPersonnelInfo() { return personnelInfo.get(); }
    public String getMissionInfo() { return missionInfo.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty vehiculeIdProperty() { return vehiculeId; }
    public IntegerProperty personnelIdProperty() { return personnelId; }
    public IntegerProperty missionIdProperty() { return missionId; }
    public StringProperty typeAffectationProperty() { return typeAffectation; }
    public ObjectProperty<LocalDateTime> dateAffectationProperty() { return dateAffectation; }
    public ObjectProperty<LocalDateTime> dateRetourPrevueProperty() { return dateRetourPrevue; }
    public ObjectProperty<LocalDateTime> dateRetourReelleProperty() { return dateRetourReelle; }
    public StringProperty statutProperty() { return statut; }
    public StringProperty commentaireProperty() { return commentaire; }
    public StringProperty agentLogistiqueProperty() { return agentLogistique; }
    public StringProperty vehiculeInfoProperty() { return vehiculeInfo; }
    public StringProperty personnelInfoProperty() { return personnelInfo; }
    public StringProperty missionInfoProperty() { return missionInfo; }

    // Setters
    public void setStatut(String statut) { this.statut.set(statut); }
    public void setDateRetourReelle(LocalDateTime date) { this.dateRetourReelle.set(date); }
    public void setCommentaire(String commentaire) { this.commentaire.set(commentaire); }
    public void setVehiculeInfo(String info) { this.vehiculeInfo.set(info); }
    public void setPersonnelInfo(String info) { this.personnelInfo.set(info); }
    public void setMissionInfo(String info) { this.missionInfo.set(info); }
}
