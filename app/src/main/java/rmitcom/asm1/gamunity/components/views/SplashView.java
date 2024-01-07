package rmitcom.asm1.gamunity.components.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import rmitcom.asm1.gamunity.R;
public class SplashView extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.splashvideo;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Set a listener to start MainActivity after video completion
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(SplashView.this, LoginView.class);
            startActivity(intent);
            finish(); // Finish the splash screen activity to prevent going back to it
        });

        videoView.start();
    }
}
