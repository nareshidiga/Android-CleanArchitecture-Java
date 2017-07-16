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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.cleanarch.app.CleanArchApp;
import com.cleanarch.features.R;
import com.cleanarch.features.wikientry.entities.WikiEntry;
import com.cleanarch.features.wikientry.usecases.GetWikiEntryUseCase;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class WikiEntryViewModel extends AndroidViewModel {

    private MutableLiveData<WikiEntry> wikiEntry;
    private final static String TAG = WikiEntryViewModel.class.getSimpleName();

    @Inject
    Lazy<GetWikiEntryUseCase> getWikiEntryUseCase;

    public WikiEntryViewModel(Application application) {
        super(application);
        ((CleanArchApp) application).getWikiEntryComponent().inject(this);
    }

    LiveData<WikiEntry> getWikiEntry() {
        if (wikiEntry == null) {
            wikiEntry = new MutableLiveData<>();
        }
        return wikiEntry;
    }

    void loadWikiEntry(String title) {

        getWikiEntryUseCase.get().execute(
                new GetWikiEntryUseCase.Input(title, AndroidSchedulers.mainThread()),
                new UseCaseSubscriber());

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        //remove subscriptions if any
        getWikiEntryUseCase.get().cancel();
        Log.d(TAG, "onCleared");
    }

    private class UseCaseSubscriber extends DisposableSubscriber<WikiEntry> {

        @Override
        public void onNext(WikiEntry wikiEntry) {

            Log.d(TAG, "Received response for wikiEntry");
            WikiEntryViewModel.this.wikiEntry.setValue(wikiEntry);
        }

        @Override
        public void onError(Throwable e) {

            Log.d(TAG, "Received error: " + e.toString());
            WikiEntryViewModel.this.wikiEntry.setValue( new WikiEntry(-1, "",
                    getApplication().getString(R.string.no_results_found)));
        }

        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete called");
        }
    }
}
