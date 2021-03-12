package com.example.cmput301w21t25;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Experiment { //make abstract

    /****************************************
                    ATTRIBUTES
    ****************************************/
    private User owner;
    private String name;
    private String description;
    private String type;
    private ArrayList<String> keywords;
    private int minNumTrials;
    private boolean isPublished = false;

    private ArrayList<User> subscribedUsers = new ArrayList<User>(); //all currently subscribed users
    private ArrayList<User> allUsers = new ArrayList<User>(); //for users that were subscribed, added data, and unsubscribed
    private ArrayList<Trial> trials = new ArrayList<Trial>();

    //private Forum forum;

    //private Region region;
    //private ArrayList<Region> geoLocations;

    /****************************************
     CONSTRUCTORS
     ****************************************/
    public Experiment(User owner, String description, int minNumTrials) {
        this.owner = owner;
        this.description = description;
        this.minNumTrials = minNumTrials;
    }

    /****************************************
                    METHODS
    ****************************************/

    public ArrayList<Trial> getTrials() {
        return trials;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public ArrayList<String> getKeywords() { return keywords; }

    public void setTrials(ArrayList<Trial> trials) {
        this.trials = trials;
    }

    public void addTrial(Trial trial) {
        trials.add(trial);
    }

    public void deleteTrial(Trial trial) {
        trials.remove(trial);
    }

    public void addUser(User user) {
        allUsers.add(user);
        subscribedUsers.add(user);
    }

    public void deleteUser(User user) {
        subscribedUsers.remove(user);
    }

    /**
     * Allows experiment owner to hide trial results submitted by a User.
     * Throws exception if user's trials are already hidden. //Do we need to though?
     * @param user
     *      The user who's trials the owner wants to hide
     */
    public void hideTrials(User user) throws IllegalArgumentException{
        for (Trial trial : trials) {
            if (!trial.isHidden()) {
                trial.setHidden(true);
            }
        }

    }

    /**
     * Allows experiment owner to show trial results submitted by a User.
     * Throws exception if user's trials are already shown. //Again, is this necessary?
     * @param user
     *      The user who's trials the owner wants to show
     */
    void showTrials(User user) {
        for (Trial trial : trials) {
            if (trial.isHidden()) {
                trial.setHidden(false);
            }
        }

    }
    /**
     * Shows stats of experiment
     */
    void showStats() {

    }

    /**
     * Plots histogram (and maybe other plots) of experiment data
     */
    void plotData() {

    }

    /**
     * Shows a map of trial geolocations
     */
    void showMap() {

    }

}
