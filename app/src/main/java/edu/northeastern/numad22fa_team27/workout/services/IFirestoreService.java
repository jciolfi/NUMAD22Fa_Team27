package edu.northeastern.numad22fa_team27.workout.services;

import edu.northeastern.numad22fa_team27.workout.callbacks.WorkoutCallback;
import edu.northeastern.numad22fa_team27.workout.models.WorkoutCategory;

public interface IFirestoreService {
    /**
     * Create a group with the given groupName and userID
     * @param groupName name for the new group
     */
    void createGroup(String groupName);

    /**
     * Find workout by workoutName and/or workoutCategory
     * @param workoutName the name of the workout, matches on case-sensitive prefix
     * @param workoutCategory the category for the workout
     * @param callback executed on the entries returned by the query
     */
    void findWorkoutsByCriteria(String workoutName, WorkoutCategory workoutCategory, double maxDifficulty, double minDifficulty, WorkoutCallback callback, int resultLimit);

    /**
     * Find users with a given username, matches on case-sensitive prefix
     * @param username entered username to query on
     * @param callback executed on the entries returned by the query
     */
    void findUsersByUsername(String username, WorkoutCallback callback);

    /**
     * Find groups the current user is a part of
     * @param callback executed on the entries returned by the query
     */
    void findUserGroups(WorkoutCallback callback);

    /**
     * Find groups with a given groupName
     * @param groupName entered group name to query on
     * @param callback executed on the entries returned by the query
     */
    void findGroupsByName(String groupName, WorkoutCallback callback);

    /**
     * Get the global leaderboard by category sorted by best streak, limited to 100
     * @param callback executed on the entries returned by the query
     */
    void findStreaksLeaderboard(WorkoutCategory category, WorkoutCallback callback);

    /**
     * Try to join the group with the corresponding groupID. Users limited to 10 groups
     * @param groupID the ID for the group
     */
    boolean tryJoinGroup(String groupID);

    /**
     * Try to join the group with the corresponding groupID
     * @param groupID the ID for the group
     */
    boolean tryLeaveGroup(String groupID);

    /**
     * Retrieve details for user with given ID
     * @param userID the user's ID
     * @param callback executed on the entry returned by the query
     */
    void getUserByID(String userID, WorkoutCallback callback);

    /**
     * Try to send a friend request for the given friend
     * @param friendID ID of the user to friend
     * @return true if this userID shows up in friend's incoming requests, false otherwise
     */
    boolean tryRequestFriend(String friendID);

    /**
     * Try to accept a friend request
     * @param friendID ID of user that friended this user
     * @return true if users are friends, false otherwise
     */
    boolean tryAcceptFriendRequest(String friendID);

    /**
     * Try to remove friend - removes each other from both users' friends list
     * @param friendID the ID of friend to remove
     * @return true if users are not friends anymore, false otherwise
     */
    boolean tryRemoveFriend(String friendID);
}
