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
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProjectionScreenSettings {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectionScreenSettings.class);
    private final Settings settings;

    private ProjectionScreenHolder projectionScreenHolder;
    @Expose
    private Integer maxFont;
    @Expose
    private Color backgroundColor;
    @Expose
    private Color color;
    private BackgroundImage backgroundImage;
    @Expose
    private String backgroundImagePath;
    @Expose
    private Boolean isBackgroundImage;
    @Expose
    private String font;
    @Expose
    private Double lineSpace;
    @Expose
    private String fontWeight;
    @Expose
    private Color progressLineColor;
    @Expose
    private Boolean breakLines;
    @Expose
    private Integer breakAfter;
    @Expose
    private Integer progressLineThickness;
    @Expose
    private Boolean showSongSecondText;
    @Expose
    private Color songSecondTextColor;
    @Expose
    private Boolean progressLinePositionIsTop;
    private boolean useGlobalSettings = true;

    public ProjectionScreenSettings() {
        settings = Settings.getInstance();
    }

    public ProjectionScreenSettings(ProjectionScreenHolder projectionScreenHolder) {
        this();
        this.projectionScreenHolder = projectionScreenHolder;
        load();
    }

    public ProjectionScreenSettings(ProjectionScreenSettings projectionScreenSettings) {
        this.settings = projectionScreenSettings.settings;
        this.maxFont = projectionScreenSettings.maxFont;
        this.backgroundColor = projectionScreenSettings.backgroundColor;
        this.color = projectionScreenSettings.color;
        this.backgroundImage = projectionScreenSettings.backgroundImage;
        this.backgroundImagePath = projectionScreenSettings.backgroundImagePath;
        this.isBackgroundImage = projectionScreenSettings.isBackgroundImage;
        this.font = projectionScreenSettings.font;
        this.lineSpace = projectionScreenSettings.lineSpace;
        this.fontWeight = projectionScreenSettings.fontWeight;
        this.progressLineColor = projectionScreenSettings.progressLineColor;
        this.breakLines = projectionScreenSettings.breakLines;
        this.breakAfter = projectionScreenSettings.breakAfter;
        this.progressLineThickness = projectionScreenSettings.progressLineThickness;
        this.showSongSecondText = projectionScreenSettings.showSongSecondText;
        this.songSecondTextColor = projectionScreenSettings.songSecondTextColor;
        this.progressLinePositionIsTop = projectionScreenSettings.progressLinePositionIsTop;
        this.projectionScreenHolder = projectionScreenSettings.projectionScreenHolder;
        this.useGlobalSettings = projectionScreenSettings.useGlobalSettings;
    }

    private static boolean isaBoolean(Boolean aBoolean) {
        return aBoolean != null && aBoolean;
    }

    public ProjectionScreenHolder getProjectionScreenHolder() {
        return projectionScreenHolder;
    }

    public Integer getMaxFont() {
        if (maxFont == null && useGlobalSettings) {
            return settings.getMaxFont();
        }
        return maxFont;
    }

    public void setMaxFont(Integer maxFont) {
        this.maxFont = maxFont;
    }

    public Color getBackgroundColor() {
        if (backgroundColor == null && useGlobalSettings) {
            return settings.getBackgroundColor();
        }
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getColor() {
        if (color == null && useGlobalSettings) {
            return settings.getColor();
        }
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @SuppressWarnings("unused")
    public BackgroundImage getBackgroundImage() {
        if (backgroundImage == null && useGlobalSettings) {
            return settings.getBackgroundImage();
        }
        return backgroundImage;
    }

    public boolean isBackgroundImage() {
        return isaBoolean(getIsBackgroundImage());
    }

    public Boolean getIsBackgroundImage() {
        if (isBackgroundImage == null && useGlobalSettings) {
            return settings.isBackgroundImage();
        }
        return isBackgroundImage;
    }

    public void setIsBackgroundImage(Boolean isBackgroundImage) {
        this.isBackgroundImage = isBackgroundImage;
    }

    public String getBackgroundImagePath() {
        if (backgroundImagePath == null && useGlobalSettings) {
            return settings.getBackgroundImagePath();
        }
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
    }

    public String getFont() {
        if (font == null && useGlobalSettings) {
            return settings.getFont();
        }
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Double getLineSpace() {
        if (lineSpace == null && useGlobalSettings) {
            return settings.getLineSpace();
        }
        return lineSpace;
    }

    public void setLineSpace(Double lineSpace) {
        this.lineSpace = lineSpace;
    }

    public FontWeight getFontWeight() {
        return SettingsController.getFontWeightByString(getFontWeightString());
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
            this.progressLinePositionIsTop = fromJson.progressLinePositionIsTop;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getFileName() {
        String screensDirectory = "screens";
        try {
            Files.createDirectories(Paths.get(screensDirectory));
        } catch (IOException ignored) {
        }
        return screensDirectory + "/" + projectionScreenHolder.getName() + ".json";
    }

    public String getFontWeightString() {
        if (fontWeight == null && useGlobalSettings) {
            return settings.getFontWeightString();
        }
        return fontWeight;
    }

    public Color getProgressLineColor() {
        if (progressLineColor == null && useGlobalSettings) {
            return settings.getProgressLineColor();
        }
        return progressLineColor;
    }

    public void setProgressLineColor(Color progressLineColor) {
        this.progressLineColor = progressLineColor;
    }

    public boolean isBreakLines() {
        return isaBoolean(getBreakLines());
    }

    public Boolean getBreakLines() {
        if (breakLines == null && useGlobalSettings) {
            return settings.isBreakLines();
        }
        return breakLines;
    }

    public void setBreakLines(Boolean breakLines) {
        this.breakLines = breakLines;
    }

    public Integer getBreakAfter() {
        if (breakAfter == null && useGlobalSettings) {
            return settings.getBreakAfter();
        }
        return breakAfter;
    }

    public void setBreakAfter(Integer breakAfter) {
        this.breakAfter = breakAfter;
    }

    public Integer getProgressLineThickness() {
        if (progressLineThickness == null && useGlobalSettings) {
            return settings.getProgressLineThickness();
        }
        return progressLineThickness;
    }

    public void setProgressLineThickness(Integer progressLineThickness) {
        this.progressLineThickness = progressLineThickness;
    }

    public boolean isShowSongSecondText() {
        return isaBoolean(getShowSongSecondText());
    }

    public Boolean getShowSongSecondText() {
        if (showSongSecondText == null && useGlobalSettings) {
            return settings.isShowSongSecondText();
        }
        return showSongSecondText;
    }

    public void setShowSongSecondText(Boolean showSongSecondText) {
        this.showSongSecondText = showSongSecondText;
    }

    public Color getSongSecondTextColor() {
        if (songSecondTextColor == null && useGlobalSettings) {
            return settings.getSongSecondTextColor();
        }
        return songSecondTextColor;
    }

    public void setSongSecondTextColor(Color songSecondTextColor) {
        this.songSecondTextColor = songSecondTextColor;
    }

    public boolean isProgressLinePositionIsTop() {
        return isaBoolean(getProgressLinePosition());
    }

    public void setProgressLinePositionIsTop(Boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop = progressLinePositionIsTop;
    }

    public Boolean getProgressLinePosition() {
        if (progressLinePositionIsTop == null && useGlobalSettings) {
            return settings.isProgressLinePositionIsTop();
        }
        return progressLinePositionIsTop;
    }

    public void setUseGlobalSettings(boolean useGlobalSettings) {
        this.useGlobalSettings = useGlobalSettings;
    }
}
