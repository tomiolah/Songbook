package com.bence.songbook.ui.activity;

import static com.bence.songbook.ui.activity.LoadActivity.SERVER_IS_NOT_AVAILABLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.LanguageApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class LanguagesActivity extends AppCompatActivity {

    public static String syncAutomatically = "syncAutomatically";
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
        noInternetConnectionToast = Toast.makeText(getApplicationContext(), SERVER_IS_NOT_AVAILABLE, Toast.LENGTH_LONG);
        new Downloader().execute();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean syncAutomatically = sharedPreferences.getBoolean(LanguagesActivity.syncAutomatically, true);
        CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setChecked(syncAutomatically);
        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        sharedPreferences.edit().putBoolean(LanguagesActivity.syncAutomatically, isChecked).apply()
        );
    }

    private void displayListView() {
        dataAdapter = new MyCustomAdapter(this,
                R.layout.activity_language_checkbox_row, languages);
        ListView listView = findViewById(R.id.languageActivity_listView);
        listView.setAdapter(dataAdapter);
    }

    private void initializeDownloadButton() {
        Button myButton = findViewById(R.id.languageActivity_downloadButton);
        myButton.setOnClickListener(v -> {
            List<Language> languageList = dataAdapter.languageList;
            List<Language> languages = new ArrayList<>();
            LanguageRepository languageRepository = new LanguageRepositoryImpl(getApplicationContext());
            for (Language language : languageList) {
                languageRepository.save(language);
                language.setSelectedForDownload(language.isSelected());
                if (language.isSelected()) {
                    languages.add(language);
                }
            }
            Memory.getInstance().setPassingLanguages(languages);
            Intent intent = new Intent(languagesActivity, LoadActivity.class);
            startActivityForResult(intent, 1);
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

        private final List<Language> languageList;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<Language> languageList) {
            super(context, textViewResourceId, languageList);
            this.languageList = new ArrayList<>();
            this.languageList.addAll(languageList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
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

                holder.textView.setOnClickListener(view -> {
                    TextView textView = (TextView) view;
                    CheckBox checkBox = (CheckBox) textView.getTag();
                    checkBox.setChecked(!checkBox.isChecked());
                    setSelection(checkBox);
                });
                holder.checkBox.setOnClickListener(view -> setSelection((CheckBox) view));
                RelativeLayout relativelayout = convertView.findViewById(R.id.layout);
                relativelayout.setOnClickListener(view -> {
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                    setSelection(holder.checkBox);
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Language language = languageList.get(position);
            holder.textView.setText(" - " + language.getNativeName() + getSizeString(language));
            holder.checkBox.setText(language.getEnglishName());
            holder.checkBox.setChecked(language.isSelected());
            holder.textView.setTag(holder.checkBox);
            holder.checkBox.setTag(language);

            return convertView;
        }

        private String getSizeString(Language language) {
            Long size = language.getSize();
            if (size == null || size < 0) {
                return "";
            }
            long almostEqualSize = getAlmostEqualSize(size);
            return " (~" + almostEqualSize + ")";
        }

        private long getAlmostEqualSize(long size) {
            if (size < 10) {
                return size;
            }
            int countDigits = getCountDigits(size);
            int resolution;
            if (countDigits < 3) {
                resolution = countDigits - 2;
            } else if (countDigits < 4) {
                resolution = countDigits - 1;
            } else {
                resolution = countDigits - 2;
            }
            return getResolutionNumber(size, resolution);
        }

        private long getResolutionNumber(long size, int resolution) {
            long x = (long) Math.pow(10, resolution);
            long a = size / x;
            return a * x;
        }

        private int getCountDigits(long size) {
            int countDigits = 0;
            while (size > 0) {
                ++countDigits;
                size /= 10;
            }
            return countDigits;
        }

        private void setSelection(CheckBox checkBox) {
            Language language = (Language) checkBox.getTag();
            language.setSelected(checkBox.isChecked());
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
                            language.setSize(onlineLanguage.getSize());
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
