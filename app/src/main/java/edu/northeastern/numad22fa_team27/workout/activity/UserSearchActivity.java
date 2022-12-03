package edu.northeastern.numad22fa_team27.workout.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.northeastern.numad22fa_team27.R;
import edu.northeastern.numad22fa_team27.workout.callbacks.FindUsersCallback;
import edu.northeastern.numad22fa_team27.workout.models.DAO.UserDAO;
import edu.northeastern.numad22fa_team27.workout.models.user_search.UserAdapter;
import edu.northeastern.numad22fa_team27.workout.services.FirestoreService;

public class UserSearchActivity  extends AppCompatActivity {
    private final String TAG = "UserSearchActivity";
    private FirestoreService firestoreService;
    private Spinner sortDropdown;
    private String[] sortOptions;
    private String prevSort;
    private RecyclerView userRV;
    private final List<UserDAO> displayUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        firestoreService = new FirestoreService();

        // populate sort dropdown
        sortOptions = new String[]{"Name ↑", "Name ↓"};
        sortDropdown = findViewById(R.id.dropdown_friends_sort);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortDropdown.setAdapter(sortAdapter);
        sortDropdown.setSelection(0);
        prevSort = sortOptions[0];
        sortDropdown.setOnItemSelectedListener(new SortListener());

        // add query listener to search view
        SearchView userSearch = findViewById(R.id.sv_users);
        userSearch.setOnQueryTextListener(new UserQueryListener());

        // set up user recycler view
        userRV = findViewById(R.id.rv_users);
        userRV.setHasFixedSize(true);
        userRV.setLayoutManager(new LinearLayoutManager(this));
        userRV.setAdapter(new UserAdapter(displayUsers));
    }

    private class UserQueryListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            firestoreService.findUsersByUsername(query, new FindUsersCallback(displayUsers, userRV));

            // reset sort
            sortDropdown.setSelection(0);
            prevSort = sortOptions[0];

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class SortListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // don't do extra work if we don't need to (select same sort again)
            if (prevSort.equals(sortOptions[position])) {
                return;
            }
            prevSort = sortOptions[position];
            boolean shouldNotify = true;

            // sort by selected option
            // return <0 if w1 comes before w2, >0 if w2 comes before w1, =0 if tie
            switch (position) {
                // Name ↑ (ascending a->z)
                case 0: {
                    displayUsers.sort(Comparator.comparing(u -> u.username));
                    break;
                }
                // Name ↓ (descending: z-a)
                case 1: {
                    displayUsers.sort((u1, u2) -> -(u1.username.compareTo(u2.username)));
                    break;
                }
                default: {
                    shouldNotify = false;
                    break;
                }
            }

            if (shouldNotify) {
                Objects.requireNonNull(userRV.getAdapter()).notifyDataSetChanged();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    }
}
