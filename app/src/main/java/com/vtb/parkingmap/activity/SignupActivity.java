package com.vtb.parkingmap.activity;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.auth.RegisterRequest;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

@SuppressLint("all")
@SuppressWarnings("all")
@Getter
public final class SignupActivity extends BaseSaigonParkingActivity {
    private static final String TAG = "SignupActivity";

    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_lastname)
    EditText _lastnameText;
    @BindView(R.id.input_username)
    EditText _usernameText;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_mobile)
    EditText _mobileText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;


    String firstName;
    String lastName;
    String userName;
    String email;
    String mobile;
    String password;
    String reEnterPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");
        firstName = _nameText.getText().toString();
        lastName = _lastnameText.getText().toString();
        userName = _usernameText.getText().toString();
        email = _emailText.getText().toString();
        mobile = _mobileText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        // TODO: Implement your own signup logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {

        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        RegisterRequest request = RegisterRequest.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setUsername(userName)
                .setPassword(password)
                .setEmail(email)
                .setPhone(mobile)
                .build();

        callApiWithExceptionHandling(() -> {
            String addedEmail = serviceStubs.getAuthServiceBlockingStub().registerUser(request).getValue();
            AlertDialog.Builder alert = new AlertDialog.Builder(SignupActivity.this);

            alert.setTitle("Sign up Finish!");
            alert.setMessage("Activate link has been sent to" + addedEmail +
                    ". Please check your email to activate account !");
            alert.setPositiveButton("OK", (dialogInterface, i) -> {


                Intent intent2 = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            });
            AlertDialog dialog = alert.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(R.color.colorPrimary));
//            Toast.makeText(getBaseContext(),
//                    String.format("Activate link has been sent to %s email. " +
//                            "Please check your email to activate account !", addedEmail), Toast.LENGTH_LONG).show();
        });
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;


        if (firstName.isEmpty() || firstName.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (lastName.isEmpty()) {
            _lastnameText.setError("at least 3 characters");
            valid = false;
        } else {
            _lastnameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }
}