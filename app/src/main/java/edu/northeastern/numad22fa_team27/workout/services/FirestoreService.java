package edu.northeastern.numad22fa_team27.workout.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.northeastern.numad22fa_team27.Util;
import edu.northeastern.numad22fa_team27.workout.callbacks.WorkoutCallback;
import edu.northeastern.numad22fa_team27.workout.models.DAO.GroupDAO;
import edu.northeastern.numad22fa_team27.workout.models.DAO.UserDAO;
import edu.northeastern.numad22fa_team27.workout.models.Group;
import edu.northeastern.numad22fa_team27.workout.models.WorkoutCategory;

public class FirestoreService implements IFirestoreService {
    private final String TAG = "FirestoreService";
    private final FirebaseFirestore firestoreDB;
    private final FirebaseAuth userAuth;
    private UserDAO currentUser;

    public FirestoreService() {
        firestoreDB = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        tryFetchUserDetails();
    }

    @Override
    public void createGroup(String groupName) {
        if (Util.stringIsNullOrEmpty(groupName)) {
            warnBadParam("createGroup");
            return;
        } else if (!tryFetchUserDetails()) {
            return;
        }

        // we already check if we can get the current user from tryFetchUserDetails, so it shouldn't be null
        String userID = Objects.requireNonNull(userAuth.getCurrentUser()).getUid();
        Group newGroup = new Group(groupName, userID);

        firestoreDB.collection("groups")
                .document(String.valueOf(newGroup.getGroupID()))
                .set(new GroupDAO(newGroup))
                .addOnSuccessListener(unused1 -> {
                    this.currentUser.joinedGroups.add(String.valueOf(newGroup.getGroupID()));
                    firestoreDB.collection("users")
                            .document(userID)
                            .set(this.currentUser)
                            .addOnSuccessListener(unused2 -> {
                                // TODO
                                Log.d(TAG, String.format("Successfully created group %s", groupName));
                            })
                            .addOnFailureListener(e -> logFailure("createGroup", e.getMessage()));

                })
                .addOnFailureListener(e -> logFailure("createGroup", e.getMessage()));
    }

    @Override
    public void findWorkoutsByCriteria(String workoutName, WorkoutCategory workoutCategory, WorkoutCallback callback) {
        if (Util.stringIsNullOrEmpty(workoutName) && workoutCategory == null) {
            warnBadParam("findWorkoutsByCriteria");
        } else if (Util.stringIsNullOrEmpty(workoutName)) {
            // search ONLY by workoutCategory
            firestoreDB.collection("workouts")
                    .whereArrayContains("categoriesPresent", String.valueOf(workoutCategory))
                    .get()
                    .addOnSuccessListener(callback::processQuery)
                    .addOnFailureListener(e -> logFailure("findWorkoutsByCriteria", e.getMessage()));
        } else if (workoutCategory == null) {
            // search ONLY by workoutName
            firestoreDB.collection("workouts")
                    .orderBy("workoutName")
                    .startAt(workoutName)
                    .endAt(workoutName + "\uf8ff")
                    .get()
                    .addOnSuccessListener(callback::processQuery)
                    .addOnFailureListener(e -> logFailure("findWorkoutsByCriteria", e.getMessage()));
        } else {
            // search by workoutCategory AND workoutName
            firestoreDB.collection("workouts")
                    .whereArrayContains("categoriesPresent", String.valueOf(workoutCategory))
                    .orderBy("workoutName")
                    .startAt(workoutName)
                    .endAt(workoutName + "\uf8ff")
                    .get()
                    .addOnSuccessListener(callback::processQuery)
                    .addOnFailureListener(e -> logFailure("findWorkoutsByCriteria", e.getMessage()));
        }
    }

    @Override
    public void findUsersByUsername(String username, WorkoutCallback callback) {
        if (Util.stringIsNullOrEmpty(username)) {
            warnBadParam("findUserByUsername");
            return;
        }

        firestoreDB.collection("users")
                .orderBy("username")
                .startAt(username)
                .endAt(username + "\uf8ff")
                .get()
                .addOnSuccessListener(callback::processQuery)
                .addOnFailureListener(e -> logFailure("findUserByUsername", e.getMessage()));
    }

    @Override
    public void findUserGroups(WorkoutCallback callback) {
        String userID = Objects.requireNonNull(userAuth.getCurrentUser()).getUid();

        firestoreDB.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserDAO user = documentSnapshot.toObject(UserDAO.class);
                    if (user == null) {
                        logFailure("findUserGroups", String.format("%s doesn't exist", userID));
                        return;
                    }

                    // get groups
                    firestoreDB.collection("groups")
                            .whereIn(FieldPath.documentId(), user.joinedGroups)
                            .get()
                            .addOnSuccessListener(callback::processQuery)
                            .addOnFailureListener(e -> logFailure("findUserGroups", e.getMessage()));
                })
                .addOnFailureListener(e -> logFailure("findUserGroups", e.getMessage()));
    }

    @Override
    public void findGroupsByName(String groupName, WorkoutCallback callback) {
        if (Util.stringIsNullOrEmpty(groupName)) {
            warnBadParam("findGroupsByName");
            return;
        }

        firestoreDB.collection("groups")
                .orderBy("groupName")
                .startAt(groupName)
                .endAt(groupName + "\uf8ff")
                .get()
                .addOnSuccessListener(callback::processQuery)
                .addOnFailureListener(e -> logFailure("findGroupsByName", e.getMessage()));
    }

    @Override
    public void findStreaksLeaderboard(WorkoutCategory category, WorkoutCallback callback) {
        firestoreDB.collection("users")
                .orderBy(String.format("bestCategoryStreaks.%s", category), Query.Direction.DESCENDING)
                .limit(100L)
                .get()
                .addOnSuccessListener(callback::processQuery)
                .addOnFailureListener(e -> logFailure("findStreaksLeaderboard", e.getMessage()));
    }

    @Override
    public boolean tryJoinGroup(String groupID) {
        if (Util.stringIsNullOrEmpty(groupID) || !tryFetchUserDetails()) {
            warnBadParam("joinGroup");
            return false;
        } else if (currentUser.joinedGroups.size() >= 10) {
            // not in same line as above to avoid race condition with getting currentUser
            return false;
        }

        String userID = userAuth.getCurrentUser().getUid();
        AtomicBoolean success = new AtomicBoolean(true);

        firestoreDB.collection("groups")
                .document(groupID)
                .get()
                .addOnSuccessListener(snapshot -> {
                    GroupDAO groupDAO = snapshot.toObject(GroupDAO.class);
                    if (groupDAO == null) {
                        success.set(false);
                    } else {
                        // add user to group
                        Group group = new Group(groupDAO, snapshot.getId());

                        if (group.getMembers().contains(userID)) {
                            success.set(false);
                        } else {
                            groupDAO.members.add(userID);
                            firestoreDB.collection("groups")
                                    .document(groupID)
                                    .set(groupDAO)
                                    .addOnFailureListener(e -> {
                                        success.set(false);
                                        logFailure("tryJoinGroup", e.getMessage());
                                    })
                                    .addOnSuccessListener(unused -> {
                                        // add group to user
                                        currentUser.joinedGroups.add(groupID);
                                        firestoreDB.collection("users")
                                                .document(userID)
                                                .set(currentUser)
                                                .addOnFailureListener(e -> {
                                                    success.set(false);
                                                    logFailure("tryJoinGroup", e.getMessage());
                                                });
                                    });
                        }
                    }
                });

        return success.get();
    }

    // ---------- Helpers ----------
    private boolean tryFetchUserDetails() {
        FirebaseUser user = userAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "Could not fetch current user");
            return false;
        }

        String userID = user.getUid();

        AtomicBoolean success = new AtomicBoolean(true);
        firestoreDB.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> currentUser = documentSnapshot.toObject(UserDAO.class))
                .addOnFailureListener(e -> success.set(false));

        return currentUser != null && success.get();
    }

    private void warnBadParam(String methodName) {
        Log.w(TAG, String.format("%s: fields were null", methodName));
    }

    private void logFailure(String methodName, String message) {
        Log.w(TAG, String.format("%s Failure: %s", methodName, message));
    }
}
