package rmitcom.asm1.gamunity.adapter;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import rmitcom.asm1.gamunity.components.fragments.ChatFragment;
import rmitcom.asm1.gamunity.components.fragments.HomeFragment;
import rmitcom.asm1.gamunity.components.fragments.NotificationFragment;
import rmitcom.asm1.gamunity.components.fragments.ProfileFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "View pager";

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Log.d(TAG, "getItem: HomeFragment" + position);
                return new HomeFragment();
            case 1:
                Log.d(TAG, "getItem: ChatFragment" + position);
                return new ChatFragment();
            case 2:
                Log.d(TAG, "getItem: NotificationFragment" + position);
                return new NotificationFragment();
            case 3:
                Log.d(TAG, "getItem: ProfileFragment" + position);
                return new ProfileFragment();

            default:
                return new Fragment();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }

}
