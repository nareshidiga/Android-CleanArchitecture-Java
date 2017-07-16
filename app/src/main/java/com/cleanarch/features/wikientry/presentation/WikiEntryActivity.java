/*
 * Copyright 2017  Naresh Gowd Idiga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cleanarch.features.wikientry.presentation;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cleanarch.features.R;
import com.cleanarch.app.CleanArchApp;
import com.cleanarch.features.wikientry.WikiEntryComponent;

public class WikiEntryActivity extends LifecycleActivity {

    private static final String TAG = WikiEntryActivity.class.getSimpleName();

    private EditText title;
    private ContentLoadingProgressBar progressBar;
    private WikiEntryViewModel wikiEntryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //WikiEntry feature component scope start here
        ((CleanArchApp)getApplication()).buildWikiEntryComponent();

        setContentView(R.layout.activity_main);

        title = (EditText) findViewById(R.id.entryTitle);
        TextView extract = (TextView) findViewById(R.id.entryDetails);
        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(submitButtonOnClickListener);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);

        wikiEntryViewModel = ViewModelProviders.of(this).get(WikiEntryViewModel.class);
        wikiEntryViewModel.getWikiEntry().observe(this, wikiEntry -> {

            Log.d( TAG,"received update for wikiEntry");

            extract.setText(wikiEntry.getExtract());
            progressBar.hide();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //WikiEntry feature compoonent scope ends here
        ((CleanArchApp)getApplication()).buildWikiEntryComponent();
    }

    private View.OnClickListener submitButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.show();
            InputMethodManager imm = (InputMethodManager) WikiEntryActivity.this.getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            wikiEntryViewModel.loadWikiEntry(title.getText().toString());
        }
    };
}
