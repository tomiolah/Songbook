package projector.application;

import com.bence.projector.common.serializer.ColorDeserializer;
import com.bence.projector.common.serializer.ColorSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
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
    @Expose
    private Boolean strokeFont;
    private boolean useGlobalSettings = true;
    private Color strokeColor;
    private Double strokeSize;
    private StrokeType strokeType;
    private Listener onChangedListener = null;

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
        this.strokeFont = projectionScreenSettings.strokeFont;
        this.strokeColor = projectionScreenSettings.strokeColor;
        this.strokeSize = projectionScreenSettings.strokeSize;
        this.strokeType = projectionScreenSettings.strokeType;
        this.onChangedListener = projectionScreenSettings.onChangedListener;
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
        onChanged();
    }

    private void onChanged() {
        if (onChangedListener != null) {
            onChangedListener.onChanged();
        }
    }

    public Color getBackgroundColor() {
        if (backgroundColor == null && useGlobalSettings) {
            return settings.getBackgroundColor();
        }
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        onChanged();
    }

    public Color getColor() {
        if (color == null && useGlobalSettings) {
            return settings.getColor();
        }
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        onChanged();
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
        onChanged();
    }

    public String getBackgroundImagePath() {
        if (backgroundImagePath == null && useGlobalSettings) {
            return settings.getBackgroundImagePath();
        }
        return backgroundImagePath;
    }

    public void setBackgroundImagePath(String backgroundImagePath) {
        this.backgroundImagePath = backgroundImagePath;
        onChanged();
    }

    public String getFont() {
        if (font == null && useGlobalSettings) {
            return settings.getFont();
        }
        return font;
    }

    public void setFont(String font) {
        this.font = font;
        onChanged();
    }

    public Double getLineSpace() {
        if (lineSpace == null && useGlobalSettings) {
            return settings.getLineSpace();
        }
        return lineSpace;
    }

    public void setLineSpace(Double lineSpace) {
        this.lineSpace = lineSpace;
        onChanged();
    }

    public FontWeight getFontWeight() {
        return SettingsController.getFontWeightByString(getFontWeightString());
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
        onChanged();
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
            this.strokeFont = fromJson.strokeFont;
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
        onChanged();
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
        onChanged();
    }

    public Integer getBreakAfter() {
        if (breakAfter == null && useGlobalSettings) {
            return settings.getBreakAfter();
        }
        return breakAfter;
    }

    public void setBreakAfter(Integer breakAfter) {
        this.breakAfter = breakAfter;
        onChanged();
    }

    public Integer getProgressLineThickness() {
        if (progressLineThickness == null && useGlobalSettings) {
            return settings.getProgressLineThickness();
        }
        return progressLineThickness;
    }

    public void setProgressLineThickness(Integer progressLineThickness) {
        this.progressLineThickness = progressLineThickness;
        onChanged();
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
        onChanged();
    }

    public Color getSongSecondTextColor() {
        if (songSecondTextColor == null && useGlobalSettings) {
            return settings.getSongSecondTextColor();
        }
        return songSecondTextColor;
    }

    public void setSongSecondTextColor(Color songSecondTextColor) {
        this.songSecondTextColor = songSecondTextColor;
        onChanged();
    }

    public boolean isProgressLinePositionIsTop() {
        return isaBoolean(getProgressLinePosition());
    }

    public void setProgressLinePositionIsTop(Boolean progressLinePositionIsTop) {
        this.progressLinePositionIsTop = progressLinePositionIsTop;
        onChanged();
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

    public Boolean getStrokeFont() {
        return strokeFont;
    }

    public void setStrokeFont(Boolean strokeFont) {
        this.strokeFont = strokeFont;
        onChanged();
    }

    public boolean isStrokeFont() {
        if (strokeFont == null && useGlobalSettings) {
            return settings.isStrokeFont();
        }
        return strokeFont != null && strokeFont;
    }

    public Color getStrokeColor() {
        if (strokeColor == null && useGlobalSettings) {
            return settings.getStrokeColor();
        }
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
        onChanged();
    }

    public Double getStrokeSize() {
        if (strokeSize == null && useGlobalSettings) {
            return settings.getStrokeSize();
        }
        return strokeSize;
    }

    public double getStrokeSizeD() {
        Double size = getStrokeSize();
        if (size == null) {
            return 0;
        }
        return size;
    }

    public void setStrokeSize(Double strokeSize) {
        this.strokeSize = strokeSize;
        onChanged();
    }

    public StrokeType getStrokeType() {
        if (strokeType == null && useGlobalSettings) {
            return settings.getStrokeType();
        }
        return strokeType;
    }

    public void setStrokeType(StrokeType strokeType) {
        this.strokeType = strokeType;
        onChanged();
    }

    public void setOnChangedListener(Listener listener) {
        this.onChangedListener = listener;
    }

    public interface Listener {
        void onChanged();
    }
}
