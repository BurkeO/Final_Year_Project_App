package burke.owen.fyp;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import static org.opencv.imgcodecs.Imgcodecs.imwrite;

import static org.opencv.imgproc.Imgproc.resize;

public class RecordButton extends androidx.appcompat.widget.AppCompatButton
{
    private static final String START_STRING = "Record";
    private static final String STOP_STRING = "Stop";
    private static final String LOG_TAG = "RecordButton";
    public boolean isRecording = false;
    private MediaRecorder recorder = null;
    private final File directory;

    final OnClickListener clicker = view -> {
        onRecord(isRecording);
        this.change_text();
    };


    public RecordButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.directory = new File(this.getContext().getFilesDir().getAbsolutePath());
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
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(this.getContext().getFilesDir().getAbsolutePath()+"/audio.mp3");
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

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
        deleteOldFiles(directory);
        String threeGpFile = directory.getAbsolutePath()+"/audio.mp3";
        String wavFile = directory.getAbsolutePath()+"/original.wav";
        FFmpeg.execute("-i " + threeGpFile + " " + wavFile);
        ArrayList<File> wavFilesArray = getSplitWavFiles(new File(wavFile));
        for(File wav : wavFilesArray)
        {
            makeImage(wav);
        }
    }

    private void deleteOldFiles(File directory)
    {
        File[] fileArray = directory.listFiles((file, name) -> name.endsWith(".wav") || name.endsWith(".png"));
        for (File file : fileArray)
        {
            file.delete();
        }
    }

    private void makeImage(File wavFile)
    {
        String imageFilename = getFileNameWithoutExtension(wavFile);
        FFmpeg.execute("-i " + wavFile.getAbsolutePath() + " -y -lavfi showspectrumpic=s=600x960:stop=10000 " + directory.getAbsolutePath()+"/" + imageFilename +".png");
        cropImage(new File(directory.getAbsolutePath()+"/" + imageFilename +".png"));
    }

    private String getFileNameWithoutExtension(File wavFile)
    {
        String fileName = "";
        try
        {
            if (wavFile != null && wavFile.exists())
            {
                String name = wavFile.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fileName = "";
        }
        return fileName;
    }

    private ArrayList<File> getSplitWavFiles(File wavFile)
    {
        ArrayList<File> splitWavFiles = new ArrayList<>();
        splitWavFiles.add(wavFile);
        return splitWavFiles;
    }

    private String secondsToMinSeconds(double seconds)
    {
        int minutes = (int)seconds/60;
        String minString = String.valueOf(minutes);
        seconds = seconds - (60*minutes);
        String secondsString = String.valueOf(seconds);
        if (seconds < 10)
            secondsString = "0" + seconds;
        if (minutes < 10)
            minString = "0" + minutes;
        return minString + ":" + secondsString;
    }

    private double getDurationInSeconds(File wavFile)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(wavFile.getAbsolutePath());
        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return (double)Long.parseLong(durationStr)/1000;
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
        int borderWidth = 120;
        int borderHeigth = 60;
        Rect crop = new Rect(borderWidth, borderHeigth, src.width()-(borderWidth*2), src.height()-(borderHeigth*2));
        Mat croppedImage = new Mat(src, crop);
        imageFile.delete();
        imwrite(imageFile.getAbsolutePath(), croppedImage);
    }
}
