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

package com.cleanarch.features.wikientry.data.remote;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;


public class WikiEntryApiResponse {

    @SerializedName("batchcomplete")
    @Expose
    public String batchcomplete;

    @SerializedName("query")
    @Expose
    public Query query;


    public class Query {

        @SerializedName("pages")
        @Expose
        public HashMap<String, Pageval> pages;

    }


    public class Pageval {

        @SerializedName("pageid")
        @Expose
        public Integer pageid;
        @SerializedName("ns")
        @Expose
        public Integer ns;
        @SerializedName("title")
        @Expose
        public String title;
        @SerializedName("extract")
        @Expose
        public String extract;

    }

}


