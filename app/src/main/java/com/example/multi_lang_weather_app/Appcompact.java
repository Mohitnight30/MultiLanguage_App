package com.example.multi_lang_weather_app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Appcompact extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager languageManager= new LanguageManager(this);
        languageManager.updateResource(languageManager.getLang());
    }
}
