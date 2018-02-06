package projector.utils.scene.text;

import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.PrismTextLayout;
import com.sun.javafx.text.TextLine;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Adds additional API to {@link TextFlow}.
 */
@SuppressWarnings("restriction")
public class MyTextFlow extends TextFlow {

    private static final Logger LOG = LoggerFactory.getLogger(MyTextFlow.class);

    private static Method mGetTextLayout;
    private static Method mGetLines;

    static {
        Method mGetLineIndex;
        Method mGetCharCount;
        try {
            mGetTextLayout = TextFlow.class.getDeclaredMethod("getTextLayout");
            mGetLines = PrismTextLayout.class.getDeclaredMethod("getLines");
            mGetLineIndex = PrismTextLayout.class.getDeclaredMethod("getLineIndex", float.class);
            mGetCharCount = PrismTextLayout.class.getDeclaredMethod("getCharCount");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        mGetTextLayout.setAccessible(true);
        mGetLines.setAccessible(true);
        mGetLineIndex.setAccessible(true);
        mGetCharCount.setAccessible(true);
    }

    private int size = 100;
    private String fontFamily;
    private FontWeight fontWeight = FontWeight.BOLD;
    private String rawText;
    private int height;
    private int width;
    private List<Text> texts = new ArrayList<>();
    private List<Text> letters = new ArrayList<>();
    private String colorStartTag = "<color=\"0x";
    private boolean tmp = false;
    private double total;
    private boolean wrapped;
    private MyTextFlow tmpTextFlow;
    private ReadOnlyObjectProperty<Bounds> boundsReadOnlyObjectProperty;
    private Text prevText = null;
    private boolean prevItalic = false;
    private Color prevColor = null;

    public MyTextFlow() {
    }

    private MyTextFlow(boolean tmp) {
        this.tmp = tmp;
        boundsReadOnlyObjectProperty = boundsInLocalProperty();
    }

    private static Object invoke(Method m, Object obj, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringTextFromRawText(String rawText) {
        if (rawText == null) {
            return "";
        }
        char[] chars = rawText.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '\\') {
                chars[i] = 0;
                ++i;
            } else if (chars[i] == '&') {
                chars[i] = '\n';
            }
        }
        rawText = String.valueOf(chars);
        return rawText;
    }

    private int getLineCount() {
        return getLines().length;
    }

    private TextLine[] getLines() {
        return (TextLine[]) invoke(mGetLines, textLayout());
    }

    private TextLayout textLayout() {
        return (TextLayout) invoke(mGetTextLayout, this);
    }

    public void setText2(String newText, int width, int height) {
        this.width = width;
        this.height = height;
        this.rawText = newText;
        Settings settings = Settings.getInstance();
        fontFamily = settings.getFont();
        fontWeight = settings.getFontWeight();
        newText = ampersandToNewLine(newText);
        String text2 = newText;
        setTextsByCharacters(text2);
        getChildren().clear();
        getChildren().addAll(texts);
        if (!tmp) {
            initializeTmpTextFlow();
            tmpTextFlow.setText2(newText, width, height);
            tmpTextFlow.maximizeSize(width, height);
            size = tmpTextFlow.size;
            setSize(size);
            setSize(size, letters);
            if (size > 10) {
                tmpTextFlow.getChildren().clear();
                tmpTextFlow.getChildren().addAll(letters);
                tmpTextFlow.calculateMaxSizeByLetters(height);
                size = tmpTextFlow.size;
                setSize(size);
                setSize(size, letters);
                maximizeSize(width, height);
                tmpTextFlow.getChildren().clear();
                wrapBetter();
            }
        }
    }

    private void wrapBetter() {
        try {
            List<Phrase> phrases = getPhrases();
            getChildren().clear();
            for (Phrase phrase : phrases) {
                wrapBetter(phrase);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void clearSpacesFromEnd(List<Phrase> lines) {
        List<Phrase> phraseList;
        int size = lines.size();
        if (size > 1) {
            if (!wrapped) {
                phraseList = lines;
            } else {
                phraseList = new ArrayList<>(size - 1);
                phraseList.addAll(lines.subList(0, size - 2));
                phraseList.add(lines.get(size - 1));
            }
        } else {
            phraseList = lines;
        }
        for (Phrase phrase : phraseList) {
            List<Word> words = phrase.getWords();
            size = words.size();
            if (size > 0) {
                Word word = words.get(size - 1);
                List<Text> letters = word.getLetters();
                int lettersSize = letters.size();
                if (lettersSize > 0) {
                    Text text = letters.get(lettersSize - 1);
                    String text1 = text.getText();
                    if (text1.equals(" ")) {
                        text.setText("\n");
                    } else {
                        if (text1.equals("\n")) {
                            if (lettersSize > 1) {
                                text = letters.get(lettersSize - 2);
                                if (text.getText().equals(" ")) {
                                    text.setText("DELETED");
                                }
                            } else {
                                if (size > 1) {
                                    word = words.get(size - 2);
                                    letters = word.getLetters();
                                    lettersSize = letters.size();
                                    if (lettersSize > 0) {
                                        text = letters.get(lettersSize - 1);
                                        text1 = text.getText();
                                        if (text1.equals(" ")) {
                                            text.setText("DELETED");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Phrase phrase : lines) {
            for (Word word : phrase.getWords()) {
                for (Text text : word.getLetters()) {
                    if (!text.getText().equals("DELETED")) {
                        getChildren().add(text);
                    }
                }
            }
        }
    }

    private void wrapBetter(Phrase phrase) {
        int width = (int) (this.getPrefWidth() + this.width) / 2;
        tmpTextFlow.resize(width, height);
        ObservableList<Node> nodes = tmpTextFlow.getChildren();
        nodes.clear();
        Phrase letters = new Phrase();
        List<Phrase> lines = new ArrayList<>();
        for (Word word : phrase.getWords()) {
            nodes.addAll(word.getClonedLetters());
            if (tmpTextFlow.getLineCount() > 1 + word.getNewLineCount()) {
                nodes.clear();
                nodes.addAll(word.getClonedLetters());
                if (letters.getWords().size() > 0) {
                    lines.add(letters);
                    letters = new Phrase();
                }
                letters.addWord(word);
            } else {
                letters.addWord(word);
            }
        }
        lines.add(letters);
        int size = lines.size();
        if (size > 1) {
            Phrase penultimateLine = lines.get(size - 2);
            double sum = penultimateLine.getWidth();
            Phrase lastLine = lines.get(size - 1);
            double sum2 = lastLine.getWidth();
            double average = (sum + sum2) / 2;
            total = 0d;
            double max = Math.max(sum, sum2);
            wrapped = false;
            wrap(penultimateLine, average, max);
            if (!wrapped) {
                wrap(lastLine, average, max);
                if (wrapped) {
                    LOG.error(rawText + " " + average + " " + max);
                }
            }
        }
        clearSpacesFromEnd(lines);
    }

    private void wrap(Phrase line, double average, double max) {
        Word prev = null;
        for (Word word : line.getWords()) {
            total += word.getWidth();
            if (total > max) {
                return;
            }
            if (total > average) {
                List<Text> wordLetters;
                if (total - average < Math.abs(total - average - word.getWidth()) || prev == null) {
                    wordLetters = word.getLetters();
                } else {
                    wordLetters = prev.getLetters();
                }
                Text text = wordLetters.get(wordLetters.size() - 1);
                text.setText("\n");
                wrapped = true;
                return;
            }
            prev = word;
        }
    }

    private List<Phrase> getPhrases() {
        List<Phrase> phrases = new ArrayList<>();
        Phrase phrase = new Phrase();
        Word word = new Word();
        for (Text letter : letters) {
            if (letter.getText().equals("\n")) {
                word.addLetter(letter);
                phrase.addWord(word);
                word = new Word();
                phrases.add(phrase);
                phrase = new Phrase();
            } else {
                word.addLetter(letter);
                char ch = letter.getText().charAt(0);
                if (Character.isWhitespace(ch)) {
                    phrase.addWord(word);
                    word = new Word();
                }
            }
        }
        phrase.addWord(word);
        phrases.add(phrase);
        return phrases;
    }

    private void maximizeSize(int trueWidth, int height) {
        double lineSpace = Settings.getInstance().getLineSpace();
        setLineSpacing(lineSpace);
        double aDouble = 1.0;
        int width = (int) (trueWidth * aDouble);
        setLayoutY(0);
        if (tmp) {
            resize(width, 20);
            calculateMaxSize(trueWidth, height);
        } else {
            setLayoutX(0);
            setPrefWidth(width);
            setPrefHeight(height);
            int w;
            int h;
            w = (int) tmpTextFlow.boundsInLocalProperty().getValue().getWidth();
            h = (int) tmpTextFlow.boundsInLocalProperty().getValue().getHeight();
            resize(width, height);
            setLayoutX((double) (trueWidth - w) / 2);
            setLayoutY((double) (height - h) / 2);
            setPrefHeight(h);
        }
    }

    private void setTextsByCharacters(String text) {
        texts.clear();
        letters.clear();
        prevColor = null;
        boolean italic = false;
        int length = text.length();
        Stack<Color> colors = new Stack<>();
        colors.push(Settings.getInstance().getColor());
        for (int i = 0; i < length; ++i) {
            char s = text.charAt(i);
            String colorEndTag = "</color>";
            if (s == '[') {
                italic = true;
            } else if (italic && s == ']') {
                italic = false;
            } else if (isaColorStartTag(text, i, s)) {
                try {
                    int beginIndex = i + colorStartTag.length();
                    String color = "0x" + text.substring(beginIndex, beginIndex + 8);
                    colors.push(Color.web(color));
                    i += colorStartTag.length() + 9;
                } catch (IllegalArgumentException ignored) {
                    addCharacter(italic, s, colors.peek());
                }
            } else if (s == '<' && text.substring(i, i + colorEndTag.length()).equals(colorEndTag)) {
                if (colors.size() > 1) {
                    colors.pop();
                }
                i += colorEndTag.length() - 1;
            } else {
                addCharacter(italic, s, colors.peek());
            }
        }
    }

    private void addCharacter(boolean italic, char s, Color color) {
        letters.add(getText(italic, s, color));
        if (prevItalic == italic && color.equals(prevColor)) {
            prevText.setText(prevText.getText() + s);
            return;
        }
        Text e = getText(italic, s, color);
        texts.add(e);
        prevItalic = italic;
        prevColor = color;
        prevText = e;
    }

    private Text getText(boolean italic, char s, Color color) {
        Text e = new Text(s + "");
        FontPosture fontPosture;
        if (italic) {
            fontPosture = FontPosture.ITALIC;
        } else {
            fontPosture = FontPosture.REGULAR;
        }
        e.setFont(Font.font(fontFamily, fontWeight, fontPosture, Font.getDefault().getSize()));
        e.setFill(color);
        return e;
    }

    private boolean isaColorStartTag(String text, int i, char s) {
        try {
            return s == '<' && text.substring(i, i + colorStartTag.length()).equals(colorStartTag);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    public void setColor(Color value) {
        ObservableList<Node> nodes = getChildren();
        for (Node node : nodes) {
            ((Text) node).setFill(value);
        }
    }

    private void calculateMaxSize(int trueWidth, int height) {
        int w, h;
        Settings settings = Settings.getInstance();
        size = (int) (settings.getMaxFont() * (((double) (height)) / (double) (768)));
        int size2 = (int) (settings.getMaxFont() * (((double) (trueWidth)) / (double) (1366)));
        if (size2 > size) {
            size = size2;
        }
        int a = 2, b = size;
        do {
            setSize(size);
            if (trueWidth == 0) {
                return;
            }
            Bounds value = boundsReadOnlyObjectProperty.getValue();
            w = (int) value.getWidth();
            h = (int) value.getHeight();
            if (size > 1) {
                double i = w;
                i /= trueWidth;
                if (i > 1.03 || h > height) {
                    if (a < b) {
                        b = size - 1;
                        size = (a + b) / 2;
                    } else {
                        do {
                            setSize(--size);
                            value = boundsReadOnlyObjectProperty.getValue();
                            w = (int) value.getWidth();
                            h = (int) value.getHeight();
                            i = w;
                            i /= trueWidth;
                        } while ((i > 1.01 || h > height) && size > 1);
                        break;
                    }
                } else {
                    if (a < b) {
                        a = size + 1;
                        size = (a + b) / 2;
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        } while (true);
    }

    private void calculateMaxSizeByLetters(int height) {
        int h;
        Bounds value = boundsReadOnlyObjectProperty.getValue();
        h = (int) value.getHeight();
        while ((h > height) && size > 1) {
            setSize(--size);
            value = boundsReadOnlyObjectProperty.getValue();
            h = (int) value.getHeight();
        }
    }

    private String ampersandToNewLine(String newText) {
        newText = getStringTextFromRawText(newText);
        return newText;
    }

    public String getRawText() {
        return rawText;
    }

    public void setBackGroundColor() {
        final Settings settings = Settings.getInstance();
        if (!settings.isBackgroundImage()) {
            BackgroundFill myBF = new BackgroundFill(settings.getBackgroundColor(), new CornerRadii(1),
                    new Insets(0.0, 0.0, 0.0, 0.0));
            // then you set to your node
            super.setBackground(new Background(myBF));
        } else {
            setBackGroundImage();
        }
    }

    private void setBackGroundImage() {
        final Settings settings = Settings.getInstance();
        if (settings.isBackgroundImage()) {
            super.setBackground(new Background(new BackgroundImage(
                    new Image(settings.getBackgroundImagePath(), width, height, false, true), BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        }
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        setText2(rawText, width, height);
    }

    public int getSize() {
        return size;
    }

    private void setSize(int size) {
        this.size = size;
        ObservableList<Node> nodes = getChildren();
        for (Node node : nodes) {
            Text text = (Text) node;
            if (text.getFont().getStyle().contains("Italic")) {
                text.setFont(Font.font(text.getFont().getFamily(), fontWeight, FontPosture.ITALIC, size));
            } else {
                text.setFont(Font.font(fontFamily, fontWeight, FontPosture.REGULAR, size));
            }
        }
    }

    private void setSize(int size, List<Text> texts) {
        this.size = size;
        for (Text text : texts) {
            if (text.getFont().getStyle().contains("Italic")) {
                text.setFont(Font.font(text.getFont().getFamily(), fontWeight, FontPosture.ITALIC, size));
            } else {
                text.setFont(Font.font(fontFamily, fontWeight, FontPosture.REGULAR, size));
            }
        }
    }

    public void setSizeAndAlign(int size) {
        setSize(size);
        align();
    }

    private void align() {
        int w;
        int h;
        resize(width, 20);
        w = (int) boundsInLocalProperty().getValue().getWidth();
        h = (int) boundsInLocalProperty().getValue().getHeight();
        resize(width, height);
        setLayoutX((double) (width - w) / 2);
        setLayoutY((double) (height - h) / 2);
    }

    private void initializeTmpTextFlow() {
        if (tmpTextFlow == null) {
            tmpTextFlow = new MyTextFlow(true);
        }
    }
}