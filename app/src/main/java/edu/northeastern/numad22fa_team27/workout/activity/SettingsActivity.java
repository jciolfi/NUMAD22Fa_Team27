package edu.northeastern.numad22fa_team27.workout.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import edu.northeastern.numad22fa_team27.R;
import edu.northeastern.numad22fa_team27.Util;
import edu.northeastern.numad22fa_team27.workout.models.universal_search.NavigationBar;

public class SettingsActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button saveBtn;
    private Button cancelBtn;
    private Button logoutBtn;
    private EditText newEmail, newPass, oldPass;
    private ProgressBar pb;
    private Uri imageUri;
    private Boolean picSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up a subset of the nav bar
        BottomNavigationView bottomNav = findViewById(R.id.navigation);
        bottomNav.setSelectedItemId(R.id.nav_messaging);
        bottomNav.setOnItemSelectedListener(NavigationBar.setNavListener(this));

        imageView = findViewById(R.id.profilePic);
        saveBtn = findViewById(R.id.saveButton);
        pb = findViewById(R.id.save_progressbar);
        cancelBtn = findViewById(R.id.cancelButton);
        logoutBtn = findViewById(R.id.sign_out);
        newEmail = findViewById(R.id.editTextEmail);
        newPass = findViewById(R.id.editTextNewPass);
        oldPass = findViewById(R.id.editTextOldPass);
        newEmail.setHint(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        newPass.setHint("New password");
        oldPass.setHint("Old Password");

        saveBtn.setOnClickListener(v -> saveChanges());
        cancelBtn.setOnClickListener(v -> Util.openActivity(SettingsActivity.this, ProfileActivity.class));


        logoutBtn.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(SettingsActivity.this, "Successfully signed out!", Toast.LENGTH_SHORT).show();
                Util.openActivity(SettingsActivity.this, LoginActivity.class);
            });

        imageView.setOnClickListener(v -> {
                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                photoIntent.setType("image/*");

                startActivityForResult(photoIntent, 1);
                picSelected = true;
        });

    }

    private void updateUsername(FirebaseUser user, String username, DocumentReference documentReference) {
        if (!username.isEmpty()) {
            if(isEmailValid(username)) {
                if (!username.equals(user.getEmail())) {
                    user.updateEmail(username).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pb.setVisibility(View.INVISIBLE);
                            documentReference.update("username", username);
                            Toast.makeText(SettingsActivity.this, "Email successfully updated!", Toast.LENGTH_SHORT).show();
                            newEmail.setText("");
                            newEmail.setHint(username);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Couldn't update the email!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    pb.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "New username cannot be same as the previous one!", Toast.LENGTH_SHORT).show();
                }
            } else {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(SettingsActivity.this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePassword(String pass, FirebaseUser user) {
        int min_pass_length = 6;
        boolean isValidPass = pass.length() >= min_pass_length;

        if (!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(oldPass.getText().toString())) {
            if (isValidPass) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), oldPass.getText().toString());
                user.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!pass.equals(oldPass.getText().toString())) {
                                    user.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                pb.setVisibility(View.INVISIBLE);
                                                newPass.setText("");
                                                oldPass.setText("");
                                                Toast.makeText(SettingsActivity.this, "Password successfully updated!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                pb.setVisibility(View.INVISIBLE);
                                                Toast.makeText(SettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    pb.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SettingsActivity.this, "New password cannot be same as the old one! Please Try again!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                pb.setVisibility(View.INVISIBLE);
                                Toast.makeText(SettingsActivity.this, "Old password is not correct!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Password length must be at least 6", Toast.LENGTH_SHORT).show();
            }
        } else {

            boolean isOnlyEmail = TextUtils.isEmpty(pass)
                    && TextUtils.isEmpty(oldPass.getText().toString())
                    && !TextUtils.isEmpty(newEmail.getText().toString())
                    && !picSelected;
            boolean isOnlyProfilePic = TextUtils.isEmpty(pass)
                    && TextUtils.isEmpty(oldPass.getText().toString())
                    && TextUtils.isEmpty(newEmail.getText().toString())
                    && picSelected;
            boolean allFields = !TextUtils.isEmpty(pass)
                    && !TextUtils.isEmpty(oldPass.getText().toString())
                    && !TextUtils.isEmpty(newEmail.getText().toString())
                    && picSelected;

            if (!allFields) {
                if (!isOnlyEmail && !isOnlyProfilePic) {
                    if (TextUtils.isEmpty(pass)) {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(this, "Please enter new pass!", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(oldPass.getText().toString())) {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(this, "Please enter old pass!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }

    }

    private void updateCreds() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (user != null) {
            String username = newEmail.getText().toString();
            String pass = newPass.getText().toString();
            updateUsername(user, username, documentReference);
            updatePassword(pass, user);

        }
    }

    private void changeProfilePic() {
        if (picSelected) {
            FirebaseStorage.getInstance()
                .getReference("images/" + FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .putFile(imageUri)
                .addOnCompleteListener(task -> {
                    pb.setVisibility(View.INVISIBLE);
                    if(task.isSuccessful()) {
                        task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    pb.setVisibility(View.INVISIBLE);
                                    updateProfilePic(task.getResult().toString());
                                    Toast.makeText(SettingsActivity.this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SettingsActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }

        // TODO
        // After profile pic saved change the imageview to default pic to avoid saving the
        // picture while updating other fields
    }

    private void saveChanges() {
        pb.setVisibility(View.VISIBLE);
        changeProfilePic();
        updateCreds();
    }

    private void updateProfilePic(String url) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("profilePic", url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            getImage();
        }
    }

    private void getImage() {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Upload not successful! Try again!", Toast.LENGTH_SHORT).show();
        }
        imageView.setImageBitmap(bitmap);
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


}