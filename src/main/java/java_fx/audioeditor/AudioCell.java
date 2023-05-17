package java_fx.audioeditor;
// Custom ListView cell factory that displays an Image and text
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class AudioCell extends ListCell<AudioFile> {
   private HBox vbox = new HBox(8.0); // 8 points of gap between controls
   private Label label = new Label();

   private Button deleteButton = new Button();

   // constructor configures VBox, ImageView and Label
   public AudioCell() {
      vbox.setAlignment(Pos.CENTER); // center VBox contents horizontally
      label.setWrapText(true); // wrap if text too wide to fit in label
      label.setTextAlignment(TextAlignment.CENTER); // center text
      deleteButton.setText("x");
      vbox.getChildren().add(label); // attach to VBox
      vbox.getChildren().add(deleteButton); // attach to VBox
      deleteButton.setOnAction(
             event -> {
                this.getListView().getItems().remove(
                        this.getIndex()
                );
             }
      );
      setPrefWidth(USE_PREF_SIZE); // use preferred size for cell width
   }

   // called to configure each custom ListView cell
   @Override
   protected void updateItem(AudioFile item, boolean empty) {
//       required to ensure that cell displays properly
      super.updateItem(item, empty);

      if (empty || item == null) {

      }
      else {
         label.setText(item.getName()); // configure Label's text
         setGraphic(vbox); // attach custom layout to ListView cell
      }
   }
}

/**************************************************************************
 * (C) Copyright 1992-2018 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 *************************************************************************/
