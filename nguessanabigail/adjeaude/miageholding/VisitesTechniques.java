package nguessanabigail.adjeaude.miageholding;
import javafx.beans.property.*;
import java.time.LocalDateTime;

public class VisitesTechniques {
    private final IntegerProperty id;
    private final IntegerProperty vehiculeId;
    private final ObjectProperty<LocalDateTime> dateEntree;
    private final ObjectProperty<LocalDateTime> dateSortie;
    private final StringProperty motif;
    private final StringProperty observation;
    private final IntegerProperty cout;
    private final StringProperty lieu;
    private final StringProperty vehiculeInfo;

    public VisitesTechniques(int id, int vehiculeId, LocalDateTime dateEntree, LocalDateTime dateSortie,
                           String motif, String observation, int cout, String lieu) {
        this.id = new SimpleIntegerProperty(id);
        this.vehiculeId = new SimpleIntegerProperty(vehiculeId);
        this.dateEntree = new SimpleObjectProperty<>(dateEntree);
        this.dateSortie = new SimpleObjectProperty<>(dateSortie);
        this.motif = new SimpleStringProperty(motif != null ? motif : "");
        this.observation = new SimpleStringProperty(observation != null ? observation : "");
        this.cout = new SimpleIntegerProperty(cout);
        this.lieu = new SimpleStringProperty(lieu != null ? lieu : "");
        this.vehiculeInfo = new SimpleStringProperty("");
    }

    public VisitesTechniques(int id, int vehiculeId, LocalDateTime dateEntree, LocalDateTime dateSortie,
                           String motif, String observation, int cout, String lieu, String vehiculeInfo) {
        this(id, vehiculeId, dateEntree, dateSortie, motif, observation, cout, lieu);
        this.vehiculeInfo.set(vehiculeInfo != null ? vehiculeInfo : "");
    }

    // Getters
    public int getId() { return id.get(); }
    public int getVehiculeId() { return vehiculeId.get(); }
    public LocalDateTime getDateEntree() { return dateEntree.get(); }
    public LocalDateTime getDateSortie() { return dateSortie.get(); }
    public String getMotif() { return motif.get(); }
    public String getObservation() { return observation.get(); }
    public int getCout() { return cout.get(); }
    public String getLieu() { return lieu.get(); }
    public String getVehiculeInfo() { return vehiculeInfo.get(); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty vehiculeIdProperty() { return vehiculeId; }
    public ObjectProperty<LocalDateTime> dateEntreeProperty() { return dateEntree; }
    public ObjectProperty<LocalDateTime> dateSortieProperty() { return dateSortie; }
    public StringProperty motifProperty() { return motif; }
    public StringProperty observationProperty() { return observation; }
    public IntegerProperty coutProperty() { return cout; }
    public StringProperty lieuProperty() { return lieu; }
    public StringProperty vehiculeInfoProperty() { return vehiculeInfo; }

    // Setters
    public void setDateEntree(LocalDateTime dateEntree) { this.dateEntree.set(dateEntree); }
    public void setDateSortie(LocalDateTime dateSortie) { this.dateSortie.set(dateSortie); }
    public void setMotif(String motif) { this.motif.set(motif != null ? motif : ""); }
    public void setObservation(String observation) { this.observation.set(observation != null ? observation : ""); }
    public void setCout(int cout) { this.cout.set(cout); }
    public void setLieu(String lieu) { this.lieu.set(lieu != null ? lieu : ""); }
    public void setVehiculeInfo(String vehiculeInfo) { this.vehiculeInfo.set(vehiculeInfo != null ? vehiculeInfo : ""); }
}
