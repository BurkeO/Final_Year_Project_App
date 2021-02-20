package burke.owen.fyp;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.arthenica.mobileffmpeg.FFmpeg;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import burke.owen.fyp.ml.Model;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioRecorderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioRecorderFragment extends Fragment
{

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    Model birdCallModel;
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
        birdCallModel.close();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try
        {
            this.birdCallModel = Model.newInstance(this.getContext());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_recorder, container, false);
        RecordButton recordButton = view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(buttonView -> {
            recordButton.onRecord(recordButton.isRecording);
            recordButton.change_text();
            File dir = new File(this.getContext().getFilesDir().getAbsolutePath());
            File[] pngFileArray = dir.listFiles((file, name) -> name.endsWith(".png"));
            if(!recordButton.isRecording)
            {
                HashMap<String, Double> speciesToTotalScore = new HashMap<>();
                for (int i = 0; i < pngFileArray.length; i++)
                {
                    File pngFile = pngFileArray[i];
                    Bitmap myBitmap = BitmapFactory.decodeFile(pngFile.getAbsolutePath());
                    if (i == 0)
                    {
                        ImageView myImage = view.findViewById(R.id.recordingImageView);
                        myImage.setImageBitmap(myBitmap);
                    }
                    TensorImage image = TensorImage.fromBitmap(myBitmap);

                    // Runs model inference and gets result.
                    Model.Outputs outputs = birdCallModel.process(image);
                    List<Category> probability = outputs.getProbabilityAsCategoryList();
                    for(Category prob : probability)
                    {
                        String label = prob.getLabel();
                        double currentScore = (speciesToTotalScore.containsKey(label)) ? speciesToTotalScore.get(label) : 0;
                        speciesToTotalScore.put(prob.getLabel(), currentScore+prob.getScore());
                    }
                }
                List<Map.Entry<String, Double>> list = new ArrayList<>(speciesToTotalScore.entrySet());
                StringBuilder text = new StringBuilder();
                list.sort(Map.Entry.comparingByValue());
                for (int j = list.size()-1; j >= 0; j--)
                {
                    text.append(list.get(j).getKey()).append(" : ").append(list.get(j).getValue()).append("\n");
                }
                TextView textView = view.findViewById(R.id.predictionsTextView);
                textView.setText(text);
                textView = view.findViewById(R.id.guessTextView);
                String guessText;
                if (list.get(list.size()-1).getValue() < 0.5*pngFileArray.length)
                    guessText = "Couldn't get better than 50% average confident ";
                else
                    guessText = "Best Guess is " + list.get(list.size()-1).getKey() + " over " + pngFileArray.length + " images";
                textView.setText(guessText);
            }
        });
        return view;
    }
}