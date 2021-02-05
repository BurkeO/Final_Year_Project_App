package burke.owen.fyp;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.IOException;

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
        setOnClickListener(clicker);
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
        String audioFilePathRead = this.getContext().getFilesDir().getAbsolutePath()+"/audio.3gp";
        String audioFilePathSave = this.getContext().getFilesDir().getAbsolutePath()+"/audio.wav";
        FFmpeg.execute("-i " + audioFilePathRead + " " + audioFilePathSave);
        //TODO generate spectrograms from wav file
        FFmpeg.execute("-i " + audioFilePathSave +
                       " -y -lavfi showspectrumpic " + this.getContext().getFilesDir().getAbsolutePath()+"/spec.png");
        //TODO update imageview
    }
}
