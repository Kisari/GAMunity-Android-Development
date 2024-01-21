package rmitcom.asm1.gamunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import rmitcom.asm1.gamunity.components.views.HomeView;
import rmitcom.asm1.gamunity.components.views.SplashView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, SplashView.class);
        startActivity(intent);
    }
}