package com.applex.utsav.registration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.ActivityNotification;
import com.applex.utsav.MainActivity;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.models.AccessToken;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.NotifCount;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Locale;

import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN = 123;
    EditText etEmail;
    EditText etPass;
    Button login;
    Button signup;
    TextView login_title,forgot_password;
    private SignInButton signin_google;
//    private Button phoneSignin;

    private FirebaseAuth mAuth;
    LottieAnimationView progress;

    TextView logintext;
    TextView signuptext;
    TextView acctInfo;
    GoogleSignInClient mGooglesigninclient;
    public FirebaseUser fireuser ;

    AccessToken accessToken;
    String tokenStr;
    boolean night_mode;
    TextView terms;
    IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(LoginActivity.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(task.getResult().getBoolean("night_mode")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_login);

        mAuth= FirebaseAuth.getInstance();
        fireuser = mAuth.getCurrentUser();

        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
        login_title=findViewById(R.id.login_title);

        signin_google = findViewById(R.id.signin_google);
//        phoneSignin = findViewById(R.id.phone);

        progress =findViewById(R.id.progressAnim);

        logintext = findViewById(R.id.login_text);
        signuptext = findViewById(R.id.signup_text);
        forgot_password = findViewById(R.id.forgot_password);
        acctInfo = findViewById(R.id.account_info);

        terms = findViewById(R.id.terms_conditions);
        TextView privacy = findViewById(R.id.privacy_policy);
        TextView cookies = findViewById(R.id.cookies);

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            ImageView imageView = findViewById(R.id.dhaki_png);

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            imageView.setImageBitmap(scaledBitmap);
        } else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            ImageView imageView = findViewById(R.id.dhaki_png);

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            imageView.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(LoginActivity.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("listener") != null && value.getBoolean("listener")) {
                                FirebaseFirestore.getInstance().document("Mode/night_mode")
                                        .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getBoolean("night_mode")) {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                        } else {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                        }
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                    new Handler().postDelayed(() -> {
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        terms.setMovementMethod(LinkMovementMethod.getInstance());
        privacy.setMovementMethod(LinkMovementMethod.getInstance());
        cookies.setMovementMethod(LinkMovementMethod.getInstance());

        Intent i = getIntent();

        ////FOR THOSE WHO REGISTER LATER///

        if(i.getStringExtra("value")!= null && i.getStringExtra("value").matches("1")){
            logintext.setVisibility(GONE);
            signuptext.setVisibility(View.VISIBLE);
            acctInfo.setText("Don't have an account yet?");
            login.setVisibility(View.VISIBLE);
            signup.setVisibility(GONE);
        }
        ///FOR THOSE WHO REGISTER LATER///

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGooglesigninclient = GoogleSignIn.getClient(this, googleSignInOptions);


        signin_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!InternetConnection.checkConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(),"Please check internet connection...",Toast.LENGTH_LONG).show();
                }
                else {
                    LoginActivity.this.SignInGoogle();
                }
            }
        });

//        phoneSignin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(LoginActivity.this, LoginActivityPhone.class));
//            }
//        });


        //////////////LOGIN/////////////////////

        signuptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logintext.setVisibility(View.VISIBLE);
                signuptext.setVisibility(GONE);
                acctInfo.setText("Already have an account?");
                forgot_password.setVisibility(GONE);
                login.setVisibility(GONE);
                signup.setVisibility(View.VISIBLE);
                etPass.setText("");
                login_title.setText("Sign Up");
            }
        });

        logintext.setOnClickListener(v -> {
            logintext.setVisibility(GONE);
            signuptext.setVisibility(View.VISIBLE);
            acctInfo.setText("Don't have an account yet?");
            forgot_password.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            signup.setVisibility(GONE);
            etPass.setText("");
            login_title.setText("Login");

        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etEmail.getText().toString().trim();

                if(email.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(email).matches()||email.isEmpty()) {
                    if (email.isEmpty()) {
                        etEmail.setError("Email missing");
                        etEmail.requestFocus();
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        etEmail.setError("Please enter a valid email");
                        etEmail.requestFocus();
                    }
                }
                else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Mail sent for resetting password", Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else{
                                Toast.makeText(LoginActivity.this, "Error in sending password reset mail", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(InternetConnection.checkConnection(LoginActivity.this)){
                    final String password = etPass.getText().toString().trim();
                    final String email = etEmail.getText().toString().trim();
                    if(email.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.isEmpty()) {
                        if (email.isEmpty()) {
                            etEmail.setError("Email missing");
                            etEmail.requestFocus();
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            etEmail.setError("Please enter a valid email");
                            etEmail.requestFocus();
                        }

                    }
                    else if (password.length()<6) {
                        etPass.setError("Password must contain at least 6 characters");
                        etPass.requestFocus();
                    }
                    else {
                        progress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
//                                            mAuth.getCurrentUser()
//                                                    .sendEmailVerification()
//                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                            if (task.isSuccessful()) {
//                                                                Toast.makeText(LoginActivity.this, "Please verify your email and register", Toast.LENGTH_LONG).show();
                                                                progress.setVisibility(GONE);

                                                                Intent intent = new Intent(LoginActivity.this, RegChoice.class);
                                                                intent.putExtra("value", "emailpass");
                                                                intent.putExtra("email", email);
                                                                intent.putExtra("password", password);

                                                                startActivity(intent);
                                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                finish();
//                                                            } else {
//                                                                Toast.makeText(LoginActivity.this, "Email verification failed", Toast.LENGTH_SHORT).show();
//                                                            }
//
//                                                        }
//                                                    });

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                                            progress.setVisibility(GONE);
                                        }
                                    }
                                });

                    }
                }
                else {
                    BasicUtility.showToast(LoginActivity.this,"Please check internet connection...");
                }

            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(InternetConnection.checkConnection(LoginActivity.this)){

                    final String password = etPass.getText().toString().trim();
                    final String email = etEmail.getText().toString().trim();

                    if(email.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(email).matches()||email.isEmpty()) {
                        if (email.isEmpty()) {
                            etEmail.setError("Email missing");
                            etEmail.requestFocus();
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            etEmail.setError("Please enter a valid email");
                            etEmail.requestFocus();
                        }
                    }
                    else if (password.length()<6) {
                        etPass.setError("Password must contain at least 6 characters");
                        etPass.requestFocus();
                    }
                    else {
                        progress.setVisibility(View.VISIBLE);
                        mAuth.signInWithEmailAndPassword(email,password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        if (authResult == null) {
                                            Toast.makeText(LoginActivity.this, "Either email or password incorrect", Toast.LENGTH_LONG).show();
                                            progress.setVisibility(GONE);
                                        } else {
//                                            FirebaseUser fireuser = mAuth.getCurrentUser();
//                                            if (fireuser.isEmailVerified()) {
                                                progress.setVisibility(GONE);

                                                FirebaseFirestore.getInstance().document("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/")
                                                        .get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if (documentSnapshot.exists()) {
                                                                    IntroPref introPref = new IntroPref(LoginActivity.this);
                                                                    BaseUserModel userModel = documentSnapshot.toObject(BaseUserModel.class);

                                                                    accessToken = new AccessToken();
                                                                    NotifCount notifCount = new NotifCount();

                                                                    FirebaseInstanceId.getInstance().getInstanceId()
                                                                            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                                                                @Override
                                                                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                                                                    if (instanceIdResult == null) {
                                                                                        Log.w("TAG", "getInstanceId failed");
                                                                                        BasicUtility.showToast(getApplicationContext(), "Error creating token");
                                                                                        return;
                                                                                    } else {
                                                                                        progress.setVisibility(GONE);
                                                                                        // Get new Instance ID token
                                                                                        tokenStr = instanceIdResult.getToken();
                                                                                        accessToken.setRegToken(tokenStr);
                                                                                        notifCount.setNotifCount(0);

                                                                                        FirebaseFirestore.getInstance()
                                                                                                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/AccessToken/")
                                                                                                .document("Token").set(accessToken);

                                                                                        FirebaseFirestore.getInstance()
                                                                                                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/notifCount/")
                                                                                                .document("notifCount").set(notifCount);

                                                                                        FirebaseFirestore.getInstance()
                                                                                                .collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                                .update("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                                                                        introPref.setType(userModel.getType());
                                                                                        introPref.setUserdp(userModel.getDp());
                                                                                        introPref.setFullName(userModel.getName());
                                                                                        introPref.setGender(userModel.getGender());
                                                                                        Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
                                                                                        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                        i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                        i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                        startActivity(i1);
                                                                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                                        finish();
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    BasicUtility.showToast(getApplicationContext(), "No internet connection");
                                                                                }
                                                                            });
                                                                } else {
                                                                    Intent i1 = new Intent(LoginActivity.this, RegChoice.class);
                                                                    i1.putExtra("value", "emailpass");
                                                                    i1.putExtra("password", password);
                                                                    i1.putExtra("email", email);
                                                                    startActivity(i1);
                                                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                    finish();
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                            }
                                                        });
//                                            }
//                                            else {
//                                                progress.setVisibility(GONE);
//                                                Toast.makeText(getApplicationContext(), "Email verification incomplete. Please verify your mail and try again.", Toast.LENGTH_LONG).show();
//                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progress.setVisibility(GONE);
                                        BasicUtility.showToast(getApplicationContext(), "Account does not exist or invalid credentials.");
                                    }
                                });

                    }
                } else BasicUtility.showToast(getApplicationContext(),"Check Internet connectivity.");
            }

        });

    }

    void SignInGoogle() {
        Intent signintent = mGooglesigninclient.getSignInIntent();
        startActivityForResult(signintent, GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {   GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null){
                    progress.setVisibility(View.VISIBLE);
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
//                Toast.makeText(LoginActivity.this, "2100", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle: " + account.getId());
        if(InternetConnection.checkConnection(getApplicationContext())){
            AuthCredential credential = GoogleAuthProvider
                    .getCredential(account.getIdToken(), "null");

            mAuth.signInWithCredential(credential)
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if(authResult != null) {
                                Log.d("TAG", "signin successful");
                                Toast.makeText(LoginActivity.this,"Signed in successfully",Toast.LENGTH_SHORT).show();

                                FirebaseFirestore.getInstance().document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if(documentSnapshot.exists()) {
                                                    IntroPref introPref= new IntroPref(LoginActivity.this);
                                                    BaseUserModel userModel = documentSnapshot.toObject(BaseUserModel.class);

                                                    accessToken = new AccessToken();
                                                    NotifCount notifCount= new NotifCount();
                                                    notifCount.setNotifCount(0);

                                                    FirebaseInstanceId.getInstance().getInstanceId()
                                                            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                                                @Override
                                                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                                                    if(instanceIdResult == null) {
                                                                        Log.w("TAG", "getInstanceId failed");
                                                                        BasicUtility.showToast(getApplicationContext(),"Error creating token");
                                                                        return;
                                                                    }
                                                                    else {

                                                                        progress.setVisibility(GONE);
                                                                        // Get new Instance ID token
                                                                        tokenStr = instanceIdResult.getToken();
                                                                        accessToken.setRegToken(tokenStr);

                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
                                                                                .document("Token").set(accessToken);

                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
                                                                                .document("notifCount").set(notifCount);

                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                                .update("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                                                            introPref.setType(userModel.getType());
                                                                            introPref.setUserdp(userModel.getDp());
                                                                            introPref.setFullName(userModel.getName());
                                                                            introPref.setGender(userModel.getGender());
                                                                            Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
                                                                            startActivity(i1);
                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                            finish();

                                                                    }
                                                                }
                                                            });
                                                }
                                                else {
                                                    Intent i = new Intent(LoginActivity.this, RegChoice.class);
                                                    i.putExtra("value", "google");
                                                    i.putExtra("email", account.getEmail());
                                                    i.putExtra("name", account.getDisplayName());
                                                    startActivity(i);
                                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    finish();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                            }
                                        });
                            }
                            else {
                                Log.w("TAG", "sign in failure");
                                Toast.makeText(LoginActivity.this, "Sign in failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                        }
                    });

//            mAuth.signInWithCredential(credential)
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                Log.d("TAG", "signin successful");
//                                Toast.makeText(LoginActivity.this,"Signed in successfully",Toast.LENGTH_SHORT).show();
//
//                                FirebaseFirestore.getInstance().document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/")
//                                        .get()
//                                        .addOnCompleteListener(task1 -> {
//                                            if(task1.getResult().exists()){
//                                                IntroPref introPref= new IntroPref(LoginActivity.this);
//                                                UserModel userModel = task1.getResult().toObject(UserModel.class);
//
//                                                accessToken = new AccessToken();
//                                                NotifCount notifCount= new NotifCount();
//                                                notifCount.setNotifCount(0);
//
//                                                FirebaseInstanceId.getInstance().getInstanceId()
//                                                        .addOnCompleteListener(t -> {
//                                                            if (!t.isSuccessful()) {
//                                                                Log.w("TAG", "getInstanceId failed", t.getException());
//                                                                BasicUtility.showToast(getApplicationContext(),"Error creating token");
//                                                                return;
//                                                            }
//                                                            else {
//
//                                                                progress.setVisibility(GONE);
//                                                                // Get new Instance ID token
//                                                                tokenStr = t.getResult().getToken();
//                                                                accessToken.setRegToken(tokenStr);
//
//                                                                FirebaseFirestore.getInstance()
//                                                                        .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
//                                                                        .document("Token").set(accessToken);
//
//                                                                FirebaseFirestore.getInstance()
//                                                                        .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
//                                                                        .document("notifCount").set(notifCount);
//
//
//                                                                introPref.setUsername(userModel.getUsername());
//                                                                introPref.setUserdp(userModel.getProfilepic());
//                                                                introPref.setInstitute(userModel.getInstitute());
//                                                                introPref.setFullName(userModel.getFirstname()+" "+userModel.getLastname());
//                                                                Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
//                                                                startActivity(i1);
//                                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                                finish();
//                                                            }
//
//                                                        });
//
//                                            }
//                                            else {
//                                                Intent i = new Intent(LoginActivity.this, RegistrationFormPost.class);
//                                                i.putExtra("value", "google");
//                                                i.putExtra("email", account.getEmail());
//                                                i.putExtra("name", account.getDisplayName());
//                                                startActivity(i);
//                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                finish();
//                                            }
//                                        });
//
//                            }
//
//                            else {
//                                Log.w("TAG", "sign in failure", task.getException());
//                                Toast.makeText(LoginActivity.this, "Sign in failed!", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

        }
        else {
            BasicUtility.showToast(getApplicationContext(), "Network unavailable...");
        }
    }

    @Override
    protected void onResume() {
        if(introPref.isFirstTime()) {
            super.onBackPressed();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        introPref.setIsFirstTime(true);
        super.onBackPressed();
    }
}