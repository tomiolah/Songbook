package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bence.songbook.R;
import com.bence.songbook.api.LanguageApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class LanguagesActivity extends AppCompatActivity {

    MyCustomAdapter dataAdapter = null;
    private List<Language> languages;
    private LanguagesActivity languagesActivity;
    private Toast noInternetConnectionToast;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);
        languagesActivity = this;
        LanguageRepositoryImpl languageRepository = new LanguageRepositoryImpl(getApplicationContext());
        languages = languageRepository.findAll();
        noInternetConnectionToast = Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG);
        new Downloader().execute();
    }

    private void displayListView() {
        dataAdapter = new MyCustomAdapter(this,
                R.layout.activity_language_checkbox_row, languages);
        ListView listView = findViewById(R.id.languageActivity_listView);
        listView.setAdapter(dataAdapter);
    }

    private void initializeDownloadButton() {
        Button myButton = findViewById(R.id.languageActivity_downloadButton);
        myButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Language> languageList = dataAdapter.languageList;
                LanguageRepository languageRepository = new LanguageRepositoryImpl(getApplicationContext());
                for (Language language : languageList) {
                    if (language.isSelected()) {
                        languageRepository.save(language);
                    } else {
                        if (language.getId() != null) {
                            Language one = languageRepository.findOne(language.getId());
                            if (one != null) {
                                languageRepository.save(language);
                            }
                        }
                    }
                }
                Intent intent = new Intent(languagesActivity, LoadActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode >= 1) {
            setResult(resultCode);
            finish();
        }
    }

    private class MyCustomAdapter extends ArrayAdapter<Language> {

        private List<Language> languageList;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<Language> languageList) {
            super(context, textViewResourceId, languageList);
            this.languageList = new ArrayList<>();
            this.languageList.addAll(languageList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.activity_language_checkbox_row, null);

                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.code);
                holder.checkBox = convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CheckBox checkBox = (CheckBox) view;
                        Language language = (Language) checkBox.getTag();
                        language.setSelected(checkBox.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Language language = languageList.get(position);
            holder.textView.setText(" (" + language.getNativeName() + ")");
            holder.checkBox.setText(language.getEnglishName());
            holder.checkBox.setChecked(language.isSelected());
            holder.checkBox.setTag(language);

            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            LanguageApiBean languageApi = new LanguageApiBean();
            List<Language> onlineLanguages = languageApi.getLanguages();
            if (onlineLanguages != null) {
                List<Language> newLanguages = new ArrayList<>();
                for (Language onlineLanguage : onlineLanguages) {
                    boolean was = false;
                    for (Language language : languages) {
                        if (language.getUuid().equals(onlineLanguage.getUuid())) {
                            was = true;
                            break;
                        }
                    }
                    if (!was) {
                        newLanguages.add(onlineLanguage);
                    }
                }
                languages.addAll(newLanguages);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (languages.size() == 0) {
                noInternetConnectionToast.show();
            } else {
                displayListView();
                initializeDownloadButton();
            }
        }
    }
}
