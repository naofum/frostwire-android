/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014,, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.search.appia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

import com.frostwire.android.gui.SearchEngine;
import com.frostwire.android.gui.util.OfferUtils;
import com.frostwire.android.util.StringUtils;
import com.frostwire.logging.Logger;
import com.frostwire.search.PagedWebSearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.frostclick.UserAgent;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class AppiaSearchPerformer extends PagedWebSearchPerformer {
    
    private final AppiaSearchThrottle throttle;
    
    public static final String HTTP_SERVER_NAME = "192.168.1.16";

    private static final Logger LOG = Logger.getLogger(AppiaSearchPerformer.class);

    private static final int MAX_RESULTS = 1;

    private final Map<String, String> customHeaders;
    

    public AppiaSearchPerformer(DomainAliasManager domainAliasManager, long token, String keywords, int timeout, UserAgent userAgent, String androidId, AppiaSearchThrottle throttle) {
        super(domainAliasManager ,token, keywords, timeout, MAX_RESULTS);
        this.customHeaders = buildCustomHeaders(userAgent, androidId);
        this.throttle = throttle;
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        //return "http://api.frostclick.com/appia";
        return "http://" + HTTP_SERVER_NAME + ":8080/frostclick-search-api/appia";
    }

    @Override
    protected List<? extends SearchResult> searchPage(int page) {
        List<? extends SearchResult> result = Collections.emptyList();

        if (OfferUtils.isfreeAppsEnabled() && throttle.canSearchAgain()) {
            String url = getUrl(-1, getEncodedKeywords());
            String text = null;
            
            try {
                text = fetch(url, null, customHeaders);
            } catch (IOException e) {
                checkAccesibleDomains();
                return result;
            }
            
            if (text != null) {
                result = searchPage(text);
            } else {
                LOG.warn("Page content empty for url: " + url);
            }
        }        
        return result;
    }

    @Override
    protected List<? extends SearchResult> searchPage(String page) {
        List<AppiaSearchResult> results = new ArrayList<AppiaSearchResult>();
        AppiaServletResponse appiaServletResponse = JsonUtils.toObject(page, AppiaServletResponse.class);
        List<AppiaServletResponseItem> responseItems = appiaServletResponse.results;
        for (AppiaServletResponseItem item : responseItems) {
            AppiaSearchResult sr = new AppiaSearchResult(item);
            results.add(sr);
            AppiaSearchPerformer.asyncHttpGet(sr.getImpressionTrackingURL());
        }
        
        if (results.isEmpty()) {
            results = Collections.emptyList();
        }
        
        return results;
    }

    private Map<String, String> buildCustomHeaders(UserAgent userAgent, String androidId) {
        Map<String, String> map = new HashMap<String, String>();
        map.putAll(userAgent.getHeadersMap());
        map.put("User-Agent", userAgent.toString());
        map.put("sessionId", userAgent.getUUID());
        map.put("androidId", androidId);
        return map;
    }
    
    private static void asyncHttpGet(String url) {
        if (!StringUtils.isNullOrEmpty(url)) {
            AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... url) {
                    try {
                        String output = HttpClientFactory.newInstance().get(url[0], 2000, SearchEngine.FROSTWIRE_ANDROID_USER_AGENT.toString());
                        if (output != null) {
                            System.out.println("Pixel tracked at " + url[0]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.execute(url);
        }
    }
    
    public final static class AppiaSearchThrottle {
        private final int MAX_SEARCHES_WITHIN_TIME_INTERVAL = 10;
        private final int TIME_INTERVAL = 1 * 60 * 1000;
        private int searchAttempts;
        private long lastTimeSearchPerformed;
        
        public AppiaSearchThrottle() {
            searchAttempts = 0;
            lastTimeSearchPerformed = -1;
        }
        
        public boolean canSearchAgain() {
            searchAttempts++;
            long timeSince = System.currentTimeMillis() - lastTimeSearchPerformed;
            boolean enoughTimePassed = timeSince >= TIME_INTERVAL;
            boolean enoughSearches = searchAttempts % MAX_SEARCHES_WITHIN_TIME_INTERVAL == 0;
            boolean canSearchAgain = enoughTimePassed || enoughSearches;
            if (canSearchAgain) {
                lastTimeSearchPerformed = System.currentTimeMillis();
                searchAttempts = 1;
            }
            return canSearchAgain;
        }
    }
}