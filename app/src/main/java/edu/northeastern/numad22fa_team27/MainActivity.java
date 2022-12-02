package edu.northeastern.numad22fa_team27;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import edu.northeastern.numad22fa_team27.spotify.SpotifyActivity;
import edu.northeastern.numad22fa_team27.sticker_messenger.FirebaseActivity;
import edu.northeastern.numad22fa_team27.workout.activity.UserFriends;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnA7 = findViewById(R.id.btn_A7);
        btnA7.setOnClickListener(view -> Util.openActivity(this, SpotifyActivity.class));
        Button btnA8 = findViewById(R.id.btn_A8);
        btnA8.setOnClickListener(view -> Util.openActivity(this, FirebaseActivity.class));
        Button btnSU = findViewById(R.id.btn_proj);
        btnSU.setOnClickListener(view -> Util.openActivity(this, UserFriends.class));
    }

    private NavigationBarView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_leaderboard:
                            Intent intent = new Intent(MainActivity.this, SpotifyActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_profile:
                            intent = new Intent(MainActivity.this, FirebaseActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_workout:
//                            intent = new Intent(MainActivity.this, WorkoutList.class);
//                            startActivity(intent);
                            break;


                    }
                    return false;
                }
            };
}