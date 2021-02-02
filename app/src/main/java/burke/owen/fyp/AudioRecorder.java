package burke.owen.fyp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioRecorder#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioRecorder extends Fragment
{

    public AudioRecorder()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AudioRecorder.
     */
    public static AudioRecorder newInstance()
    {
        AudioRecorder fragment = new AudioRecorder();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            RecordButton button = (RecordButton)buttonView;
            button.change_text();
        });
        return view;
    }
}