/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.andrew.apollo.MusicPlaybackService;
import com.andrew.apollo.utils.MusicUtils;
import com.frostwire.android.R;

/**
 * A custom {@link ImageButton} that represents the "repeat" button.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class RepeatButton extends ImageButton implements OnClickListener {

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // Control playback (cycle repeat modes)
        setOnClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }

    /**
     * Sets the correct drawable for the repeat state.
     */
    public void updateRepeatState() {
        switch (MusicUtils.getRepeatMode()) {
        case MusicPlaybackService.REPEAT_ALL:
            setContentDescription(getResources().getString(R.string.accessibility_repeat_all));
            setImageResource(R.drawable.btn_playback_repeat_all);
            break;
        case MusicPlaybackService.REPEAT_CURRENT:
            setContentDescription(getResources().getString(R.string.accessibility_repeat_one));
            setImageResource(R.drawable.btn_playback_repeat_one);
            break;
        case MusicPlaybackService.REPEAT_NONE:
            setContentDescription(getResources().getString(R.string.accessibility_repeat));
            setImageResource(R.drawable.btn_playback_repeat);
            break;
        default:
            break;
        }
    }
}
