package projector.application;

import com.bence.projector.common.serializer.ColorDeserializer;
import com.bence.projector.common.serializer.ColorSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.SettingsController;
import projector.controller.util.ProjectionScreenHolder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ProjectionScreenSettings {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectionScreenSettings.class);
    private final Settings settings;

    private ProjectionScreenHolder projectionScreenHolder;
    @Expose
    private int maxFont = 80;
    @Expose
    private Color backgroundColor = Color.BLACK;
    @Expose
    private boolean defaultBackgroundColor = true;
    @Expose
    private Color color = Color.WHITE;
    private BackgroundImage backgroundImage;
    @Expose
    private String backgroundImagePath;
    @Expose
    private boolean isBackgroundImage = false;
    @Expose
    private String font = "system";
    @Expose
    private double lineSpace = 3.131991051454138;
    @Expose
    private String fontWeight = "NORMAL";
    @Expose
    private Color progressLineColor = new Color(1.0, 1.0, 1.0, 0.7);
    @Expose
    private boolean breakLines = false;
    @Expose
    private int breakAfter = 77;
    @Expose
    private Integer progressLineThickness = 5;
    @Expose
    private boolean showSongSecondText = false;
    @Expose
    private Color songSecondTextColor = new Color(0.46, 1.0, 1.0, 1.0);
    @Expose
    private boolean progressLinePositionIsTop = true;

    public ProjectionScreenSettings() {
        settings = Settings.getInstance();
    }

    public ProjectionScreenSettings(ProjectionScreenHolder projectionScreenHolder) {
        this();
        this.projectionScreenHolder = projectionScreenHolder;
        load();
    }

    public ProjectionScreenHolder getProjectionScreenHolder() {
        return projectionScreenHolder;
    }

    public int getMaxFont() {
        return maxFont;
    }

    public void setMaxFont(int maxFont) {
        this.maxFont = maxFont;
    }

    public boolean isDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public void setDefaultBackgroundColor(boolean defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
    }

    public Color getBackgroundColor() {
        if (defaultBackgroundColor) {
            return settings.getBackgroundColor();
        }
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        defaultBackgroundColor = false;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BackgroundImage getBackgroundImage() {
        return backgroundImage;
    }

    public boolean isBackgroundImage() {
        return isBackgroundImage;
    }

    public void setBackgroundImage(BackgroundImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public void setBackgroundImage(boolean isBackgroundImage) {
        this.isBackgroundImage = isBackgroundImage;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public double getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(double lineSpace) {
        this.lineSpace = lineSpace;
    }

    public FontWeight getFontWeight() {
        return SettingsController.getFontWeightByString(fontWeight);
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public void save() {
        FileOutputStream ofStream;
        try {
            ofStream = new FileOutputStream(getFileName());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Color.class, new ColorSerializer())
                    .create();
            String json = gson.toJson(this);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void load() {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(getFileName());
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            br.close();
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Color.class, new ColorDeserializer())
                    .create();
            ProjectionScreenSettings fromJson = gson.fromJson(s.toString(), ProjectionScreenSettings.class);
            if (fromJson == null) {
                return;
            }
            this.maxFont = fromJson.maxFont;
            this.backgroundColor = fromJson.backgroundColor;
            this.defaultBackgroundColor = fromJson.defaultBackgroundColor;
            this.color = fromJson.color;
            this.backgroundImage = fromJson.backgroundImage;
            this.backgroundImagePath = fromJson.backgroundImagePath;
            this.isBackgroundImage = fromJson.isBackgroundImage;
            this.font = fromJson.font;
            this.lineSpace = fromJson.lineSpace;
            this.fontWeight = fromJson.fontWeight;
            this.progressLineColor = fromJson.progressLineColor;
            this.breakLines = fromJson.breakLines;
            this.breakAfter = fromJson.breakAfter;
            this.progressLineThickness = fromJson.progressLineThickness;
            this.showSongSecondText = fromJson.showSongSecondText;
            this.songSecondTextColor = fromJson.songSecondTextColor;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getFileName() {
        return projectionScreenHolder.getName() + ".json";
    }

    public String getFontWeightString() {
        return fontWeight;
    }

    public Color getProgressLineColor() {
        return progressLineColor;
    }

    public void setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
    }

    public boolean isBreakLines() {
        return breakLines;
    }

    public void setBreakLines(boolean breakLines) {
        this.breakLines = breakLines;
    }

    public int getBreakAfter() {
        return breakAfter;
    }

    public void setBreakAfter(int breakAfter) {
        this.breakAfter = breakAfter;
    }

    public Integer getProgressLineThickness() {
        return progressLineThickness;
    }

    public void setProgressLineThickness(Integer progressLineThickness) {
        this.progressLineThickness = progressLineThickness;
    }

    public boolean isShowSongSecondText() {
        return showSongSecondText;
    }

    public void setShowSongSecondText(boolean showSongSecondText) {
        this.showSongSecondText = showSongSecondText;
    }

    public Color getSongSecondTextColor() {
        return songSecondTextColor;
    }

    public void setSongSecondTextColor(Color songSecondTextColor) {
        this.songSecondTextColor = songSecondTextColor;
    }

    public boolean isProgressLinePositionIsTop() {
        return progressLinePositionIsTop;
    }

    public void setProgressLinePositionIsTop(boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop = progressLinePositionIsTop;
    }
}
