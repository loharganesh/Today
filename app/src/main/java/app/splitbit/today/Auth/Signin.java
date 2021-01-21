package app.splitbit.today.Auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import app.splitbit.today.MainActivity;
import app.splitbit.today.R;

public class Signin extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private ProgressBar progress_bar;
    private LinearLayout button_signin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //initializing layout components
        initLayoutComponents();

        //google sign in
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        auth = FirebaseAuth.getInstance();

    }

    //Initializing Activity UI
    private void initLayoutComponents(){
        progress_bar = (ProgressBar) findViewById(R.id.signing_in_indicator);
        button_signin = (LinearLayout) findViewById(R.id.button_signin);

        button_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress_bar.setVisibility(View.VISIBLE);
                button_signin.setEnabled(false);
                signIn();
            }
        });

    }

    //Google authentication process
    private void signIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                //Google Signin is successfull
                //Authenticating user to firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            }catch (Exception e){
                e.printStackTrace();
                progress_bar.setVisibility(View.INVISIBLE);
                button_signin.setEnabled(true);
            }
        }else{

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d("Firebase Auth ID",account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        auth.signInWithCredential(credential)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        //Sign in successfull, handle user info and UI here
                        if(auth.getCurrentUser() != null){
                           updateUI(auth.getCurrentUser());
                        }

                    }else{
                        Log.d("Sign in status","Sign In Failed "+task.getException());
                        progress_bar.setVisibility(View.INVISIBLE);
                        button_signin.setEnabled(true);
                    }
                }
            });
    }


    private void updateUI(FirebaseUser user){
        if(user!=null){
            startActivity(new Intent(Signin.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        }else{

        }
    }

}
