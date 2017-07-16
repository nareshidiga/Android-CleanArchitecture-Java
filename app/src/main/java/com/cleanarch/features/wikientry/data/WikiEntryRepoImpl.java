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
import com.cleanarch.features.wikientry.usecases.WikiEntryRepo;

import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

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

        return entries.flatMap(new Function<List<WikiEntryTable>, Flowable<WikiEntry>>() {
            @Override
            public Flowable<WikiEntry> apply(List<WikiEntryTable> wikiEntryTables) throws Exception {

                if (wikiEntryTables.size() > 0) {

                    WikiEntryTable firstEntry = wikiEntryTables.get(0);
                    Log.d(TAG, "Found and sending entry from local");

                    return Flowable.just(new WikiEntry(firstEntry.getPageId(),
                            firstEntry.getTitle(), firstEntry.getExtract()));
                }

                Log.d(TAG, "Returning flowable with invalid entry from local");
                return Flowable.empty();
                //return Flowable.just(new WikiEntry(-1, "", ""));
            }
        });

    }


    private Flowable<WikiEntry> fetchFromRemote(String title) {

        Log.d(TAG, "fetchFromRemote enter");

        Flowable<WikiEntryApiResponse> getRequest = wikiApiService.getWikiEntry(title);

        return getRequest.flatMap(new Function<WikiEntryApiResponse, Publisher<? extends WikiEntry>>() {
            @Override
            public Publisher<? extends WikiEntry> apply(WikiEntryApiResponse wikiEntryApiResponse) throws Exception {

                Log.d(TAG, "received response from remote");

                Iterator<WikiEntryApiResponse.Pageval> pagevalIterator = wikiEntryApiResponse.query.pages.values().iterator();
                WikiEntryApiResponse.Pageval pageVal = pagevalIterator.next();

                if (isValidResult(pageVal)) {
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

            }
        });
    }

    private boolean isValidResult(WikiEntryApiResponse.Pageval pageVal) {
        return pageVal.pageid == null || pageVal.pageid <= 0 ||
                pageVal.title == null || pageVal.title.length() < 1 ||
                pageVal.extract == null || pageVal.extract.length() < 1;
    }

}
