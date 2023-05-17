package java_fx.audioeditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import javax.sound.sampled.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class AudioEditorController {
    MediaPlayer mediaPlayer;

    @FXML
    private Canvas CanvasMonitor;

    @FXML
    private Canvas ChartCanvas;

    @FXML
    private ListView<AudioFile> AudioListView;
    GraphicsContext gc;
    GraphicsContext ch_gc;
    Media buzzer;

    @FXML
    private AreaChart<Number, Number> lineChart;

    @FXML
    private Label timeLineLabel;

    public String parseDuration(Duration duration){
       String result = "";

       int seconds = (int)
               Math.round(duration.toSeconds());
       int minutes = seconds/60;
       seconds = seconds%60;

       return String.format("%02d",minutes)  + ":"+String.format("%02d",seconds);
    }
    @FXML
    void onPlayButtonClick(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if(mediaPlayer.getStatus()== MediaPlayer.Status.PLAYING){
           mediaPlayer.pause();
        }else{
            mediaPlayer.play();
            mediaPlayer.currentTimeProperty().addListener(
                (observable, oldValue, newValue) -> {
                    timeLineLabel.setText(
                        parseDuration(mediaPlayer.getCurrentTime())
                            + "/"+
                                parseDuration(mediaPlayer.getTotalDuration())
                    );

                    ch_gc.setFill(new Color(0,0,0,1));
                    ch_gc.clearRect(0,0,ChartCanvas.getWidth(),ChartCanvas.getHeight());
                    ch_gc.setFill(new Color(0,0,0,0.5));
                    ch_gc.fillRect(
                            (ChartCanvas.getWidth())*mediaPlayer.getCurrentTime().toSeconds()/mediaPlayer.getTotalDuration().toSeconds(),
                            7,1,ChartCanvas.getHeight());
                }
            );
        }
    }
    @FXML
    void AddTrackAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("audio", "*.wav")
        );
        File f = fc.showOpenDialog(null);
        if(f!=null){
            AudioListView.getItems().add(new AudioFile(f.getAbsolutePath()));
        }else{

        }
    }

    @FXML
    void initialize() throws IOException {
        gc = CanvasMonitor.getGraphicsContext2D();
        ch_gc = ChartCanvas.getGraphicsContext2D();

        AudioListView.setCellFactory(
                new Callback<ListView<AudioFile>, ListCell<AudioFile>>() {
                    @Override
                    public ListCell<AudioFile> call(ListView<AudioFile> param) {
                        return new AudioCell();
                    }
                }
        );

        AudioListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    lineChart.getData().removeAll();
                    lineChart.getData().clear();
                    var file = new File(newValue.GetFile().getAbsolutePath());
                    buzzer = new Media(file.toURI().toString());
                    mediaPlayer = new MediaPlayer(buzzer);
                    mediaPlayer.setAudioSpectrumListener(new SpektrumListener());
                    mediaPlayer.setOnReady(() ->{
                        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        timeLineLabel.setText(

                                "00:00/"+
                                        parseDuration(mediaPlayer.getTotalDuration())
                        );
                        double duration = mediaPlayer.getTotalDuration().toMillis();
                        double seconds =  mediaPlayer.getTotalDuration().toSeconds();
                        int[] bytes = extract(file);
                        XYChart.Series series1 = new XYChart.Series();
                        lineChart.setCreateSymbols(false);
                        lineChart.setAnimated(false);

                        int sum = 0;
                        int acc = (int) (bytes.length/4000);
                        for (int i = 0; i < (int) seconds*44_100; i++) {
                            sum+=bytes[i];

                            if(i%acc==0) {
                                series1.getData().add(new XYChart.Data(String.valueOf(i), sum/acc));
                                sum=0;
                            }
                            if(i%100000==0) {
                                System.out.println(i +" of " + bytes.length);
                            }

                        }
                        System.out.println("acc: " + acc);
                        System.out.println("duration: " + seconds);
                        System.out.println("bytes: "+bytes.length);
                        lineChart.getData().addAll(series1);
                    });

                }
        );

        ChartCanvas.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                (MouseEvent mouseEvent)->{
                    double x = mouseEvent.getX();
                    ch_gc.clearRect(0,0,ChartCanvas.getWidth(),ChartCanvas.getHeight());
                    ch_gc.setFill(new Color(0,0,0,0.5));
                    ch_gc.fillRect(
                            x,
                            0,1,ChartCanvas.getHeight()-34);

                    // 16+(ChartCanvas.getWidth()-6-16)*mediaPlayer.getCurrentTime().toSeconds()/mediaPlayer.getTotalDuration().toSeconds(),

                }
        );
        ChartCanvas.addEventHandler(
                MouseEvent.MOUSE_RELEASED,
                (MouseEvent mouseEvent)->{
                    double x = mouseEvent.getX();
                    double y = x*1000*mediaPlayer.getTotalDuration().toSeconds()/(ChartCanvas.getWidth());

                    mediaPlayer.pause();
                    mediaPlayer.seek(Duration.millis(y));

                    mediaPlayer.play();
                }
        );


    }


    private class SpektrumListener implements AudioSpectrumListener {
        int count = 0;
        double width = CanvasMonitor.getWidth();
        double height = CanvasMonitor.getHeight();
        @Override
        public void spectrumDataUpdate(double timestamp, double duration,
                                       float[] magnitudes, float[] phases) {
                count++;
                gc.setFill(new Color(1, 1, 1, 0.9));
                gc.fillRect(0, 0, width, height);
                gc.beginPath();
                for (int i = 0; i < magnitudes.length; i++) {

                    double x = (((1.6*i) * width  / magnitudes.length));
                    double y = (height+160-(magnitudes[i] + 120.0)*height/60);
                    gc.lineTo(x, y);
                    gc.stroke();
                }
                gc.closePath();
                gc.moveTo(0, 0);

        }
    }


    public int[] extract(File file){
        AudioInputStream in = null;
        try {
            in = AudioSystem.getAudioInputStream(file);
        } catch (Exception e) {
            System.out.println("Cannot read audio file");
            return new int[0];
        }
        AudioFormat format = in.getFormat();
        byte[] audioBytes;
        try {
            audioBytes = (in).readAllBytes();
        }catch (Exception e){
            System.out.println("Cannot read audio file");
            return new int[0];
        }

        int[] result = null;
        if (format.getSampleSizeInBits() == 16) {
            int samplesLength = audioBytes.length / 2;
            result = new int[samplesLength];
            if (format.isBigEndian()) {
                for (int i = 0; i < samplesLength; ++i) {
                    byte MSB = audioBytes[i * 2];
                    byte LSB = audioBytes[i * 2 + 1];
                    result[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for (int i = 0; i < samplesLength; i += 2) {
                    byte LSB = audioBytes[i * 2];
                    byte MSB = audioBytes[i * 2 + 1];
                    result[i / 2] = MSB << 8 | (255 & LSB);
                }
            }
        } else {
            int samplesLength = audioBytes.length;
            result = new int[samplesLength];
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                for (int i = 0; i < samplesLength; ++i) {
                    result[i] = audioBytes[i];
                }
            } else {
                for (int i = 0; i < samplesLength; ++i) {
                    result[i] = audioBytes[i] - 128;
                }
            }
        }

        return result;
        }
}






