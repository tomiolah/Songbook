package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.model.Bible;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static projector.utils.KeyEventUtil.getTextFromEvent;

public class ParallelBiblesController {
    @FXML
    private VBox listView;
    private List<Bible> bibles;
    private List<CheckBox> checkBoxes;
    private List<ColorPicker> colorPickers;
    private List<TextField> textFields;
    private BibleController bibleController;

    static void sortParallelBibles(List<Bible> bibles) {
        bibles.sort((o1, o2) -> {
            int parallelNumber = o1.getParallelNumber();
            if (parallelNumber == 0) {
                return 1;
            }
            int o2ParallelNumber = o2.getParallelNumber();
            if (o2ParallelNumber == 0) {
                return -1;
            }
            return Integer.compare(parallelNumber, o2ParallelNumber);
        });
    }

    private static Color getRandomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    public void initialize(List<Bible> bibles) {
        this.bibles = new ArrayList<>(bibles);
        sortParallelBibles(this.bibles);
        checkBoxes = new ArrayList<>(this.bibles.size());
        colorPickers = new ArrayList<>(this.bibles.size());
        textFields = new ArrayList<>(this.bibles.size());
        for (Bible bible : this.bibles) {
            addBibleToVBox(bible);
        }
    }

    private void addBibleToVBox(Bible bible) {
        CheckBox checkBox = new CheckBox(bible.getName() + " - " + bible.getShortName());
        boolean selected = bible.getParallelNumber() > 0;
        checkBox.setSelected(selected);
        ColorPicker colorPicker = new ColorPicker();
        Color color = bible.getColor();
        colorPicker.setValue(Objects.requireNonNullElseGet(color, ParallelBiblesController::getRandomColor));
        TextField textField = new TextField();
        colorPicker.setDisable(!selected);
        textField.setDisable(!selected);
        textField.setText(bible.getParallelNumber() + "");
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!getTextFromEvent(event).matches("[0-9]")) {
                event.consume();
            }
        });
        Platform.runLater(() -> {
            HBox hBox = new HBox(new Label("Nr "), textField);
            listView.getChildren().addAll(checkBox, colorPicker, hBox);
        });
        checkBoxes.add(checkBox);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            colorPicker.setDisable(!newValue);
            textField.setDisable(!newValue);
            if (newValue) {
                int parallelNumber = 0;
                try {
                    parallelNumber = Integer.parseInt(textField.getText().trim());
                } catch (NumberFormatException ignored) {
                }
                if (parallelNumber == 0) {
                    int count = 0;
                    for (CheckBox box : checkBoxes) {
                        if (box.isSelected()) {
                            ++count;
                        }
                    }
                    textField.setText(count * 10 + "");
                }
            }
        });
        colorPickers.add(colorPicker);
        textFields.add(textField);
    }

    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            int i = 0;
            boolean was = false;
            BibleService bibleService = ServiceManager.getBibleService();
            for (Bible bible : bibles) {
                if (checkBoxes.get(i).isSelected()) {
                    bible.setColor(colorPickers.get(i).getValue());
                    try {
                        int parallelNumber = Integer.parseInt(textFields.get(i).getText().trim());
                        if (parallelNumber == 0) {
                            parallelNumber = bibles.size();
                        }
                        bible.setParallelNumber(parallelNumber);
                    } catch (NumberFormatException e) {
                        bible.setParallelNumber(1);
                    }
                    was = true;
                } else {
                    bible.setParallelNumber(0);
                }
                ++i;
                bibleService.update(bible);
            }
            Settings.getInstance().setParallel(was);
            bibleController.sortParallelBibles();
        });
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

}
