import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class PlayerView implements Comparable<PlayerView>{
    private Node image;
    private Label label;
    private IntegerProperty treasureCollected = new SimpleIntegerProperty();
    private StringProperty name = new SimpleStringProperty();
    private boolean quit=false;

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public void setImage(Node image) {
        this.image = image;
    }

    public void setLabelWithName(Label label, String name) {
        this.label = label;
        this.name.set(name);
    }

    public void setTreasureCollected(int treasureCollected) {
        this.treasureCollected.set(treasureCollected);
    }

    public Node getImage() {

        return image;
    }

    public Label getLabel() {
        return label;
    }

    public int getTreasureCollected() {
        return treasureCollected.get();
    }

    public String getName() {
        return name.get();
    }

    public final StringProperty nameProperty() {
        return name;
    }

    public final IntegerProperty treasureCollectedProperty() {
        return treasureCollected;
    }

    public boolean containsLabel() {
        return label != null;
    }

    public boolean containsImage() {
        return image != null;
    }

    @Override
    public int compareTo(PlayerView o) {
        return this.getTreasureCollected()-o.getTreasureCollected();
    }
}
