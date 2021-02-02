package burke.owen.fyp;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

public class PlayButton extends androidx.appcompat.widget.AppCompatButton
{
    private static final String START_STRING = "Play";
    private static final String STOP_STRING = "Stop";
    private static final String LOG_TAG = "PlayButton";
    private MediaPlayer player = null;
    private boolean isPlaying = false;
    final OnClickListener clicker = view -> {
        onPlay(isPlaying);
        change_text();
    };

    public PlayButton(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        setOnClickListener(clicker);
    }

    private void onPlay(boolean isPlaying)
    {
        if (isPlaying)
        {
            stopPlaying();
            this.isPlaying = false;
        }
        else
        {
            startPlaying();
            this.isPlaying = true;
        }
    }

    public void change_text()
    {
        if (isPlaying)
        {
            this.setText(PlayButton.STOP_STRING);
        }
        else
        {
            this.setText(PlayButton.START_STRING);
        }
    }

    private void startPlaying()
    {
        player = new MediaPlayer();
        try
        {
            player.setDataSource(this.getContext().getFilesDir().getAbsolutePath()+"/temp.3gp");
            player.prepare();
            player.start();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void stopPlaying()
    {
        player.release();
        player = null;
    }
}
