package rmitcom.asm1.gamunity.components.ui;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class AsyncImage {
    @SuppressLint("StaticFieldLeak")
    ImageView bmImage;
    ProgressBar progressBar;

    public AsyncImage(ImageView bmImage, ProgressBar progressBar) {
        this.bmImage = bmImage;
        this.progressBar = progressBar;
    }

    public void loadImage(String urls) {
        try {
            Glide.with(bmImage).load(urls)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "AsyncImageFailed with urls: " + urls);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(bmImage);
        }
        catch (Exception e){
            Log.e(TAG, "loadImage: ", e);
            e.printStackTrace();
        }
    }

//    protected boolean hasImage(@NonNull ImageView view) {
//        Drawable drawable = view.getDrawable();
//        boolean hasImage = (drawable != null);
//
//        if (hasImage && (drawable instanceof BitmapDrawable)) {
//            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
//        }
//
//        return hasImage;
//    }

}
