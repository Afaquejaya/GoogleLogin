package com.example.friendster.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.friendster.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    ProgressBar progressBar;
    private SignInButton signInButton;
    private Button btnSignOut;

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 101;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.signin_button);
        progressBar = findViewById(R.id.progresbar);
        btnSignOut = findViewById(R.id.sign_out_button);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleSignInClient.signOut();
                Toast.makeText(LoginActivity.this,"You are Logged Out",Toast.LENGTH_SHORT).show();
                btnSignOut.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    //dilog box will appear for google login all a/c
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //once user click on email address, this run in bk ground & verify
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken()); //once verified we will get the USER TOKEN
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    // ==BAsed upon (getIdToken) we are verifying our user
    private void firebaseAuthWithGoogle(String idToken) {
       // progressBar.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser(); // This will contain different =DETAILS=  about of the user.
                            updateUI(user);
                           /* Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("message_key", user);*/
                            // start the Intent
                            //startActivity(intent);

                            /*Map<String, String> userDetails = new HashMap<>();
                            userDetails.put("name", user.getDisplayName());
                            userDetails.put("email", user.getEmail());
*/
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();

                            updateUI(null);
                        }
                    }


                           /* final FirebaseUser user = mAuth.getCurrentUser();
                            Map<String, String> userDetails = new HashMap<>();
                            userDetails.put("name", user.getDisplayName());
                            userDetails.put("email", user.getEmail());
                            userDetails.put("profileUrl", user.getPhotoUrl().toString());
                            db.collection("users").document(user.getUid()).set(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        updateUI(user);
                                    } else {
                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();

                                        updateUI(null);
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            // updateUI(null);
                        }
                        }*/


                });
    }

    private void updateUI(FirebaseUser user) {
       // progressBar.setVisibility(View.GONE);
        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (user != null) {
            String personName = account.getDisplayName(); //or  String personName = user.getDisplayName();
            String personEmail = account.getEmail();
            String phone = user.getPhoneNumber();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();

            Toast.makeText(LoginActivity.this, personName +"\n"+ personEmail , Toast.LENGTH_SHORT).show();
            // ==perform rest of our tasks===
            // startActivity(new Intent(this, MainActivity.class));
           /* signInButton.setVisibility(View.GONE);
            finish();
            Log.d(TAG, user.getDisplayName());
            Log.d(TAG, user.getEmail());
            Log.d(TAG, user.getPhotoUrl().toString());
            Log.d(TAG, user.getPhoneNumber());
        } else {
            signInButton.setVisibility(View.VISIBLE);
        }*/
        }
    }

}
