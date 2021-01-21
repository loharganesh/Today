package app.splitbit.today;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import app.splitbit.today.Auth.Signin;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth auth;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            startActivity(new Intent(SplashScreen.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
            finish();
        }else{
            startActivity(new Intent(this, Signin.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle());
            finish();
        }
    }
}
