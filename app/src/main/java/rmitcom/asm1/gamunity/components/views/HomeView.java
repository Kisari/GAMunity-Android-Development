package rmitcom.asm1.gamunity.components.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ViewPagerAdapter;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;

public class HomeView extends AppCompatActivity {

    private ViewPager viewPager;

    private ViewPagerAdapter pagerAdapter;

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);

        setUI();
    }

    private void setUI(){
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.firstItem).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.secondItem).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.thirdItem).setChecked(true);
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.lastItem).setChecked(true);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.firstItem){
                viewPager.setCurrentItem(0);
            }
            else if(item.getItemId() == R.id.secondItem){
                viewPager.setCurrentItem(1);
            }
            else if(item.getItemId() == R.id.thirdItem){
                viewPager.setCurrentItem(2);
            }
            else if(item.getItemId() == R.id.lastItem){
                viewPager.setCurrentItem(3);
            }
            return true;
        });
    }

    public void setFragmentItem(int position, String forumRef){
        viewPager.setCurrentItem(position);

        Intent toForumDetailView = new Intent(HomeView.this, ForumView.class);
        toForumDetailView.putExtra("forumId", forumRef);
        startActivity(toForumDetailView);

    }

    public Fragment getNotificationFragment(){
        return pagerAdapter.getItem(2);
    }

}