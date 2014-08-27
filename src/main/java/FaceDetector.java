import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceDetector extends Application {

    private static final ImageView imageView = new ImageView();
    private static boolean isRunning = true;

    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox root = new VBox();
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1000, 850);

        HBox imageStrip = new HBox();
        imageStrip.setPadding(new Insets(20));
        imageStrip.setAlignment(Pos.CENTER);

        Thread cameraThread = new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();

            BufferedImage capture = null;
            List<BufferedImage> facesList = new ArrayList<>();

            while(isRunning) {
                capture = webcam.getImage();

                HaarCascadeDetector detector = new HaarCascadeDetector();
                List<DetectedFace> faces = detector.detectFaces(ImageUtilities.createFImage(capture));

                facesList.clear();
                for(DetectedFace face : faces) {
                    facesList.add(ImageUtilities.createBufferedImage(face.getFacePatch()));
                }

                System.out.println("Detected " + faces.size() + " faces");

                final BufferedImage finalCapture = capture;
                Platform.runLater(() -> {
                    imageView.setImage(convertBufferedImage(finalCapture));

                    imageStrip.getChildren().clear();
                    for(BufferedImage img : facesList) {
                        ImageView imgView = new ImageView(convertBufferedImage(img));
                        imageStrip.getChildren().add(imgView);
                    }
                });
            }
            webcam.close();
        });
        cameraThread.start();

        HBox hbox = new HBox(imageView);
        hbox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(hbox, imageStrip);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Camera Face Recognition");
        primaryStage.setOnCloseRequest(event -> isRunning = false);
        primaryStage.show();
    }

    private Image convertBufferedImage(BufferedImage bufImage) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufImage, "PNG", os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Image(new ByteArrayInputStream(os.toByteArray()));
    }

}
