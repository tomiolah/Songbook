package projector.controller;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.controller.util.ImageCacheService;
import projector.controller.util.ImageContainer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bence.projector.common.util.DebugUtil.gatherTime;
import static com.bence.projector.common.util.DebugUtil.summaryGatheredTime;

public class GalleryController {

    private static final String FOLDER_PATH = "gallery";
    private static final Logger LOG = LoggerFactory.getLogger(GalleryController.class);
    public BorderPane borderPane;
    private ImageContainer selectedImageContainer; // to keep track of the selected image container
    private FlowPane flowPane = null;
    private List<ImageContainer> containerHolders;
    private boolean pauseSelectByFocus = false;
    private Canvas previewCanvas;

    public static void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawImageOnCanvas(Image image, Canvas canvas) {
        if (image == null) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = image.getWidth();
        double height = image.getHeight();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double scaleFactor = Math.min(canvasWidth / width, canvasHeight / height);
        double scaledWidth = width * scaleFactor;
        double scaledHeight = height * scaleFactor;

        // Calculate the position to center the image on the canvas
        double x = (canvasWidth - scaledWidth) / 2;
        double y = (canvasHeight - scaledHeight) / 2;

        // Draw the scaled image on the canvas
        gc.drawImage(image, x, y, scaledWidth, scaledHeight);
    }

    public static void main(String[] args) {
        // GalleryController galleryController = new GalleryController();
        // for (int i = 1; i <= 464 * 2; ++i) {
        //     // System.out.print("i = " + i + "\t");
        //     int bufferSize = 8192 / 2 * i;
        //     System.out.print(bufferSize + ",");
        //     extracted(galleryController, bufferSize);
        // }
        gatherTime();
        gatherTime();
        summaryGatheredTime();
        // System.out.println(image.getWidth() + "x" + image.getHeight());
        System.out.println("Works");
    }

    private static void loadImagePathToCanvas(String imagePath, Canvas canvas) {
        Image image = ImageCacheService.getInstance().getImage(imagePath, (int) canvas.getWidth(), (int) canvas.getHeight());
        drawImageOnCanvas(image, canvas);
    }

    private void loadImagePathToPreviewCanvas(String imagePath) {
        clearCanvas(previewCanvas);
        loadImagePathToCanvas(imagePath, previewCanvas);
    }

    public void onTabOpened() {
        // if (flowPane != null) {
        //     return;
        // }
        borderPane.setCenter(createGalleryPane()); // TODO: this should be optimized
        borderPane.setRight(createPreviewCanvas());
    }

    private Canvas createPreviewCanvas() {
        previewCanvas = new Canvas(400, 400);
        return previewCanvas;
    }

    private StackPane createGalleryPane() {
        StackPane galleryPane = new StackPane();
        galleryPane.setPadding(new Insets(10));
        galleryPane.setFocusTraversable(true);

        flowPane = new FlowPane(10, 10);
        flowPane.setPadding(new Insets(0));
        flowPane.setPrefWrapLength(780);
        flowPane.setOnKeyPressed(this::handleKeyPress);
        flowPane.setFocusTraversable(true);
        flowPane.setHgap(10);
        containerHolders = new ArrayList<>();
        ObservableList<Node> flowPaneChildren = flowPane.getChildren();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<String> imagePaths = getImagePathsFromFolder(FOLDER_PATH);
        for (String imagePath : imagePaths) {

            Canvas canvas = new Canvas(200, 200);
            // Image image = new Image(fileImagePath);
            executorService.submit(() -> {
                try {
                    loadImagePathToCanvas(imagePath, canvas);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            // ImageView imageView = new ImageView(image);
            // imageView.setPreserveRatio(true);
            // imageView.setFitWidth(200);
            // imageView.setFitHeight(200); // Set the desired height

            Label filenameLabel = new Label(getFileNameFromPath(imagePath));
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(canvas);
            // borderPane.setCenter(imageView);
            borderPane.setBottom(filenameLabel);
            BorderPane.setAlignment(filenameLabel, javafx.geometry.Pos.CENTER);
            BorderPane.setMargin(canvas, new Insets(5));
            // BorderPane.setMargin(imageView, new Insets(5));

            ImageContainer imageContainer = new ImageContainer(filenameLabel, borderPane);
            imageContainer.setFileImagePath(imagePath);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(imageContainer.getHighlightRect(), imageContainer.getContainer());
            stackPane.setOnMouseClicked(event -> {
                try {
                    pauseSelectByFocus = true;
                    stackPane.requestFocus();
                    selectImageContainer(imageContainer);
                } finally {
                    pauseSelectByFocus = false;
                }
            });
            stackPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (pauseSelectByFocus) {
                    return;
                }
                if (newValue != null && newValue && selectedImageContainer != imageContainer) {
                    selectImageContainer(imageContainer);
                }
            });
            stackPane.setFocusTraversable(true);
            containerHolders.add(imageContainer);
            imageContainer.setMainPane(stackPane);
            flowPaneChildren.add(stackPane);
        }
        executorService.shutdown();

        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFocusTraversable(false);
        scrollPane.setOnKeyPressed(this::handleKeyPress);
        galleryPane.getChildren().addAll(scrollPane);
        return galleryPane;
    }

    @SuppressWarnings("unused")
    private Image convertToJavaFXImage(BufferedImage image) {
        WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
        gatherTime();
        javafx.scene.image.PixelWriter pixelWriter = writableImage.getPixelWriter();
        gatherTime();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setArgb(x, y, image.getRGB(x, y));
            }
        }
        gatherTime();
        return writableImage;
    }

    private void selectImageContainer(ImageContainer imageContainer) {
        try {
            if (selectedImageContainer != null) {
                selectedImageContainer.getHighlightRect().setVisible(false);
            }
            if (selectedImageContainer != imageContainer) {
                imageContainer.getHighlightRect().setVisible(true);
                selectedImageContainer = imageContainer;
                String nextFileImagePath = getNextFileImagePath();
                String fileImagePath = imageContainer.getFileImagePath();
                MyController.getInstance().getProjectionScreenController().setImage(fileImagePath, ProjectionType.IMAGE, nextFileImagePath);
                loadImagePathToPreviewCanvas(fileImagePath);
            } else {
                selectedImageContainer = null;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getNextFileImagePath() {
        int selectedIndex = getSelectedImageIndex();
        ImageContainer nextImageContainer = getImageContainer(selectedIndex + 1);
        String nextFileImagePath;
        if (nextImageContainer != null) {
            nextFileImagePath = nextImageContainer.getFileImagePath();
        } else {
            nextFileImagePath = null;
        }
        return nextFileImagePath;
    }

    private List<String> getImagePathsFromFolder(@SuppressWarnings("SameParameterValue") String folderPath) {
        List<String> imagePaths = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isImageFile(file.getName())) {
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return imagePaths;
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileNameFromPath(String path) {
        File file = new File(path);
        return file.getName();
    }

    public void handleKeyPress(KeyEvent event) {
        if (selectedImageContainer != null) {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.LEFT) {
                setPrevious();
                event.consume();
            } else if (keyCode == KeyCode.RIGHT) {
                setNext();
                event.consume();
            }
        }
    }

    private int getSelectedImageIndex() {
        return getSelectedImageIndex(selectedImageContainer);
    }

    private int getSelectedImageIndex(ImageContainer imageContainer) {
        if (imageContainer != null) {
            StackPane mainPane = imageContainer.getMainPane();
            return flowPane.getChildren().indexOf(mainPane);
        }
        return -1;
    }

    private int getNumImages() {
        return flowPane.getChildren().size();
    }

    private void setSelectedImage(int index) {
        try {
            pauseSelectByFocus = true;
            StackPane stackPane = (StackPane) this.flowPane.getChildren().get(index);
            stackPane.requestFocus();
            if (containerHolders == null || containerHolders.size() <= index) {
                return;
            }
            ImageContainer imageContainer = getImageContainer(index);
            if (imageContainer != null) {
                selectImageContainer(imageContainer);
            }
        } finally {
            pauseSelectByFocus = false;
        }
    }

    private ImageContainer getImageContainer(int index) {
        if (index < 0 || index >= containerHolders.size()) {
            return null;
        }
        return containerHolders.get(index);
    }

    public void setNext() {
        int selectedIndex = getSelectedImageIndex();
        if (selectedIndex < getNumImages() - 1) {
            setSelectedImage(selectedIndex + 1);
        }
    }

    public void setPrevious() {
        int selectedIndex = getSelectedImageIndex();
        if (selectedIndex > 0) {
            setSelectedImage(selectedIndex - 1);
        }
    }

}
