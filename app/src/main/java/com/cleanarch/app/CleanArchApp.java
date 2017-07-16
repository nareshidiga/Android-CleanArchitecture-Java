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

package com.cleanarch.app;

import android.app.Application;
import android.util.Log;

import com.cleanarch.features.wikientry.DaggerWikiEntryComponent;
import com.cleanarch.features.wikientry.WikiEntryComponent;
import com.cleanarch.features.wikientry.WikiEntryModule;

public class CleanArchApp extends Application {

    private static final String TAG = CleanArchApp.class.getSimpleName();

    private AppComponent appComponent;
    private WikiEntryComponent wikiEntryComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = buildAppComponent();
        Log.d(TAG, "onApplicationCreate");
    }


    private AppComponent buildAppComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }



    /********Support for Feature Component ****************/

    public WikiEntryComponent buildWikiEntryComponent() {
        wikiEntryComponent = DaggerWikiEntryComponent.builder()
                .appComponent(appComponent)
                .wikiEntryModule(new WikiEntryModule())
                .build();
        return wikiEntryComponent;
    }

    public void releaseWikiEntryComponent() {
        wikiEntryComponent = null;
    }

    public WikiEntryComponent getWikiEntryComponent() {
        return wikiEntryComponent;
    }

    /**********************************************************/



}
