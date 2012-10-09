/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.android.gui.views;

import java.util.List;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.frostwire.android.R;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.HttpFetcher;
import com.frostwire.android.gui.PromotionsHandler;
import com.frostwire.android.gui.adapters.PromotionsAdapter;
import com.frostwire.android.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class Promotions2View extends LinearLayout {

    private static final String TAG = "FW.PromotionsView";

    public Promotions2View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_promotions, this);

        if (isInEditMode()) {
            return;
        }

        try {
            GridView gridview = (GridView) findViewById(R.id.view_promotions_grid);
            gridview.setAdapter(new PromotionsAdapter(getContext(), loadSlides()));

            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "Error loading slides", e);
        }
    }

    private List<PromotionsHandler.Slide> loadSlides() {
        byte[] jsonBytes = new HttpFetcher(buildUrl()).fetch();
        PromotionsHandler.SlideList slides = JsonUtils.toObject(new String(jsonBytes), PromotionsHandler.SlideList.class);
        return slides.slides;
    }

    private String buildUrl() {
        return String.format("%s?from=android&fw=%s&sdk=%s", Constants.SERVER_PROMOTIONS_URL, Constants.FROSTWIRE_VERSION_STRING, Build.VERSION.SDK_INT);
    }
}