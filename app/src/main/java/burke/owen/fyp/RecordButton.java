package burke.owen.fyp;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.INTER_AREA;

import static org.opencv.imgproc.Imgproc.resize;

public class RecordButton extends androidx.appcompat.widget.AppCompatButton
{
    private static final String START_STRING = "Record";
    private static final String STOP_STRING = "Stop";
    private static final String LOG_TAG = "RecordButton";
    public boolean isRecording = false;
    private MediaRecorder recorder = null;

    final OnClickListener clicker = view -> {
        onRecord(isRecording);
        this.change_text();
    };


    public RecordButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //setOnClickListener(clicker);
    }


    public void onRecord(boolean isRecording)
    {
        if (isRecording)
        {
            stopRecording();
            this.isRecording = false;
        }
        else
        {
            startRecording();
            this.isRecording = true;
        }
    }

    public void change_text()
    {
        if (isRecording)
        {
            this.setText(RecordButton.STOP_STRING);
        }
        else
        {
            this.setText(RecordButton.START_STRING);
        }
    }


    private void startRecording()
    {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(this.getContext().getFilesDir().getAbsolutePath()+"/audio.3gp");
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try
        {
            recorder.prepare();
            recorder.start();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void stopRecording()
    {
        recorder.stop();
        recorder.release();
        recorder = null;
        makeImage();
    }

    private void makeImage()
    {
        File directory = new File(this.getContext().getFilesDir().getAbsolutePath());
        File[] fileArray = directory.listFiles((file, name) -> name.endsWith(".wav") || name.endsWith(".png"));
        for (File file : fileArray)
        {
            file.delete();
        }
        String audioFilePathRead = directory.getAbsolutePath()+"/audio.3gp";
        String audioFilePathSave = directory.getAbsolutePath()+"/original.wav";
        FFmpeg.execute("-i " + audioFilePathRead + " " + audioFilePathSave);
        processWav(new File(audioFilePathSave), directory);
        makeImages(directory);
    }

    private void processWav(File audioFilePathSave, File directory)
    {
        String normFilename = directory.getAbsolutePath()  + "/norm.wav";
//        String passFilename = directory + "/pass.wav";
//        String noiseFilename = directory + "/afftdn.wav";
//        String silenceFilename = directory + "/silence.wav";
//        int code = FFmpeg.execute("-i " + audioFilePathSave.getAbsolutePath() + " -af loudnorm " + normFilename);
//        if (code != 0 )
//        {
//            int temp = 2;
//        }
//        FFmpeg.execute("-i " + normFilename + " -af \"highpass=f=22, lowpass=f=9000\" " + passFilename);
//        FFmpeg.execute("-i " + passFilename + " -af afftdn " + noiseFilename);
//        FFmpeg.execute("-i " + noiseFilename + " -af silenceremove=stop_periods=-1:stop_duration=1:stop_threshold=-46dB " + silenceFilename);
//        FFmpeg.execute("-i " + silenceFilename + " -f segment -segment_time 3 -c copy " + directory.getAbsolutePath() + "/audio%03d.wav");
//
//        new File(normFilename).delete();
//        new File(passFilename).delete();
//        new File(noiseFilename).delete();
//        new File(silenceFilename).delete();
        FFmpeg.execute("-i " + audioFilePathSave.getAbsolutePath() + " -f segment -segment_time 6 -c copy " + directory.getAbsolutePath() + "/audio%03d.wav");
        //FFmpeg.execute("-i " + normFilename + " -f segment -segment_time 6 -c copy " + directory.getAbsolutePath() + "/audio%03d.wav");
    }

    private void makeImages(File directoryPath)
    {
        File[] wavFileArray = directoryPath.listFiles((file, name) -> name.endsWith(".wav") && !name.contains("original"));
        for (int i = 0; i < wavFileArray.length; i++)
        {
            File wavFile = wavFileArray[i];
            if (!wavFile.getName().startsWith("audio"))
                continue;
            FFmpeg.execute("-i " + wavFile.getAbsolutePath() +
                                   " -y -lavfi showspectrumpic=stop=10000 " + directoryPath.getAbsolutePath()+"/spec_"+ i +".png");
            cropImage(new File(directoryPath.getAbsolutePath()+"/spec_"+ i +".png"));
        }
    }

    private void cropImage(File imageFile)
    {
        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        int borderWidth = 115;
        int borderHeigth = 58;
        Rect crop = new Rect(borderWidth, borderHeigth, src.width()-(borderWidth*2), src.height()-(borderHeigth*2));
        Mat croppedImage = new Mat(src, crop);
        imageFile.delete();
        imwrite(imageFile.getAbsolutePath(), croppedImage);
    }
}
