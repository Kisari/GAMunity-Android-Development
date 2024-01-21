package rmitcom.asm1.gamunity.components.ui;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import rmitcom.asm1.gamunity.R;

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
            Context context = bmImage.getContext();
            Glide.with(context)
                    .applyDefaultRequestOptions(new RequestOptions()
                            .placeholder(R.mipmap.not_found_foreground)
                            .error(R.mipmap.not_found_foreground))
                    .load(urls)
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

}
