package rmitcom.asm1.gamunity.components.views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import rmitcom.asm1.gamunity.MainActivity;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.model.User;

public class SignupView extends AppCompatActivity {
    private EditText signupFName, signupLName, signupDOB, signupEmail, signupPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        signupFName = findViewById(R.id.signup_firstname);
        signupLName = findViewById(R.id.signup_lastname);
        signupDOB = findViewById(R.id.signup_birthday);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);

        Button signupButton = findViewById(R.id.signup_button);
        TextView loginRedirectText = findViewById(R.id.login_redirect);

        signupButton.setOnClickListener(view -> {
            String fName = signupFName.getText().toString().trim();
            String lName = signupLName.getText().toString().trim();
            String dob = signupDOB.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String pass = signupPassword.getText().toString().trim();

            if (fName.isEmpty()){
                signupFName.setError("First name cannot be empty");
            }
            if (lName.isEmpty()){
                signupLName.setError("Last name cannot be empty");
            }
            if (dob.isEmpty()){
                signupDOB.setError("Date of birth cannot be empty");
            }
            if (email.isEmpty()){
                signupEmail.setError("Email cannot be empty");
            }
            if (pass.isEmpty()){
                signupPassword.setError("Password cannot be empty");
            } else{
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User has been created successfully
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        String name = fName + " " + lName;

                        // Create a User object with the provided details
                        User newUser = new User(userId, false, name, dob, email);

                        // Add the user to Firestore
                        db.collection("USERS").document(userId)
                                .set(newUser)
                                .addOnSuccessListener(documentReference -> {
                                    // DocumentSnapshot added successfully
                                    Toast.makeText(SignupView.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupView.this, LoginView.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle errors in adding the user to Firestore
                                    Toast.makeText(SignupView.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

//                        Toast.makeText(SignupView.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SignupView.this, LoginView.class));
                    } else {
                        Toast.makeText(SignupView.this, "SignUp Failed" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(SignupView.this, LoginView.class));
            finish();
        });
    }
}