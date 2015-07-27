/*
 * Copyright (C) 2015 Zemin Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zemin.autocomplete.widget.samples;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;

import zemin.autocomplete.widget.SpannedAutoCompleteView;
import zemin.autocomplete.widget.SpannedAutoCompleteView.SpanLayer;
import com.example.textdrawable.drawable.TextDrawable;

import java.util.ArrayList;

//
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "zemin.autocomplete.samples";
    private static final boolean DBG = true;

    private SpannedAutoCompleteView mSpannedView;
    private SpanLayer mIconLayer;
    private SpanLayer mTextLayer;

    private final String[] STRINGS = {
        "hahahaha",
        "hihihihi",
        "hehehehe",
        "hidfdadf",
        "hiadlfidi",
        "hejaidflk",
        "hadfak293r",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView msgView = (TextView) findViewById(R.id.msg);
        ArrayList<DropDownItem> list = new ArrayList<DropDownItem>();
        for (int i = 0, count = STRINGS.length; i < count; i++) {
            msgView.append("\n" + STRINGS[i]);
            list.add(new DropDownItem(R.drawable.android_gray, STRINGS[i]));
        }

        SearchAdapter adapter = new SearchAdapter(this, list);

        mSpannedView = (SpannedAutoCompleteView) findViewById(R.id.sacv);
        mSpannedView.setAdapter(adapter);
        mSpannedView.setAutoRemove(true);

        // background layer
        mSpannedView.setSpanBackground(R.drawable.span_background);

        // close view layer
        mSpannedView.createSpanLayer()
            .setDrawable(R.drawable.close_button)
            .setGravity(SpanLayer.RIGHT)
            .setMargin(10, 10, 10, 10);

        // icon view layer
        mIconLayer = mSpannedView.createSpanLayer()
            .setGravity(SpanLayer.LEFT)
            .setMargin(10, 10, 10, 10);

        // text view layer
        mTextLayer = mSpannedView.createSpanLayer()
            .setGravity(SpanLayer.CENTER);

        mSpannedView.setCallback(new SpannedAutoCompleteView.SimpleCallback() {

                @Override
                public void onSpanCreate(SpannedAutoCompleteView view, Object dropdownItem) {
                    DropDownItem item = (DropDownItem) dropdownItem;

                    TextDrawable td = new TextDrawable(MainActivity.this);
                    td.setText(item.text);
                    td.setTextSize(15);

                    mIconLayer.setDrawable(item.icon);
                    mTextLayer.setDrawable(td);
                }
            });
    }

    public class DropDownItem {
        public Drawable icon;
        public String text;

        public DropDownItem(int icon, String text) {
            this(getResources().getDrawable(icon), text);
        }

        public DropDownItem(Drawable icon, String text) {
            this.icon = icon; this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public class SearchAdapter extends ArrayAdapter<DropDownItem> {

        public SearchAdapter(Context context, ArrayList<DropDownItem> results) {
            super(context, 0, results);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DropDownItem item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.dropdown_item, null);
            }

            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            iconView.setImageDrawable(item.icon);

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(item.text);

            return convertView;
        }
    }
}
