package burke.owen.fyp;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioRecorderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioRecorderFragment extends Fragment
{

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    RecordButton recordButton;
    private boolean permissionToRecordAccepted = false;

    public AudioRecorderFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AudioRecorderFragment.
     */
    public static AudioRecorderFragment newInstance()
    {
        AudioRecorderFragment fragment = new AudioRecorderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
        {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted)
            finish();

    }

    private void finish()
    {
        //TODO do something if permission not granted
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_recorder, container, false);
        RecordButton recordButton = (RecordButton) view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(buttonView -> {
            recordButton.onRecord(recordButton.isRecording);
            recordButton.change_text();
            File imgFile = new  File(this.getContext().getFilesDir().getAbsolutePath()+"/spec.png");
            if(imgFile.exists()){
                Mat src = Imgcodecs.imread(this.getContext().getFilesDir().getAbsolutePath()+"/spec.png");
                Mat resizeImage = new Mat();
                Size scaleSize = new Size(32,32);
                resize(src, resizeImage, scaleSize , 0, 0, INTER_AREA);
                Imgcodecs.imwrite(this.getContext().getFilesDir().getAbsolutePath()+"/spec.png", resizeImage);
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView myImage = (ImageView) view.findViewById(R.id.recordingImageView);
                myImage.setImageBitmap(myBitmap);
            }
        });
        return view;
    }
}