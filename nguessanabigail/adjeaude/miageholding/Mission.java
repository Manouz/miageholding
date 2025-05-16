package nguessanabigail.adjeaude.miageholding;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Mission {
    private final IntegerProperty id;
    private final IntegerProperty vehiculeId;
    private final StringProperty libelle;
    private final ObjectProperty<LocalDateTime> dateDebut;
    private final ObjectProperty<LocalDateTime> dateFin;
    private final IntegerProperty coutMission;
    private final IntegerProperty coutCarburant;
    private final StringProperty observation;
    private final StringProperty circuit;
    private final StringProperty vehiculeInfo;
    private final StringProperty statut;

    // Constructeur principal
    public Mission(int id, int vehiculeId, String libelle, LocalDateTime dateDebut,
                   LocalDateTime dateFin, int coutMission, int coutCarburant,
                   String observation, String circuit) {
        this.id = new SimpleIntegerProperty(id);
        this.vehiculeId = new SimpleIntegerProperty(vehiculeId);
        this.libelle = new SimpleStringProperty(libelle != null ? libelle : "");
        this.dateDebut = new SimpleObjectProperty<>(dateDebut);
        this.dateFin = new SimpleObjectProperty<>(dateFin);
        this.coutMission = new SimpleIntegerProperty(coutMission);
        this.coutCarburant = new SimpleIntegerProperty(coutCarburant);
        this.observation = new SimpleStringProperty(observation != null ? observation : "");
        this.circuit = new SimpleStringProperty(circuit != null ? circuit : "");
        this.vehiculeInfo = new SimpleStringProperty("");
        this.statut = new SimpleStringProperty(calculateStatut());
    }

    // Constructeur avec informations véhicule
    public Mission(int id, int vehiculeId, String libelle, LocalDateTime dateDebut,
                   LocalDateTime dateFin, int coutMission, int coutCarburant,
                   String observation, String circuit, String vehiculeInfo) {
        this(id, vehiculeId, libelle, dateDebut, dateFin, coutMission, coutCarburant, observation, circuit);
        this.vehiculeInfo.set(vehiculeInfo != null ? vehiculeInfo : "");
    }

    // Méthode privée pour calculer le statut
    private String calculateStatut() {
        try {
            if (dateDebut.get() == null || dateFin.get() == null) {
                return "Non défini";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime debut = dateDebut.get();
            LocalDateTime fin = dateFin.get();

            if (now.isBefore(debut)) {
                return "Planifiée";
            } else if (now.isAfter(fin)) {
                return "Terminée";
            } else {
                return "En cours";
            }
        } catch (Exception e) {
            return "Non défini";
        }
    }

    // Méthode publique pour obtenir le statut (recalcule à chaque appel)
    public String getStatut() {
        String newStatut = calculateStatut();
        statut.set(newStatut);
        return newStatut;
    }

    // Getters
    public int getId() { return id.get(); }
    public int getVehiculeId() { return vehiculeId.get(); }
    public String getLibelle() { return libelle.get(); }
    public LocalDateTime getDateDebut() { return dateDebut.get(); }
    public LocalDateTime getDateFin() { return dateFin.get(); }
    public int getCoutMission() { return coutMission.get(); }
    public int getCoutCarburant() { return coutCarburant.get(); }
    public String getObservation() { return observation.get(); }
    public String getCircuit() { return circuit.get(); }
    public String getVehiculeInfo() { return vehiculeInfo.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty vehiculeIdProperty() { return vehiculeId; }
    public StringProperty libelleProperty() { return libelle; }
    public ObjectProperty<LocalDateTime> dateDebutProperty() { return dateDebut; }
    public ObjectProperty<LocalDateTime> dateFinProperty() { return dateFin; }
    public IntegerProperty coutMissionProperty() { return coutMission; }
    public IntegerProperty coutCarburantProperty() { return coutCarburant; }
    public StringProperty observationProperty() { return observation; }
    public StringProperty circuitProperty() { return circuit; }
    public StringProperty vehiculeInfoProperty() { return vehiculeInfo; }
    public StringProperty statutProperty() {
        statut.set(calculateStatut());
        return statut;
    }

    // Setters
    public void setLibelle(String libelle) {
        this.libelle.set(libelle != null ? libelle : "");
    }
    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut.set(dateDebut);
        this.statut.set(calculateStatut());
    }
    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin.set(dateFin);
        this.statut.set(calculateStatut());
    }
    public void setCoutMission(int coutMission) {
        this.coutMission.set(coutMission);
    }
    public void setCoutCarburant(int coutCarburant) {
        this.coutCarburant.set(coutCarburant);
    }
    public void setObservation(String observation) {
        this.observation.set(observation != null ? observation : "");
    }
    public void setCircuit(String circuit) {
        this.circuit.set(circuit != null ? circuit : "");
    }
    public void setVehiculeInfo(String vehiculeInfo) {
        this.vehiculeInfo.set(vehiculeInfo != null ? vehiculeInfo : "");
    }
}
