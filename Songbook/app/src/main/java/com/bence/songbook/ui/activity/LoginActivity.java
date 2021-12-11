package com.bence.songbook.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bence.projector.common.dto.LoginDTO;
import com.bence.projector.common.dto.UserDTO;
import com.bence.songbook.R;
import com.bence.songbook.api.LoginApiBean;
import com.bence.songbook.api.UserApiBean;
import com.bence.songbook.ui.utils.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.fabSubmit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        final LoginDTO loginDTO = getLoginDTO();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LoginApiBean loginApiBean = new LoginApiBean();
                if (!loginApiBean.login(loginDTO)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, R.string.try_again_later, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                UserApiBean userApiBean = new UserApiBean();
                UserDTO loggedInUser = userApiBean.getLoggedInUser();
                if (loggedInUser != null) {
                    System.out.println("logged in " + loggedInUser.getEmail());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
        thread.start();
    }

    private LoginDTO getLoginDTO() {
        LoginDTO loginDTO = new LoginDTO();
        EditText editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        loginDTO.setUsername(getTrimText(editTextTextEmailAddress));
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        String password = getStringFromEditText(editTextPassword);
        loginDTO.setPassword(password);
        return loginDTO;
    }

    private String getTrimText(EditText editTextTextEmailAddress) {
        return getStringFromEditText(editTextTextEmailAddress).trim();
    }

    private String getStringFromEditText(EditText editTextTextEmailAddress) {
        return editTextTextEmailAddress.getText().toString();
    }
}
