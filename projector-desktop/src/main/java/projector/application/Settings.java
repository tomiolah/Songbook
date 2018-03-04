package projector.application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import projector.controller.song.util.OrderMethod;
import projector.model.Bible;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;

public class Settings {

    private static Settings instance = null;
    private List<Bible> bibles;
    private int maxFont = 80;
    private boolean withAccents = false;
    private int currentBible = 0;
    private Color backgroundColor = Color.WHITE;
    private Color color = Color.BLACK;
    private Color parallelBibleColor = Color.BLUE;
    private BackgroundImage backgroundImage;
    private String backgroundImagePath;
    private boolean isBackgroundImage = false;
    private boolean isFastMode = false;
    private boolean isParallel = false;
    private String parallelBiblePath = " ";
    private String font = "system";
    private double lineSpace = 10;
    private String fontWeight = "NORMAL";
    private boolean showReferenceOnly = false;
    private boolean referenceItalic = true;
    private boolean logging = false;
    private int parallelBibleIndex = 0;
    private double previewX;
    private double previewY;
    private double previewWidth;
    private double previewHeight;
    private boolean previewLoadOnStart;
    private double songTabHorizontalSplitPaneDividerPosition = 0.3753943217665615;
    private double songTabVerticalSplitPaneDividerPosition = 0.7344632768361582;
    private double bibleTabHorizontalSplitPaneDividerPosition = 0.2572314049586777;
    private double bibleTabVerticalSplitPaneDividerPosition = 0.6777546777546778;
    private double mainHeight;
    private double mainWidth;
    private boolean referenceChapterSorting;
    private boolean referenceVerseSorting;
    private Locale preferredLanguage = new Locale("hu", "HU");
    private ResourceBundle resourceBundle;
    private double songHeightSliderValue = 250;
    private double verseListViewFontSize = 24;
    private boolean shareOnNetwork = false;
    private BooleanProperty connectedToShared = new SimpleBooleanProperty(false);

    private SimpleBooleanProperty showProgressLine = new SimpleBooleanProperty(true);
    private Color progressLineColor = new Color(1.0, 1.0, 1.0, 0.7);
    private SimpleBooleanProperty progressLinePositionIsTop = new SimpleBooleanProperty(true);
    private OrderMethod songOrderMethod = OrderMethod.BY_COLLECTION;

    protected Settings() {
        load();
    }

    public synchronized static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public synchronized List<String> getBibleTitles() {
        List<String> bibleTitles = new ArrayList<>(bibles.size());
        for (Bible bible : bibles) {
            bibleTitles.add(bible.getName());
        }
        return bibleTitles;
    }

    public synchronized List<String> getBiblePaths() {
        List<String> biblePaths = new ArrayList<>(bibles.size());
        for (Bible bible : bibles) {
            biblePaths.add(bible.getPath());
        }
        return biblePaths;
    }

    public synchronized int getMaxFont() {
        return maxFont;
    }

    public synchronized void setMaxFont(int maxFont) {
        this.maxFont = maxFont;
    }

    public synchronized boolean isWithAccents() {
        return withAccents;
    }

    public synchronized void setWithAccents(boolean withAccents) {
        this.withAccents = withAccents;
    }

    public synchronized int getCurrentBible() {
        return currentBible;
    }

    public synchronized void setCurrentBible(int currentBible) {
        this.currentBible = currentBible;
    }

    public synchronized Color getBackgroundColor() {
        return backgroundColor;
    }

    public synchronized void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public synchronized Color getColor() {
        return color;
    }

    public synchronized void setColor(Color color) {
        this.color = color;
    }

    public synchronized BackgroundImage getBackgroundImage() {
        return backgroundImage;
    }

    public synchronized void setBackgroundImage(BackgroundImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public synchronized boolean isBackgroundImage() {
        return isBackgroundImage;
    }

    public synchronized void setBackgroundImage(boolean isBackgroundImage) {
        this.isBackgroundImage = isBackgroundImage;
    }

    public synchronized String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public synchronized void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public synchronized boolean isFastMode() {
        return isFastMode;
    }

    public synchronized void setFastMode(boolean isFastMode) {
        this.isFastMode = isFastMode;
    }

    public synchronized boolean isParallel() {
        return isParallel;
    }

    public synchronized void setParallel(boolean isParallel) {
        this.isParallel = isParallel;
    }

    public synchronized String getParallelBiblePath() {
        return parallelBiblePath;
    }

    public synchronized void setParallelBiblePath(String parallelBiblePath) {
        this.parallelBiblePath = parallelBiblePath;
    }

    public synchronized String getFont() {
        return font;
    }

    public synchronized void setFont(String font) {
        this.font = font;
    }

    public synchronized double getLineSpace() {
        return lineSpace;
    }

    public synchronized void setLineSpace(double lineSpace) {
        this.lineSpace = lineSpace;
    }

    public synchronized FontWeight getFontWeight() {
        FontWeight tmp;
        switch (fontWeight) {
            case "BOLD":
                tmp = FontWeight.BOLD;
                break;
            case "BLACK":
                tmp = FontWeight.BLACK;
                break;
            case "EXTRA_BOLD":
                tmp = FontWeight.EXTRA_BOLD;
                break;
            case "EXTRA_LIGHT":
                tmp = FontWeight.EXTRA_LIGHT;
                break;
            case "LIGHT":
                tmp = FontWeight.LIGHT;
                break;
            case "MEDIUM":
                tmp = FontWeight.MEDIUM;
                break;
            case "SEMI_BOLD":
                tmp = FontWeight.SEMI_BOLD;
                break;
            case "THIN":
                tmp = FontWeight.THIN;
                break;
            default:
                tmp = FontWeight.NORMAL;
                break;
        }
        return tmp;
    }

    public synchronized void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public synchronized boolean isShowReferenceOnly() {
        return showReferenceOnly;
    }

    public synchronized void setShowReferenceOnly(boolean showReferenceOnly) {
        this.showReferenceOnly = showReferenceOnly;
    }

    public synchronized boolean isReferenceItalic() {
        return referenceItalic;
    }

    public synchronized void setReferenceItalic(boolean referenceItalic) {
        this.referenceItalic = referenceItalic;
    }

    public synchronized void save() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("settings.ini");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            bw.write(bibles.size() + System.lineSeparator());
            for (Bible bible : bibles) {
                bw.write(bible.getName() + System.lineSeparator());
                bw.write(bible.getPath() + " " + bible.getUsage() + System.lineSeparator());
            }
            bw.write("maxFont" + System.lineSeparator());
            bw.write(maxFont + System.lineSeparator());
            bw.write("withAccents" + System.lineSeparator());
            bw.write(withAccents + System.lineSeparator());
            bw.write("backgroundColor" + System.lineSeparator());
            bw.write(backgroundColor.getRed() + System.lineSeparator());
            bw.write(backgroundColor.getGreen() + System.lineSeparator());
            bw.write(backgroundColor.getBlue() + System.lineSeparator());
            bw.write(backgroundColor.getOpacity() + System.lineSeparator());
            bw.write("color" + System.lineSeparator());
            bw.write(color.getRed() + System.lineSeparator());
            bw.write(color.getGreen() + System.lineSeparator());
            bw.write(color.getBlue() + System.lineSeparator());
            bw.write(color.getOpacity() + System.lineSeparator());
            bw.write("isImage" + System.lineSeparator());
            bw.write(isBackgroundImage + System.lineSeparator());
            bw.write("imagePath" + System.lineSeparator());
            bw.write(backgroundImagePath + System.lineSeparator());
            bw.write("isFastMode" + System.lineSeparator());
            bw.write(isFastMode + System.lineSeparator());
            bw.write("isParallel" + System.lineSeparator());
            bw.write(isParallel + System.lineSeparator());
            bw.write("parallelBiblePath" + System.lineSeparator());
            bw.write(parallelBiblePath + System.lineSeparator());
            bw.write("font" + System.lineSeparator());
            bw.write(font + System.lineSeparator());
            bw.write("lineSpace" + System.lineSeparator());
            bw.write(lineSpace + System.lineSeparator());
            bw.write("fontWeight" + System.lineSeparator());
            bw.write(fontWeight + System.lineSeparator());
            bw.write("showReferenceOnly" + System.lineSeparator());
            bw.write(showReferenceOnly + System.lineSeparator());
            bw.write("referenceItalic" + System.lineSeparator());
            bw.write(referenceItalic + System.lineSeparator());
            bw.write("logging" + System.lineSeparator());
            bw.write(logging + System.lineSeparator());
            bw.write("parallelBibleIndex" + System.lineSeparator());
            bw.write(parallelBibleIndex + System.lineSeparator());
            bw.write("previewX" + System.lineSeparator());
            bw.write(previewX + System.lineSeparator());
            bw.write("previewY" + System.lineSeparator());
            bw.write(previewY + System.lineSeparator());
            bw.write("previewWidth" + System.lineSeparator());
            bw.write(previewWidth + System.lineSeparator());
            bw.write("previewHeight" + System.lineSeparator());
            bw.write(previewHeight + System.lineSeparator());
            bw.write("previewLoadOnStart" + System.lineSeparator());
            bw.write(previewLoadOnStart + System.lineSeparator());
            bw.write("songTabHorizontalSplitPaneDividerPosition" + System.lineSeparator());
            bw.write(songTabHorizontalSplitPaneDividerPosition + System.lineSeparator());
            bw.write("songTabVerticalSplitPaneDividerPosition" + System.lineSeparator());
            bw.write(songTabVerticalSplitPaneDividerPosition + System.lineSeparator());
            bw.write("bibleTabHorizontalSplitPaneDividerPosition" + System.lineSeparator());
            bw.write(bibleTabHorizontalSplitPaneDividerPosition + System.lineSeparator());
            bw.write("bibleTabVerticalSplitPaneDividerPosition" + System.lineSeparator());
            bw.write(bibleTabVerticalSplitPaneDividerPosition + System.lineSeparator());
            bw.write("mainHeight" + System.lineSeparator());
            bw.write(mainHeight + System.lineSeparator());
            bw.write("mainWidth" + System.lineSeparator());
            bw.write(mainWidth + System.lineSeparator());
            bw.write("referenceChapterSorting" + System.lineSeparator());
            bw.write(referenceChapterSorting + System.lineSeparator());
            bw.write("referenceVerseSorting" + System.lineSeparator());
            bw.write(referenceVerseSorting + System.lineSeparator());
            bw.write("preferredLanguage" + System.lineSeparator());
            bw.write(preferredLanguage.getLanguage() + System.lineSeparator());
            bw.write("songHeightSliderValue" + System.lineSeparator());
            bw.write(songHeightSliderValue + System.lineSeparator());
            bw.write("verseListViewFontSize" + System.lineSeparator());
            bw.write(verseListViewFontSize + System.lineSeparator());
            bw.write("progressLineColor" + System.lineSeparator());
            bw.write(progressLineColor.getRed() + System.lineSeparator());
            bw.write(progressLineColor.getGreen() + System.lineSeparator());
            bw.write(progressLineColor.getBlue() + System.lineSeparator());
            bw.write(progressLineColor.getOpacity() + System.lineSeparator());
            bw.write("showProgressLine" + System.lineSeparator());
            bw.write(showProgressLine.get() + System.lineSeparator());
            bw.write("progressLinePositionIsTop" + System.lineSeparator());
            bw.write(progressLinePositionIsTop.get() + System.lineSeparator());
            bw.write("parallelBibleColor" + System.lineSeparator());
            bw.write(parallelBibleColor.toString() + System.lineSeparator());
            bw.write("songOrderMethod" + System.lineSeparator());
            bw.write(songOrderMethod.name() + System.lineSeparator());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void increaseCurrentBibleUsage() {
        Bible currentBible = bibles.get(this.currentBible);
        currentBible.setUsage(currentBible.getUsage() + 1);
    }

    private synchronized void load() {
        BufferedReader br = null;
        try {
            FileInputStream fileInputStream = new FileInputStream("settings.ini");
            br = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            Integer bibleNumber = Integer.parseInt(br.readLine());
            bibles = new ArrayList<>(bibleNumber);
            for (int i = 0; i < bibleNumber; ++i) {
                Bible bible = new Bible();
                bible.setName(br.readLine());
                String[] line = br.readLine().split(" ");
                bible.setPath(line[0]);
                try {
                    bible.setUsage(Integer.parseInt(line[1]));
                } catch (Exception ignored) {
                }
                bibles.add(bible);
            }
            br.readLine();
            maxFont = Integer.parseInt(br.readLine());
            br.readLine();
            String strline = br.readLine();
            System.out.println(strline + strline);
            withAccents = parseBoolean(strline);
            br.readLine();
            backgroundColor = new Color(parseDouble(br.readLine()), parseDouble(br.readLine()),
                    parseDouble(br.readLine()), parseDouble(br.readLine()));
            br.readLine();
            color = new Color(parseDouble(br.readLine()), parseDouble(br.readLine()),
                    parseDouble(br.readLine()), parseDouble(br.readLine()));
            br.readLine();
            isBackgroundImage = parseBoolean(br.readLine());
            br.readLine();
            backgroundImagePath = br.readLine();
            if (isBackgroundImage) {
                BackgroundImage myBI = new BackgroundImage(new Image(backgroundImagePath, 1024, 768, false, true),
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                        BackgroundSize.DEFAULT);
                setBackgroundImage(myBI);
            }
            br.readLine();
            isFastMode = parseBoolean(br.readLine());
            br.readLine();
            isParallel = parseBoolean(br.readLine());
            br.readLine();
            parallelBiblePath = br.readLine();
            br.readLine();
            font = br.readLine();
            br.readLine();
            lineSpace = parseDouble(br.readLine());
            br.readLine();
            fontWeight = br.readLine();
            br.readLine();
            showReferenceOnly = parseBoolean(br.readLine());
            br.readLine();
            referenceItalic = parseBoolean(br.readLine());
            if (br.ready()) {
                br.readLine();
                logging = parseBoolean(br.readLine());
            }
            br.readLine();
            br.readLine();
            bibles.sort((l, r) -> Integer.compare(r.getUsage(), l.getUsage()));
            for (int i = 0; i < bibleNumber; ++i) {
                if (bibles.get(i).getPath().equals(parallelBiblePath)) {
                    parallelBibleIndex = i;
                    break;
                }
            }
            br.readLine();
            previewX = parseDouble(br.readLine());
            br.readLine();
            previewY = parseDouble(br.readLine());
            br.readLine();
            previewWidth = parseDouble(br.readLine());
            br.readLine();
            previewHeight = parseDouble(br.readLine());
            br.readLine();
            previewLoadOnStart = parseBoolean(br.readLine());
            br.readLine();
            songTabHorizontalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            songTabVerticalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            bibleTabHorizontalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            bibleTabVerticalSplitPaneDividerPosition = parseDouble(br.readLine());
            br.readLine();
            mainHeight = parseDouble(br.readLine());
            br.readLine();
            mainWidth = parseDouble(br.readLine());
            br.readLine();
            referenceChapterSorting = parseBoolean(br.readLine());
            br.readLine();
            referenceVerseSorting = parseBoolean(br.readLine());
            br.readLine();
            setPreferredLanguage(br.readLine());
            br.readLine();
            songHeightSliderValue = parseDouble(br.readLine());
            br.readLine();
            verseListViewFontSize = parseDouble(br.readLine());
            br.readLine();
            progressLineColor = new Color(parseDouble(br.readLine()), parseDouble(br.readLine()),
                    parseDouble(br.readLine()), parseDouble(br.readLine()));
            br.readLine();
            showProgressLine.set(parseBoolean(br.readLine()));
            br.readLine();
            progressLinePositionIsTop.set(parseBoolean(br.readLine()));
            br.readLine();
            parallelBibleColor = Color.web(br.readLine());
            br.readLine();
            songOrderMethod = OrderMethod.valueOf(br.readLine());
            br.close();
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public synchronized String getFontWeightString() {
        return fontWeight;
    }

    public synchronized void setLogging(boolean logging) {
        this.logging = logging;
    }

    public synchronized int getParallelBibleIndex() {
        return parallelBibleIndex;
    }

    public synchronized void setParallelBibleIndex(int parallelBibleIndex) {
        this.parallelBibleIndex = parallelBibleIndex;
        parallelBiblePath = bibles.get(parallelBibleIndex).getPath();
    }

    public synchronized double getPreviewX() {
        return previewX;
    }

    public synchronized void setPreviewX(double previewX) {
        this.previewX = previewX;
    }

    public synchronized double getPreviewY() {
        return previewY;
    }

    public synchronized void setPreviewY(double previewY) {
        this.previewY = previewY;
    }

    public synchronized double getPreviewWidth() {
        return previewWidth;
    }

    public synchronized void setPreviewWidth(double previewWidth) {
        this.previewWidth = previewWidth;
    }

    public synchronized double getPreviewHeight() {
        return previewHeight;
    }

    public synchronized void setPreviewHeight(double previewHeight) {
        this.previewHeight = previewHeight;
    }

    public synchronized boolean isPreviewLoadOnStart() {
        return previewLoadOnStart;
    }

    public synchronized void setPreviewLoadOnStart(boolean previewLoadOnStart) {
        this.previewLoadOnStart = previewLoadOnStart;
    }

    public synchronized double getSongTabHorizontalSplitPaneDividerPosition() {
        return songTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized void setSongTabHorizontalSplitPaneDividerPosition(double songTabHorizontalSplitPaneDividerPosition) {
        this.songTabHorizontalSplitPaneDividerPosition = songTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized double getSongTabVerticalSplitPaneDividerPosition() {
        return songTabVerticalSplitPaneDividerPosition;
    }

    public synchronized void setSongTabVerticalSplitPaneDividerPosition(double songTabVerticalSplitPaneDividerPosition) {
        this.songTabVerticalSplitPaneDividerPosition = songTabVerticalSplitPaneDividerPosition;
    }

    public synchronized double getBibleTabHorizontalSplitPaneDividerPosition() {
        return bibleTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized void setBibleTabHorizontalSplitPaneDividerPosition(double bibleTabHorizontalSplitPaneDividerPosition) {
        this.bibleTabHorizontalSplitPaneDividerPosition = bibleTabHorizontalSplitPaneDividerPosition;
    }

    public synchronized double getBibleTabVerticalSplitPaneDividerPosition() {
        return bibleTabVerticalSplitPaneDividerPosition;
    }

    public synchronized void setBibleTabVerticalSplitPaneDividerPosition(double bibleTabVerticalSplitPaneDividerPosition) {
        this.bibleTabVerticalSplitPaneDividerPosition = bibleTabVerticalSplitPaneDividerPosition;
    }

    public synchronized double getMainHeight() {
        return mainHeight;
    }

    public synchronized void setMainHeight(double mainHeight) {
        this.mainHeight = mainHeight;
    }

    public synchronized double getMainWidth() {
        return mainWidth;
    }

    public synchronized void setMainWidth(double mainWidth) {
        this.mainWidth = mainWidth;
    }

    public synchronized boolean isReferenceChapterSorting() {
        return referenceChapterSorting;
    }

    public synchronized void setReferenceChapterSorting(boolean referenceChapterSorting) {
        this.referenceChapterSorting = referenceChapterSorting;
    }

    public synchronized boolean isReferenceVerseSorting() {
        return referenceVerseSorting;
    }

    public synchronized void setReferenceVerseSorting(boolean referenceVerseSorting) {
        this.referenceVerseSorting = referenceVerseSorting;
    }

    public synchronized Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    public synchronized void setPreferredLanguage(String language) {
        switch (language) {
            case "hu":
                preferredLanguage = new Locale(language, "HU");
                break;
            default:
                preferredLanguage = new Locale("en", "US");
        }
    }

    public synchronized ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("bundles.language", getPreferredLanguage());
        }
        return resourceBundle;
    }

    public synchronized double getSongHeightSliderValue() {
        return songHeightSliderValue;
    }

    public synchronized void setSongHeightSliderValue(double songHeightSliderValue) {
        this.songHeightSliderValue = songHeightSliderValue;
    }

    public synchronized double getVerseListViewFontSize() {
        return verseListViewFontSize;
    }

    public synchronized void setVerseListViewFontSize(double verseListViewFontSize) {
        this.verseListViewFontSize = verseListViewFontSize;
    }

    public synchronized boolean isShowProgressLine() {
        return showProgressLine.get();
    }

    public synchronized void setShowProgressLine(boolean showProgressLine) {
        this.showProgressLine.set(showProgressLine);
    }

    public synchronized SimpleBooleanProperty showProgressLineProperty() {
        return showProgressLine;
    }

    public synchronized Color getProgressLineColor() {
        return progressLineColor;
    }

    public synchronized void setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
    }

    public synchronized boolean isProgressLinePositionIsTop() {
        return progressLinePositionIsTop.get();
    }

    public synchronized void setProgressLinePositionIsTop(boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop.set(progressLinePositionIsTop);
    }

    public synchronized SimpleBooleanProperty progressLinePositionIsTopProperty() {
        return progressLinePositionIsTop;
    }

    public synchronized boolean isShareOnNetwork() {
        return shareOnNetwork;
    }

    public synchronized void setShareOnNetwork(boolean shareOnNetwork) {
        this.shareOnNetwork = shareOnNetwork;
    }

    public synchronized boolean isConnectedToShared() {
        return connectedToShared.get();
    }

    public synchronized void setConnectedToShared(boolean connectedToShared) {
        this.connectedToShared.set(connectedToShared);
    }

    public synchronized BooleanProperty connectedToSharedProperty() {
        return connectedToShared;
    }

    public synchronized Color getParallelBibleColor() {
        return parallelBibleColor;
    }

    public synchronized void setParallelBibleColor(Color parallelBibleColor) {
        this.parallelBibleColor = parallelBibleColor;
    }

    public synchronized OrderMethod getSongOrderMethod() {
        return songOrderMethod;
    }

    public synchronized void setSongOrderMethod(OrderMethod songOrderMethod) {
        this.songOrderMethod = songOrderMethod;
    }
}