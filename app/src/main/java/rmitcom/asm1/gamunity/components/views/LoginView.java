package rmitcom.asm1.gamunity.components.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import rmitcom.asm1.gamunity.MainActivity;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.db.FireBaseManager;

public class LoginView extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private final FireBaseManager manager = new FireBaseManager();
    TextView forgotPassword;
    GoogleSignInOptions gOptions;
    GoogleSignInClient gClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView signup_redirect = findViewById(R.id.signup_redirect);
        forgotPassword = findViewById(R.id.forgot_password);
        ImageView loginGoogle = findViewById(R.id.google_btn);


        loginButton.setOnClickListener(v -> {

            String email = loginEmail.getText().toString();
            String pass = loginPassword.getText().toString();

            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    manager.getAuthProvider().signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                Toast.makeText(LoginView.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                manager.changeUserIdWithDeviceToken(Objects.requireNonNull(manager.getCurrentUser()).getUid(), LoginView.this);
                                startActivity(new Intent(LoginView.this, HomeView.class));
                                finish();
                            }).addOnFailureListener(e -> Toast.makeText(LoginView.this, "Login Failed", Toast.LENGTH_SHORT).show());
                } else {
                    loginPassword.setError("Empty fields are not allowed");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Empty fields are not allowed");
            } else {
                loginEmail.setError("Please enter correct email");
            }
        });

        signup_redirect.setOnClickListener(v -> startActivity(new Intent(LoginView.this, SignupView.class)));

        forgotPassword.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginView.this);
            View dialogView = getLayoutInflater().inflate(R.layout.fragment_forgotpw, null);
            EditText emailBox = dialogView.findViewById(R.id.reset_email);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            dialogView.findViewById(R.id.reset_button).setOnClickListener(view12 -> {
                String userEmail = emailBox.getText().toString();

                if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    Toast.makeText(LoginView.this, "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }
                manager.getAuthProvider().sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(LoginView.this, "Check your email", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginView.this, "Unable to send, failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            dialogView.findViewById(R.id.cancel_button).setOnClickListener(view1 -> dialog.dismiss());
            if (dialog.getWindow() != null){
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            dialog.show();

            // After showing the dialog, set its window layout parameters
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });

        //Inside onCreate
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null){
            finish();
            Intent intent = new Intent(LoginView.this, MainActivity.class);
            startActivity(intent);
            gClient.signOut(); // Logout
        }

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            task.getResult(ApiException.class);
                            Toast.makeText(LoginView.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginView.this, HomeView.class);
                            startActivity(intent);
                            finish();

                        } catch (ApiException e){
                            Toast.makeText(LoginView.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        loginGoogle.setOnClickListener(view -> {
            Intent signInIntent = gClient.getSignInIntent();
            activityResultLauncher.launch(signInIntent);
        });
    }
}