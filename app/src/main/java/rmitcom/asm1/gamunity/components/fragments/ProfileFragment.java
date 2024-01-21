package rmitcom.asm1.gamunity.components.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rmitcom.asm1.gamunity.components.views.HomeView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;

public class ProfileFragment extends Fragment implements FirebaseFetchAndSetUI {
    private final FireBaseManager db = new FireBaseManager();
    private Constant constant = new Constant();

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

        if(((HomeView) currentView.getContext()).getCurrentItemPosition() == 3){
            setUI();
        }
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PROFILE_REQUEST) {
            if (resultCode == constant.EDIT) {

            }
        }

    }

    @Override
    public void fetchData() {

    }

    @Override
    public void setUI() {

        Intent intent = new Intent(currentView.getContext(), ProfileView.class);
        intent.putExtra("userId", db.getCurrentUser().getUid());
        startActivityForResult(intent, constant.PROFILE_REQUEST);
    }
}