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

package com.cleanarch.features.wikientry.usecases;

import android.util.Log;

import com.cleanarch.base.usecases.UseCase;
import com.cleanarch.features.wikientry.entities.WikiEntry;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;


public class GetWikiEntryUseCase extends UseCase<GetWikiEntryUseCase.Input, WikiEntry> {

    private static final String TAG = GetWikiEntryUseCase.class.getSimpleName();
    private WikiEntryRepo repo;

    @Inject
    GetWikiEntryUseCase(WikiEntryRepo repo) {
        this.repo = repo;
    }

    @Override
    public void execute(Input input, DisposableSubscriber<WikiEntry> subscriber) {

        Flowable.just(input.title)
                .flatMap(title -> repo.getWikiEntry(title))
                .subscribeOn(Schedulers.newThread())
                .observeOn(input.observerOnScheduler)
                .subscribe(subscriber);

        Log.d(TAG, "called subscribe on getWikiEntry flowable");

        disposables.add(subscriber);
    }

    public static class Input {

        private String title;
        private Scheduler observerOnScheduler;

        public Input(String title, Scheduler observerOnScheduler) {
            this.title = title;
            this.observerOnScheduler = observerOnScheduler;
        }
    }

}
