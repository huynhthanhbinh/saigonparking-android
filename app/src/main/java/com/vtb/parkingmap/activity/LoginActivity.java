package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.parkingmap.api.proto.user.LoginRequest;
import com.bht.parkingmap.api.proto.user.LoginResponseType;
import com.bht.parkingmap.api.proto.user.UserRole;
import com.vtb.parkingmap.Common;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingFragmentActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

@Getter
public class LoginActivity extends BaseSaigonParkingFragmentActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.editusername)
    EditText _username;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

//        _loginButton.setEnabled(false);

//        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
//                R.style.AppTheme_Dark_Dialog);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Authenticating...");
//        progressDialog.show();

        String username = _username.getText().toString();
        String password = _passwordText.getText().toString();


        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra("UserName", username);
        intent.putExtra("PassWord", password);
        startActivity(intent);
        // TODO: Implement your own authentication logic here.

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // On complete call either onLoginSuccess or onLoginFailed
//                        onLoginSuccess();
//                        // onLoginFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 500);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _username.getText().toString();
        String password = _passwordText.getText().toString();


        if (username.isEmpty()) {
            _username.setError("Pls input UserName");
            return false;

        }

        if (password.isEmpty()) {
            _passwordText.setError("Pls Input password");
            return false;
        }
        if (password.length() < 4 || password.length() > 10) {
            _passwordText.setError(" PassWord between 4 and 10 alphanumeric characters");
            return false;
        }

        LoginResponseType loginResponseType = Common.userServiceBlockingStub
                .validateLogin(LoginRequest.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .setUserRole(UserRole.CUSTOMER)
                        .build())
                .getResponse();

        Log.d("checkusernameandpassword", loginResponseType.toString());

        switch (loginResponseType) {
            case SUCCESS:
                valid = true;
                _username.setError(null);
                _passwordText.setError(null);
                break;
            case INCORRECT:
                valid = false;
                _passwordText.setError("Sai Mat Khau");

                break;
            case NON_EXIST:
                valid = false;
                _username.setError("Tai khoan khong ton tai");
                break;
            case INACTIVATED:
                valid = false;
                _username.setError("Tai khoan chua duoc kich hoat - can lien he tong dai ");
                break;
            default:
                break;
        }

        return valid;
    }
}
