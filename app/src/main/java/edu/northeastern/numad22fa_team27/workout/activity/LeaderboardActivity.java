package edu.northeastern.numad22fa_team27.workout.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.northeastern.numad22fa_team27.R;
import edu.northeastern.numad22fa_team27.workout.callbacks.GetLeaderboardCallback;
import edu.northeastern.numad22fa_team27.workout.fragments.UniversalSearchFragment;
import edu.northeastern.numad22fa_team27.workout.models.DAO.UserDAO;
import edu.northeastern.numad22fa_team27.workout.models.WorkoutCategory;
import edu.northeastern.numad22fa_team27.workout.models.leaderboard.LeaderboardAdapter;
import edu.northeastern.numad22fa_team27.workout.models.universal_search.NavigationBar;
import edu.northeastern.numad22fa_team27.workout.services.FirestoreService;

public class LeaderboardActivity extends AppCompatActivity {
    private final static String TAG = "LeaderboardActivity";
    private FirestoreService firestoreService;
    private final String[] prevCategory = new String[]{""};
    private List<String> categories;
    private RecyclerView leaderboardRV;
    private final List<UserDAO> users = new ArrayList<>();

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        firestoreService = new FirestoreService();

        // Set up nav bar
        BottomNavigationView bottomNav = findViewById(R.id.navigation);
        bottomNav.setSelectedItemId(R.id.nav_leaderboard);
        bottomNav.setOnItemSelectedListener(NavigationBar.setNavListener(this));
        FloatingActionButton fabSearch = findViewById(R.id.searchButton);

        AtomicBoolean searchHidden = new AtomicBoolean(true);
        UniversalSearchFragment search = new UniversalSearchFragment();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.fragmentSearch, search, "search")
                .hide(search)
                .commit();

        fabSearch.setOnClickListener(v -> {
            searchHidden.set(!searchHidden.get());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (searchHidden.get()) {
                transaction.hide(search).commit();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            } else {
                transaction.show(search).commit();
            }
        });
        // End setup nav bar

        // set up categories dropdown
        Spinner categoryDropdown = findViewById(R.id.dropdown_leaderboard_category);
        categories = WorkoutCategory.listCategories(true, true);

        List<SpannableString> categoriesWithColor = new ArrayList<>();
        for (String cat : categories) {
            SpannableString coloredTitle = new SpannableString(cat);
            coloredTitle.setSpan(new ForegroundColorSpan(R.color.md_theme_light_onBackground), 0, coloredTitle.length(), 0);
            categoriesWithColor.add(coloredTitle);
        }

        ArrayAdapter<SpannableString> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoriesWithColor);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryDropdown.setAdapter(categoryAdapter);
        categoryDropdown.setSelection(0);
        CategoryListener categoryListener = new CategoryListener();
        categoryDropdown.setOnItemSelectedListener(categoryListener);

        // set up recycler view
        leaderboardRV = findViewById(R.id.rv_leaderboard);
        leaderboardRV.setHasFixedSize(true);
        leaderboardRV.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRV.setAdapter(new LeaderboardAdapter(users, prevCategory));
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // don't do extra work if we don't need to (select same sort again)
            if (prevCategory[0].equals(categories.get(position))) {
                return;
            }

            // update leaderboard
            firestoreService.findStreaksLeaderboard(
                    WorkoutCategory.toCategory(categories.get(position)), new GetLeaderboardCallback(users, leaderboardRV));

            // update previous selected category to search on
            prevCategory[0] = categories.get(position);

            Objects.requireNonNull(leaderboardRV.getAdapter()).notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    }
}
