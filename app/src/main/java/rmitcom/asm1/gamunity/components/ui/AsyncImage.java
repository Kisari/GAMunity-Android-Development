package rmitcom.asm1.gamunity.components.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Objects;

public class AsyncImage extends AsyncTask<String, Void, Bitmap> {
    @SuppressLint("StaticFieldLeak")
    ImageView bmImage;

    public AsyncImage(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String URL = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(URL).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }


    protected void onPostExecute(Bitmap result) {
        if(hasImage(bmImage)){
            return;
        }
        bmImage.setImageBitmap(result);
    }
}
