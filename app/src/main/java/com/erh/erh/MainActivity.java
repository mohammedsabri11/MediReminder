package com.erh.erh;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.erh.erh.fragments.AboutUsFragment;
import com.erh.erh.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements OnFragment  {

    FragmentManager fragmentManager;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_aboutus:
                    fragmentManager.beginTransaction().replace(R.id.container_fragment, new AboutUsFragment()).commit();
                    return true;
                case R.id.navigation_scan:
                    startActivity(new Intent(MainActivity.this,ScanActivity.class));
                    return true;
                case R.id.navigation_settings:
                    fragmentManager.beginTransaction().replace(R.id.container_fragment, new SettingsFragment()).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager=getSupportFragmentManager();


        if(new DarkModePrefManager(this).isNightMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fragmentManager.beginTransaction().replace(R.id.container_fragment, new AboutUsFragment()).commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }


    @Override
    public void onDarkMode() {
    recreate();
    }



}






