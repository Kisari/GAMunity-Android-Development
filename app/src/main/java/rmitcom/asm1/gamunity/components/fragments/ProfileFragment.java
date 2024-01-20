package rmitcom.asm1.gamunity.components.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import rmitcom.asm1.gamunity.ProfileView;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.views.LoginView;
import rmitcom.asm1.gamunity.components.views.profile.EditProfileView;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;

public class ProfileFragment extends Fragment implements FirebaseFetchAndSetUI {
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String profileId = userAuth.getUid();

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

        Intent testIntent = new Intent(view.getContext(), EditProfileView.class);
        startActivity(testIntent);

//        Intent intent = new Intent(view.getContext(), ProfileView.class);
//        intent.putExtra("userId", profileId);
//        startActivity(intent);

        setUI();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void fetchData() {

    }

    @Override
    public void setUI() {
//        TextView tempText = currentView.findViewById(R.id.tempTextLogout);
//        final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//        tempText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                userAuth.signOut();
//                Intent newIntent = new Intent(getContext(), LoginView.class);
//                startActivity(newIntent);
//            }
//        });
    }
}