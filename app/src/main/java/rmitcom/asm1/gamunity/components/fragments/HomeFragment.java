package rmitcom.asm1.gamunity.components.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.views.AddForumForm;
import rmitcom.asm1.gamunity.components.views.ForumView;
import rmitcom.asm1.gamunity.components.views.HomeView;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;

public class HomeFragment extends Fragment implements FirebaseFetchAndSetUI {

    private View currentView;
    private Button addForumButton;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        currentView = view;
        // Inflate the layout for this fragment
        Toast.makeText(getActivity(), "onCreateView", Toast.LENGTH_SHORT).show();
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUI();
    }


    @Override
    public void fetchData() {

    }

    @Override
    public void setUI() {
        if (currentView == null) {
            Log.e("HomeFragment", "currentView is null");
            return;
        }

        addForumButton = currentView.findViewById(R.id.addForumForm);

        addForumForm();
    }

    private void addForumForm() {
        addForumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Home Fragment", Toast.LENGTH_SHORT).show();
                Intent addIntent = new Intent(getActivity(), ForumView.class);
                startActivity(addIntent);
            }
        });
    }
}