package com.example.bumobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.bumobile.services.FragmentSetClass;
import com.example.bumobile.services.UserAutoAuth;
import com.example.bumobile.R;
import com.example.bumobile.models.Auth;
import com.example.bumobile.models.Token;
import com.example.bumobile.services.RequestBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProfile extends Fragment {
    public EditText username;
    private EditText password;
    private Button loginButton;
    private TextView signup;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setViews(view);
        setListeners();
        return view;
    }

    /**
     * Инициализация элементов интерфейса
     * @param view
     */
    public void setViews(View view) {
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.loginButton);
        signup = view.findViewById(R.id.signupText);
    }

    /**
     * установка обработчиков нажатия
     */
    public void setListeners() {
        loginButton.setOnClickListener(v -> signIn());
        signup.setOnClickListener(v -> openSignUpFragment());
    }

    /**
     * Авторизация пользователя
     */
    private void signIn() {
        Auth auth = new Auth();
        auth.setUserName(username.getText().toString());
        auth.setPassword(password.getText().toString());
        RequestBuilder.getApiClient().singIn(auth).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    onSignInSuccess(response.body());
                } else {
                    showSignInError();
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * Обработка успешной авторизации
     * @param token
     */
    public void onSignInSuccess(String token) {
        UserAutoAuth userAutoAuth = new UserAutoAuth();
        userAutoAuth.setToFile(getActivity(), username.getText().toString(), password.getText().toString());
        Token.setToken(token);
        FragmentUserProfile userProfileFragment = new FragmentUserProfile();
        FragmentSetClass.openFragment(userProfileFragment, getFragmentManager().beginTransaction());
    }

    /**
     * отображение ошибки
     */
    public void showSignInError() {
        username.setError("Такого пользователя не существует\nВведен неверный логин или пароль");
    }

    public void openSignUpFragment() {
        FragmentSignUp signUp = new FragmentSignUp();
        FragmentSetClass.openFragment(signUp, getFragmentManager().beginTransaction());
    }
}