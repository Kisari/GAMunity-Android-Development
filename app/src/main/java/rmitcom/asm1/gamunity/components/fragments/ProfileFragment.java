package rmitcom.asm1.gamunity.components.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;

public class ProfileFragment extends Fragment implements FirebaseFetchAndSetUI {

    View currentView;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        currentView = view;
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void fetchData() {

    }

    @Override
    public void setUI() {

    }
}