package app.splitbit.today.Application;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;


public class ConfigureApp extends Application {

    private SharedPreferences sharedPreferences;
    private static final String THEME_PREFERENCES = "themePreferences";
    private static final String IS_DARK_MODE = "isDarkMode";

    private static ConfigureApp mInstance;
    protected static boolean isVisible = false;
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        sharedPreferences = getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(IS_DARK_MODE,false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }




    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public static Context getInstance() {
        return mInstance;
    }

}
