package com.example.finalproject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Vignesh on 12/13/2015.
 */
public class User implements Parcelable, Comparable<User> {
    private String userId;
    private String userName;
    private String firstName;
    private String lastName;

    public String fullName(){
        return this.firstName + " " + this.lastName;
    }

    @Override
    public int compareTo(User another){
        return this.fullName().compareTo(another.fullName());
    }

    public User(String userId, String userName, String firstName, String lastName){
        this.userId = userId;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(firstName);
        dest.writeString(lastName);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){
        public User createFromParcel(Parcel in){
            return new User(in);
        }

        public User[] newArray(int size){
            return new User[size];
        }
    };

    private User(Parcel in){
        this.userId = in.readString();
        this.userName = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
    }
}
