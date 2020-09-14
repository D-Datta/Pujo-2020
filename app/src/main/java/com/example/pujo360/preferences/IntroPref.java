package com.example.pujo360.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class IntroPref {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "com.applex.campus24.users";
    private static final String IS_FIRST_TIME_LAUNCH = "firstTime";
    private static final String USERDP = "userdp";
    private static final String FULLNAME = "fullname";
    private static final String DEFAULTDP = "defaultdp";
    private static final String TYPE= "type";
    private static final String GENDER= "gender";
    private static final String ACCOUNT= "account";
    private static final String FOLDER = "preptotal";
    private static final String BATCHID = "batchID";
    private static final String CITY = "city";
    private Gson gson;

    @SuppressLint("CommitPrefEdits")
    public IntroPref(Context context){
        this.context = context;
        if(context != null) {
            preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        }
        editor = preferences.edit();
        gson = new Gson();
    }

    public void setIsFirstTimeLaunch(boolean firstTimeLaunch)
    {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH,firstTimeLaunch);
        editor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return preferences.getBoolean(IS_FIRST_TIME_LAUNCH,true);
    }


    //GENDER////
    public String getGender(){
        return preferences.getString(GENDER, null);
    }

    public void setGender(String gender){
        editor.putString(GENDER, gender);
        editor.apply();
    }
    //GENDER////

    //CITY////
    public String getCity(){
        return preferences.getString(CITY, null);
    }

    public void setCity(String city){
        editor.putString(CITY, city);
        editor.apply();
    }
    //CITY////

    ///USERDP///
    public String getUserdp(){
        return preferences.getString(USERDP, null);
    }

    public void setUserdp(String userdp){
        editor.putString(USERDP, userdp);
        editor.apply();
    }
    ///USERDP///

    ///DEFAULTDP///
    public String getDefaultdp(){
        return preferences.getString(DEFAULTDP, null);
    }

    public void setDefaultdp(String defaultdp){
        editor.putString(DEFAULTDP, defaultdp);
        editor.apply();
    }
    ///DEFAULTDP///


    ///FULLNAME///
    public String getFullName(){
        return preferences.getString(FULLNAME, null);
    }

    public void setFullName(String fullName){
        editor.putString(FULLNAME, fullName);
        editor.apply();
    }
    ///FULLNAME///

    ///TYPE///
    public String getType(){
        return preferences.getString(TYPE, null);
    }

    public void setType(String type){
        editor.putString(TYPE, type);
        editor.apply();
    }
    ///TYPE///

    ///PREPTOTAL FOLDER///
    public String getFolderID() { return preferences.getString(FOLDER, null); }

    public void setFolderID(String folderID) {
        editor.putString(FOLDER, folderID);
        editor.apply();
    }
    ///PREPTOTAL FOLDER///

    ///ALL OTHER FOLDERS///
    public String getSpecificFolderID(String folder) { return preferences.getString(folder, null); }

    public void setSpecificFolderID(String folder, String folderID) {
        editor.putString(folder, folderID);
        editor.apply();
    }
    ///ALL OTHER FOLDERS///

    ///BATCH ID///
    public String getBatchID() { return preferences.getString(BATCHID, null); }

    public void setBatchID(String batchID) {
        editor.putString(BATCHID, batchID);
        editor.apply();
    }
    ///BATCH ID///

    public GoogleSignInAccount getGoogleSignInAccount() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriDeserializer())
                .create();
        String json = preferences.getString(ACCOUNT, "");
        return gson.fromJson(json, GoogleSignInAccount.class);
    }

    public void setGoogleSignInAccount(GoogleSignInAccount googleSignInAccount) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriSerializer())
                .create();
        String json = gson.toJson(googleSignInAccount);
        editor.putString(ACCOUNT, json);
        editor.apply();
    }

    public static class UriSerializer implements JsonSerializer<Uri> {
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(final JsonElement src, final Type srcType,
                               final JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(src.getAsString());
        }
    }
}