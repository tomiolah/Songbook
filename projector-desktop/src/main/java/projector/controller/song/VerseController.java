package projector.controller.song;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.SongVerse;

import static projector.utils.scene.text.MyTextFlow.getStringTextFromRawText;

public class VerseController {

    private static final Logger LOG = LoggerFactory.getLogger(VerseController.class);
    @FXML
    private BorderPane rightBorderPane;
    @FXML
    private TextArea secondTextArea;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleButton chorusToggleButton;
    private SongVerse songVerse;

    public void initialize() {
        chorusToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chorusToggleButton.setStyle("-fx-base: lightgreen;");
            } else {
                chorusToggleButton.setStyle("-fx-base: #e6e6e6;");
            }
        });
        chorusToggleButton.setFocusTraversable(false);
        showSecondText(false);
    }

    String getRawText() {
        final String textAreaText = textArea.getText();
        if (textAreaText != null) {
            String text = textAreaText.trim();
            char[] chars = text.toCharArray();
            StringBuilder newText = new StringBuilder();
            for (int i = 0; i < chars.length; ++i) {
                if (chars[i] == '\\') {
                    newText.append("\\\\");
                } else if (chars[i] == '&') {
                    newText.append("\\&");
                } else if (chars[i] == '\n' && i + 1 < chars.length && chars[i + 1] == '\n') {
                    newText.append("&\n");
                    ++i;
                } else {
                    newText.append(chars[i]);
                }
            }
            return newText.toString();
        }
        return "";
    }

    TextArea getTextArea() {
        return textArea;
    }

    public TextArea getSecondTextArea() {
        return secondTextArea;
    }

    SongVerse getSongVerse() {
        songVerse.setChorus(chorusToggleButton.isSelected());
        songVerse.setText(textArea.getText().trim());
        songVerse.setSecondText(secondTextArea.getText());
        return songVerse;
    }

    void setSongVerse(SongVerse songVerse) {
        this.songVerse = songVerse;
        chorusToggleButton.setSelected(songVerse.isChorus());
        textArea.setText(getStringTextFromRawText(songVerse.getText()));
        try {
            String secondText = songVerse.getSecondText();
            if (secondText == null) {
                secondText = "";
            }
            secondTextArea.setText(secondText);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void showSecondText(boolean showSecondText) {
        try {
            if (showSecondText) {
                rightBorderPane.setCenter(secondTextArea);
            } else {
                rightBorderPane.setCenter(null);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
