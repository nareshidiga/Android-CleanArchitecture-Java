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

package com.cleanarch.features.wikientry.data;

import android.util.Log;

import com.cleanarch.app.AppDatabase;
import com.cleanarch.features.wikientry.data.local.WikiEntryDao;
import com.cleanarch.features.wikientry.data.local.WikiEntryTable;
import com.cleanarch.features.wikientry.data.remote.WikiApiService;
import com.cleanarch.features.wikientry.data.remote.WikiEntryApiResponse;
import com.cleanarch.features.wikientry.entities.WikiEntry;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Flowable;

@Singleton
public class WikiEntryRepoImpl implements WikiEntryRepo {

    private static final String TAG = WikiEntryRepoImpl.class.getSimpleName();
    private AppDatabase appDatabase;
    private WikiApiService wikiApiService;

    @Inject
    public WikiEntryRepoImpl(AppDatabase appDatabase, WikiApiService wikiApiService) {
        this.appDatabase = appDatabase;
        this.wikiApiService = wikiApiService;
    }

    @Override
    public Flowable<WikiEntry> getWikiEntry(String title) {

        //define your single source of truth : is it cache? database? or cloud?

        Flowable<WikiEntry> local = fetchFromLocal(title);
        Flowable<WikiEntry> remote = fetchFromRemote(title);

        return Flowable.merge(local, remote).firstElement().toFlowable();

    }


    private Flowable<WikiEntry> fetchFromLocal(String title) {


        Flowable<List<WikiEntryTable>> entries = appDatabase.wikiEntryDao().getByTitle(title);

        return entries.flatMap(wikiEntryTables -> {

            if (!wikiEntryTables.isEmpty()) {

                WikiEntryTable firstEntry = wikiEntryTables.get(0);
                Log.d(TAG, "Found and sending entry from local");

                return Flowable.just(new WikiEntry(firstEntry.getPageId(),
                        firstEntry.getTitle(), firstEntry.getExtract()));
            }

            Log.d(TAG, "Returning flowable with invalid entry from local");
            return Flowable.empty();
        });

    }


    private Flowable<WikiEntry> fetchFromRemote(String title) {

        Log.d(TAG, "fetchFromRemote enter");

        Flowable<WikiEntryApiResponse> getRequest = wikiApiService.getWikiEntry(title);

        return getRequest.flatMap(wikiEntryApiResponse -> {

            Log.d(TAG, "received response from remote");

            Iterator<WikiEntryApiResponse.Pageval> pageValIterator = wikiEntryApiResponse.query.pages.values().iterator();
            WikiEntryApiResponse.Pageval pageVal = pageValIterator.next();

            if (invalidResult(pageVal)) {
                Log.d(TAG, "Sending error from remote");
                return Flowable.error(new NoResultFound());

            } else {

                WikiEntry wikiEntry = new WikiEntry(pageVal.pageid, pageVal.title, pageVal.extract);
                appDatabase.beginTransaction();
                try {
                    WikiEntryTable newEntry = new WikiEntryTable();
                    newEntry.setPageId(wikiEntry.getPageid());
                    newEntry.setTitle(wikiEntry.getTitle());
                    newEntry.setExtract(wikiEntry.getExtract());

                    WikiEntryDao entryDao = appDatabase.wikiEntryDao();
                    entryDao.insert(newEntry);
                    appDatabase.setTransactionSuccessful();
                } finally {
                    appDatabase.endTransaction();
                }
                Log.d(TAG, "added new entry into app database table");

                Log.d(TAG, "Sending entry from remote");
                return Flowable.just(wikiEntry);
            }

        });
    }

    private boolean invalidResult(WikiEntryApiResponse.Pageval pageVal) {
        return pageVal.pageid == null || pageVal.pageid <= 0 ||
                pageVal.title == null || pageVal.title.isEmpty() ||
                pageVal.extract == null || pageVal.extract.isEmpty();
    }

}
