package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.example.pujo360.models.AccessToken;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.NotifCount;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.registration.RegChoice;
import com.example.pujo360.util.InternetConnection;
import com.example.pujo360.util.Utility;
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

import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN = 123;
    EditText etEmail;
    EditText etPass;
    Button login;
    Button signup;
    //    TextView skip;
    TextView login_title,forgot_password;
    private SignInButton signin_google;

    private FirebaseAuth mAuth;
    LottieAnimationView progress;

    TextView logintext;
    TextView signuptext;
    TextView acctInfo;
    GoogleSignInClient mGooglesigninclient;
    public FirebaseUser fireuser ;

    AccessToken accessToken;
    String tokenStr;

    TextView terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth= FirebaseAuth.getInstance();
        fireuser = mAuth.getCurrentUser();

        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
        login_title=findViewById(R.id.login_title);

        signin_google = findViewById(R.id.signin_google);

        progress =findViewById(R.id.progressAnim);

        logintext = findViewById(R.id.login_text);
        signuptext = findViewById(R.id.signup_text);
        forgot_password = findViewById(R.id.forgot_password);
        acctInfo = findViewById(R.id.account_info);

        terms = findViewById(R.id.terms_conditions);
        TextView privacy = findViewById(R.id.privacy_policy);
        TextView cookies = findViewById(R.id.cookies);


        ///////////////Set Image Bitmap/////////////////////
        ImageView imageView = findViewById(R.id.dhaki_png);

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma2, options);
        int width = options.outWidth;
        if (width > displayWidth) {
            int widthRatio = Math.round((float) width / (float) displayWidth);
            options.inSampleSize = widthRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma2, options);
        imageView.setImageBitmap(scaledBitmap);
        ///////////////Set Image Bitmap/////////////////////

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


        ////////////////LOGIN/////////////////////

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
                                            mAuth.getCurrentUser()
                                                    .sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(LoginActivity.this, "Please verify your email and register", Toast.LENGTH_LONG).show();
                                                                progress.setVisibility(GONE);

                                                                Intent intent = new Intent(LoginActivity.this, RegChoice.class);
                                                                intent.putExtra("value", "emailpass");
                                                                intent.putExtra("email", email);
                                                                intent.putExtra("password", password);

                                                                startActivity(intent);
                                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(LoginActivity.this, "Email verification failed", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                        } else {
                                            Toast.makeText(LoginActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                                            progress.setVisibility(GONE);
                                        }
                                    }
                                });

//                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                    @Override
//                                    public void onSuccess(AuthResult authResult) {
//                                        if(authResult != null) {
//                                            mAuth.getCurrentUser()
//                                                    .sendEmailVerification()
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            Toast.makeText(LoginActivity.this, "Please verify your email and register", Toast.LENGTH_LONG).show();
//                                                            progress.setVisibility(GONE);
//                                                            Intent intent = new Intent(LoginActivity.this, RegistrationFormPost.class);
//                                                            intent.putExtra("value","emailpass");
//                                                            intent.putExtra("email",email);
//                                                            intent.putExtra("password",password);
//                                                            startActivity(intent);
//                                                            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
//                                                            finish();
//                                                        }
//                                                    })
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            Toast.makeText(LoginActivity.this, "Email verification failed", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    });
//                                        } else {
//                                            Toast.makeText(LoginActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
//                                            progress.setVisibility(GONE);
//                                        }
//                                    }
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Utility.showToast(getApplicationContext(), "No Internet Connection");
//                                    }
//                                });

                    }
                }
                else {
                    Utility.showToast(LoginActivity.this,"Please check internet connection...");
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
                                            FirebaseUser fireuser = mAuth.getCurrentUser();
                                            if (fireuser.isEmailVerified()) {
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
                                                                                        Utility.showToast(getApplicationContext(), "Error creating token");
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

//                                                                                        if(userModel.getInterestL() == null || userModel.getCourse() == null || userModel.getCoursestart() == null || userModel.getCourseend() == null){
//                                                                                            Toast.makeText(getApplicationContext(), "Please add some more details", Toast.LENGTH_LONG).show();
//                                                                                            Intent homeIntent = new Intent(LoginActivity.this, RegFormPost2.class);
//                                                                                            homeIntent.putExtra("firstname", userModel.getFirstname());
//                                                                                            homeIntent.putExtra("lastname", userModel.getLastname());
//                                                                                            introPref.setUserdp(userModel.getProfilepic());
//                                                                                            homeIntent.putExtra("about", userModel.getAbout());
//                                                                                            introPref.setUsername(userModel.getUsername());
//                                                                                            introPref.setUserdp(userModel.getProfilepic());
//                                                                                            introPref.setInstitute(userModel.getInstitute());
//                                                                                            introPref.setFullName(userModel.getFirstname() + " " + userModel.getLastname());
//
//                                                                                            startActivity(homeIntent);
//                                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                                                            finish();
//                                                                                        }
//                                                                                        else if(userModel.getCourse().isEmpty() || userModel.getCourseend().isEmpty() || userModel.getCoursestart().isEmpty()){
//                                                                                            Toast.makeText(getApplicationContext(), "Please add some more details", Toast.LENGTH_LONG).show();
//                                                                                            Intent homeIntent = new Intent(LoginActivity.this, RegFormPost2.class);
//                                                                                            homeIntent.putExtra("firstname", userModel.getFirstname());
//                                                                                            homeIntent.putExtra("lastname", userModel.getLastname());
//                                                                                            introPref.setUsername(userModel.getUsername());
//                                                                                            introPref.setUserdp(userModel.getProfilepic());
//                                                                                            introPref.setInstitute(userModel.getInstitute());
//                                                                                            introPref.setFullName(userModel.getFirstname() + " " + userModel.getLastname());
//
//                                                                                            startActivity(homeIntent);
//                                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                                                            finish();
//                                                                                        }
//                                                                                        else {
                                                                                            introPref.setType(userModel.getUsertype());
                                                                                            introPref.setUserdp(userModel.getDp());
//                                                                                            introPref.setInstitute(userModel.getInstitute());
                                                                                            introPref.setFullName(userModel.getName());
                                                                                            Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
                                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                            startActivity(i1);
                                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                                            finish();
//                                                                                        }
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Utility.showToast(getApplicationContext(), "No internet connection");
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
                                                                Utility.showToast(getApplicationContext(), "Something went wrong...");
                                                            }
                                                        });
                                            } else {
                                                progress.setVisibility(GONE);
                                                Toast.makeText(getApplicationContext(), "Email verification incomplete. Please verify your mail and try again.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progress.setVisibility(GONE);
                                        Utility.showToast(getApplicationContext(), "Account does not exist or invalid credentials.");
                                    }
                                });

//                                .addOnCompleteListener(task -> {
//                                    if (!task.isSuccessful()) {
//                                        Toast.makeText(LoginActivity.this, "Either email or password incorrect", Toast.LENGTH_LONG).show();
//                                        progress.setVisibility(GONE);
//                                    }
//                                    else {
//                                        FirebaseUser fireuser = mAuth.getCurrentUser();
//                                        if (fireuser.isEmailVerified()) {
//                                            progress.setVisibility(GONE);
//
//                                            FirebaseFirestore.getInstance().document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/")
//                                                    .get()
//                                                    .addOnCompleteListener(task1 -> {
//                                                        if(task1.getResult().exists()){
//                                                            IntroPref introPref= new IntroPref(LoginActivity.this);
//                                                            UserModel userModel = task1.getResult().toObject(UserModel.class);
//
//                                                            accessToken = new AccessToken();
//                                                            NotifCount notifCount= new NotifCount();
//
//                                                            FirebaseInstanceId.getInstance().getInstanceId()
//                                                                    .addOnCompleteListener(t -> {
//                                                                        if (!t.isSuccessful()) {
//                                                                            Log.w("TAG", "getInstanceId failed", t.getException());
//                                                                            Utility.showToast(getApplicationContext(),"Error creating token");
//                                                                            return;
//                                                                        }
//                                                                        else {
//                                                                            progress.setVisibility(GONE);
//                                                                            // Get new Instance ID token
//                                                                            tokenStr = t.getResult().getToken();
//                                                                            accessToken.setRegToken(tokenStr);
//                                                                            notifCount.setNotifCount(0);
//
//                                                                            FirebaseFirestore.getInstance()
//                                                                                    .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
//                                                                                    .document("Token").set(accessToken);
//
//                                                                            FirebaseFirestore.getInstance()
//                                                                                    .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
//                                                                                    .document("notifCount").set(notifCount);
//                                                                            introPref.setUsername(userModel.getUsername());
//                                                                            introPref.setUserdp(userModel.getProfilepic());
//                                                                            introPref.setInstitute(userModel.getInstitute());
//                                                                            introPref.setFullName(userModel.getFirstname()+" "+userModel.getLastname());
//                                                                            Intent i1 = new Intent(LoginActivity.this, MainActivity.class);
//                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                                            i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                                            startActivity(i1);
//                                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                                            finish();
//                                                                        }
//
//                                                                    });
//                                                        }
//                                                        else {
//                                                            Intent i1 = new Intent(LoginActivity.this, RegistrationFormPost.class);
//                                                            i1.putExtra("value", "emailpass");
//                                                            i1.putExtra("password", password);
//                                                            i1.putExtra("email", email);
//                                                            startActivity(i1);
//                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                                            finish();
//                                                        }
//                                                    });
//
//                                        }
//                                        else{
//                                            progress.setVisibility(GONE);
//                                            Toast.makeText(getApplicationContext(), "Email verification incomplete. Please verify your mail and try again.", Toast.LENGTH_LONG).show();
//                                        }
//                                }
//
//                });

                    }
                } else Utility.showToast(getApplicationContext(),"Check Internet connectivity.");
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
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try
            {   GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null){
                    progress.setVisibility(View.VISIBLE);
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "2100", Toast.LENGTH_SHORT).show();
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
                                                                        Utility.showToast(getApplicationContext(),"Error creating token");
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

                                                                            introPref.setType(userModel.getUsertype());
                                                                            introPref.setUserdp(userModel.getDp());
                                                                            introPref.setFullName(userModel.getName());
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
                                                Utility.showToast(getApplicationContext(), "Something went wrong...");
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
                            Utility.showToast(getApplicationContext(), "Something went wrong...");
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
//                                                                Utility.showToast(getApplicationContext(),"Error creating token");
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
            Utility.showToast(getApplicationContext(), "Network unavailable...");
        }

    }


}