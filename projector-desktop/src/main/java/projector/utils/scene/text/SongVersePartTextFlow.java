package projector.utils.scene.text;

import com.bence.projector.common.dto.SongVerseProjectionDTO;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import projector.application.Settings;
import projector.model.SongVerse;
import projector.utils.ColorUtil;

import static projector.utils.ColorUtil.getGeneralTextColor;
import static projector.utils.ColorUtil.getSubduedTextColor;

public class SongVersePartTextFlow extends HBox {

    public static final int DESCRIPTION_BORDER_PANE_WIDTH = 20;
    public static final int SPACING = 4;
    private final Text splittedIndexText;
    private SongVerse songVerse;
    private SongVerseProjectionDTO songVerseProjectionDTO;
    private MyTextFlow myTextFlow;
    private final Text sectionTypeText;
    private final BorderPane descriptionBorderPane;
    private final Settings settings = Settings.getInstance();

    public SongVersePartTextFlow() {
        ObservableList<Node> nodes = getChildren();
        descriptionBorderPane = new BorderPane();

        descriptionBorderPane.setPrefWidth(DESCRIPTION_BORDER_PANE_WIDTH);
        sectionTypeText = new Text();
        sectionTypeText.setTextAlignment(TextAlignment.CENTER);
        sectionTypeText.setFill(getGeneralTextColor());
        splittedIndexText = new Text();
        splittedIndexText.setFill(getSubduedTextColor());
        setVisibility(splittedIndexText, false);
        VBox vBox = new VBox();
        vBox.setPrefWidth(0);
        vBox.setPrefHeight(0);
        vBox.setAlignment(Pos.CENTER);
        ObservableList<Node> vBoxChildren = vBox.getChildren();
        vBoxChildren.add(sectionTypeText);
        vBoxChildren.add(splittedIndexText);
        descriptionBorderPane.setCenter(vBox);
        setSpacing(SPACING);

        nodes.add(descriptionBorderPane);
        nodes.add(getMyTextFlow());
    }

    private void setVisibility(Text text, boolean visible) {
        text.setVisible(visible);
        text.setManaged(visible);
    }


    public void setSongVerse(SongVerse songVerse) {
        this.songVerse = songVerse;
        if (songVerse != null) {
            descriptionBorderPane.setStyle("-fx-background-color: " +
                    songVerse.getSectionType().getBackgroundColor(settings.isDarkTheme()) + ";");
            sectionTypeText.setText(songVerse.getSectionTypeStringWithCount());
            if (songVerseProjectionDTO != null) {
                Integer focusedTextIndex = songVerseProjectionDTO.getFocusedTextIndex();
                if (focusedTextIndex != null) {
                    if (songVerseProjectionDTO.isTextsSplit()) {
                        splittedIndexText.setText((focusedTextIndex + 1) + "");
                        setVisibility(splittedIndexText, true);
                    }
                    Border border = getBorder(focusedTextIndex);
                    descriptionBorderPane.setBorder(border);
                }
            }
        }
    }

    private Border getBorder(Integer focusedTextIndex) {
        int top;
        if (focusedTextIndex == 0) {
            top = 2;
        } else {
            top = 0;
        }
        int bottom;
        if (songVerseProjectionDTO.isLastOne()) {
            bottom = 2;
        } else {
            bottom = 0;
        }
        return new Border(new BorderStroke(ColorUtil.getSongVerseBorderColor(),
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(top, 0, bottom, 1)));
    }

    public SongVerse getSongVerse() {
        return songVerse;
    }

    public void setSongVerseProjectionDTO(SongVerseProjectionDTO songVerseProjectionDTO) {
        this.songVerseProjectionDTO = songVerseProjectionDTO;
    }

    public SongVerseProjectionDTO getSongVerseProjectionDTO() {
        return songVerseProjectionDTO;
    }

    public MyTextFlow getMyTextFlow() {
        if (myTextFlow == null) {
            myTextFlow = new MyTextFlow();
        }
        return myTextFlow;
    }

    public void setAWidth(int width) {
        getMyTextFlow().setPrefWidth(getAWidth(width));
    }

    private double getAWidth(int width) {
        return width - descriptionBorderPane.getWidth();
    }

    public void setText2(String string, int width1, int size) {
        getMyTextFlow().setText2(string, (int) getAWidth(width1), size);
    }
}