package burke.owen.fyp;

import android.content.Context;
import android.util.AttributeSet;

public class RecordButton extends androidx.appcompat.widget.AppCompatButton
{
    private static final String RECORD_STRING = "Record";
    private static final String STOP_STRING = "Stop";
    private boolean isRecording = false;

    public RecordButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void change_text()
    {
        if (isRecording)
        {
            this.setText(RecordButton.STOP_STRING);
            this.isRecording = false;
        }
        else
        {
            this.setText(RecordButton.RECORD_STRING);
            this.isRecording = true;
        }
    }
}
