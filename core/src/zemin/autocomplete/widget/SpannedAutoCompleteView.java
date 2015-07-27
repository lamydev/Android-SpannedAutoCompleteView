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

package zemin.autocomplete.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpanWatcher;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.QwertyKeyListener;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class converts the auto-completion text into a image span constructed by
 * a set of span layers.
 *
 * @see #SpanLayer
 */
public class SpannedAutoCompleteView extends MultiAutoCompleteTextView {

    private static final String TAG = "zemin.SpannedAutoCompleteView";
    public static boolean DBG = true;

    private final List<SpanLayer> mSpanLayers = new ArrayList<SpanLayer>();
    private final List<SpanInfo> mSpans = new ArrayList<SpanInfo>();

    private Drawable mSpanBackground;
    private Callback mCallback;
    private DefaultTokenizer mTokenizer;
    private SpanWatcherImpl mSpanWatcher;
    private SpanInfo mLastSpan;
    private boolean mAutoRemove;
    private CharSequence mSeparator;

    public interface Callback {

        /**
         * Called when the user clicks on a dropdownItem, and a span associated
         * with that dropdownItem is being created.
         */
        void onSpanCreate(SpannedAutoCompleteView view, Object dropdownItem);

        /**
         * Called when a span is added.
         */
        void onSpanAdded(SpannedAutoCompleteView view, Object dropdownItem);

        /**
         * Called when a span is removed.
         */
        void onSpanRemoved(SpannedAutoCompleteView view, Object dropdownItem);

        /**
         * Called when the user clicks on a span.
         */
        void onSpanClick(SpannedAutoCompleteView view, Object dropdownItem);
    }

    public static class SimpleCallback implements Callback {
        public void onSpanCreate(SpannedAutoCompleteView view, Object dropdownItem) {}
        public void onSpanClick(SpannedAutoCompleteView view, Object dropdownItem) {}
        public void onSpanAdded(SpannedAutoCompleteView view, Object dropdownItem) {}
        public void onSpanRemoved(SpannedAutoCompleteView view, Object dropdownItem) {}
    }

    public static class SpanLayer {

        /*
         * ************ Gravity ***************
         *    ____________________________    *
         *   |                            |   *
         *   |            Top             |   *
         *   |       --------------       |   *
         *   |       |            |       |   *
         *   |  Left |   Center   | Right |   *
         *   |       |            |       |   *
         *   |       --------------       |   *
         *   |           Bottom           |   *
         *   |____________________________|   *
         *                                    *
         * ************************************
         */

        public static final int CENTER         = 0;
        public static final int LEFT           = 1;
        public static final int TOP            = 2;
        public static final int RIGHT          = 3;
        public static final int BOTTOM         = 4;

        private Context mContext;

        public int gravity;
        public int width;
        public int height;
        public int leftMargin;
        public int topMargin;
        public int rightMargin;
        public int bottomMargin;
        Drawable drawable;
        int idx;

        public SpanLayer(Context context) {
            mContext = context;
        }

        public SpanLayer setDrawable(int resId) {
            return setDrawable(mContext.getResources().getDrawable(resId));
        }

        public SpanLayer setDrawable(Drawable drawable) {
            this.drawable = drawable;
            this.width = drawable.getIntrinsicWidth();
            this.height = drawable.getIntrinsicHeight();
            return this;
        }

        public SpanLayer setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public SpanLayer setMargin(int l, int t, int r, int b) {
            this.leftMargin = l;
            this.topMargin = t;
            this.rightMargin = r;
            this.bottomMargin = b;
            return this;
        }
    }

    private class SpanInfo {
        Object dropdownItem;
        ImageSpan image;
        ClickableSpan clickable;
        int sepLength;

        SpanInfo(Object dropdownItem) { this.dropdownItem = dropdownItem; }
    }

    public SpannedAutoCompleteView(Context context) {
        this(context, null);
    }

    public SpannedAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setMovementMethod(LinkMovementMethod.getInstance());
        setTokenizer(new DefaultTokenizer());
        setSeparator(' ');
        mSpanWatcher = new SpanWatcherImpl();
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    /**
     * Automatically remove span when it gets clicked.
     */
    public void setAutoRemove(boolean auto) {
        mAutoRemove = auto;
    }

    /**
     * Set span separator.
     */
    public void setSeparator(char separator) {
        setSeparator(String.valueOf(separator));
    }

    public void setSeparator(CharSequence separator) {
        mSeparator = separator;
    }

    /**
     * Span background will be placed at the bottom of the span layer stack.
     * If no span layers exist, calling this method will have no effect.
     */
    public void setSpanBackground(int resId) {
        setSpanBackground(getResources().getDrawable(resId));
    }

    public void setSpanBackground(Drawable drawable) {
        mSpanBackground = drawable;
    }

    /**
     * Create a new span layer.
     */
    public SpanLayer createSpanLayer() {
        SpanLayer layer = new SpanLayer(getContext());
        mSpanLayers.add(layer);
        return layer;
    }

    /**
     * Destroy a span layer.
     */
    public void destroySpanLayer(SpanLayer layer) {
        if (mSpanLayers.contains(layer)) {
            mSpanLayers.remove(layer);
        }
    }

    /**
     * Remove the specific span associated with the dropdownItem.
     */
    public void removeSpan(Object dropdownItem) {
        SpanInfo span = null;
        for (SpanInfo s : mSpans) {
            if (s.dropdownItem == dropdownItem) {
                span = s; break;
            }
        }
        if (span != null) {
            removeSpan(span);
        }
    }

    @Override
    public void setTokenizer(Tokenizer tokenizer) {
        if (!(tokenizer instanceof DefaultTokenizer)) {
            throw new IllegalArgumentException("unsupported tokenizer.");
        }
        mTokenizer = (DefaultTokenizer) tokenizer;
        super.setTokenizer(tokenizer);
    }

    @Override
    protected void replaceText(CharSequence text) {
        clearComposingText();

        final int end = getSelectionEnd();
        final int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        replaceTextWithSpannable(mTokenizer.terminateToken(text), start, end);
        editable.append(mSeparator);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        if (isPerformingCompletion()) {
            addSpanInfo(new SpanInfo(selectedItem));
            if (mCallback != null) {
                mCallback.onSpanCreate(this, selectedItem);
            }
        }
        return super.convertSelectionToString(selectedItem);
    }

    private void replaceTextWithSpannable(CharSequence text, int start, int end) {

        final ArrayList<SpanLayer> spanLayers = new ArrayList<SpanLayer>(mSpanLayers);
        final Iterator<SpanLayer> iter = spanLayers.iterator();
        while (iter.hasNext()) {
            SpanLayer layer = iter.next();
            if (layer.drawable == null) {
                iter.remove();
            }
        }

        int count = spanLayers.size();
        if (count == 0) {
            // no spans. fallback to the default.
            Editable editable = getText();
            editable.replace(start, end, text);
            mSpans.remove(mLastSpan);
            return;
        }

        final boolean hasSpanBackground = mSpanBackground != null;
        if (hasSpanBackground) {
            count++;
        }

        final Drawable[] drawables = new Drawable[count];
        LayerDrawable layerDrawable;

        if (hasSpanBackground) {
            // always the bottom layer
            drawables[0] = cloneDrawable(mSpanBackground);
        }

        int idx = hasSpanBackground ? 1 : 0;
        for (SpanLayer layer : spanLayers) {
            layer.idx = idx++;
            drawables[layer.idx] = cloneDrawable(layer.drawable);
        }
        layerDrawable = new LayerDrawable(drawables);

        int w, h, lw, lh, rw, rh, tw, th, bw, bh, cw, ch;
        lw = lh = rw = rh = tw = th = bw = bh = cw = ch = 0;

        for (SpanLayer layer : spanLayers) {
            w = layer.width + layer.leftMargin + layer.rightMargin;
            h = layer.height + layer.topMargin + layer.bottomMargin;

            switch (layer.gravity) {
            case SpanLayer.LEFT:
                lw = Math.max(lw, w);
                lh = Math.max(lh, h);
                break;

            case SpanLayer.RIGHT:
                rw = Math.max(rw, w);
                rh = Math.max(rh, h);
                break;

            case SpanLayer.TOP:
                tw = Math.max(tw, w);
                th = Math.max(th, h);
                break;

            case SpanLayer.BOTTOM:
                bw = Math.max(bw, w);
                bh = Math.max(bh, h);
                break;

            case SpanLayer.CENTER:
                cw = Math.max(cw, w);
                ch = Math.max(ch, h);
                break;
            }
        }

        w = lw + rw + cw;
        w = Math.max(w, tw);
        w = Math.max(w, bw);

        h = th + bh + ch;
        h = Math.max(h, lh);
        h = Math.max(h, rh);

        int l, t, r, b, o;
        l = t = r = b = 0;

        for (SpanLayer layer : spanLayers) {
            switch (layer.gravity) {
            case SpanLayer.LEFT:
                l = layer.leftMargin;
                r = w - l - layer.width;
                t = b = (h - layer.height) / 2;
                break;

            case SpanLayer.RIGHT:
                r = layer.rightMargin;
                l = w - r - layer.width;
                t = b = (h - layer.height) / 2;
                break;

            case SpanLayer.TOP:
                t = layer.topMargin;
                b = h - t - layer.height;
                l = r = (w - layer.width) / 2;
                break;

            case SpanLayer.BOTTOM:
                b = layer.bottomMargin;
                t = h - b - layer.height;
                l = r = (w - layer.width) / 2;
                break;

            case SpanLayer.CENTER:
                o = (w - lw - rw - cw) / 2;
                l = lw + o;
                r = rw + o;
                o = (h - th - bh - ch) / 2;
                t = th + o;
                b = bh + o;
                break;
            }

            layerDrawable.setLayerInset(layer.idx, l, t, r, b);
        }

        layerDrawable.setBounds(0, 0, w, h);

        Editable editable = getText();
        SpanInfo spanInfo = mLastSpan;
        spanInfo.image = new ImageSpan(layerDrawable, ImageSpan.ALIGN_BOTTOM);;
        spanInfo.clickable = new OnSpanClickListener(spanInfo);
        mLastSpan = null;

        editable.replace(start, end, text);
        updateSpanWatcher();
        addSpan(spanInfo, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private class OnSpanClickListener extends ClickableSpan {
        private SpanInfo mSpanInfo;

        OnSpanClickListener(SpanInfo span) {
            mSpanInfo = span;
        }

        @Override
        public void onClick(View widget) {
            if (mCallback != null) {
                mCallback.onSpanClick(SpannedAutoCompleteView.this, mSpanInfo.dropdownItem);
            }

            if (mAutoRemove) {
                removeSpan(mSpanInfo);
            }
        }
    }

    private class SpanWatcherImpl implements SpanWatcher {

        @Override
        public void onSpanAdded(Spannable text, Object span, int start, int end) {
            if (span instanceof OnSpanClickListener) {
                SpanInfo s = ((OnSpanClickListener) span).mSpanInfo;
                s.sepLength = mSeparator != null ? mSeparator.length() : 0;
                mTokenizer.setStart(end + s.sepLength);
                if (mCallback != null) {
                    mCallback.onSpanAdded(SpannedAutoCompleteView.this, s.dropdownItem);
                }
            }
        }

        @Override
        public void onSpanRemoved(Spannable text, Object span, int start, int end) {
            if (span instanceof OnSpanClickListener) {
                SpanInfo s = ((OnSpanClickListener) span).mSpanInfo;
                removeSpanInfo(s);
                if (mCallback != null) {
                    mCallback.onSpanRemoved(SpannedAutoCompleteView.this, s.dropdownItem);
                }
            }
        }

        @Override
        public void onSpanChanged(Spannable text, Object span, int previousStart,
                                  int previousEnd, int newStart, int newEnd) {
        }
    }

    private Drawable cloneDrawable(Drawable drawable) {
        Drawable mutated = drawable.mutate();
        Drawable.ConstantState state = mutated.getConstantState();
        return state != null ? state.newDrawable() : mutated;
    }

    private void updateSpanWatcher() {
        Spannable spannable = getText();
        final SpanWatcherImpl[] watchers =
            spannable.getSpans(0, spannable.length(), SpanWatcherImpl.class);
        for (int i = 0, count = watchers.length; i < count; i++) {
            spannable.removeSpan(watchers[i]);
        }
        spannable.setSpan(mSpanWatcher, 0, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    private void addSpan(SpanInfo span, int start, int end, int flag) {
        Spannable spannable = getText();
        spannable.setSpan(span.image, start, end, flag);
        spannable.setSpan(span.clickable, start, end, flag);
    }

    private void removeSpan(SpanInfo span) {
        Editable editable = getText();
        int start = editable.getSpanStart(span.image);
        int end = editable.getSpanEnd(span.image);

        editable.removeSpan(span.image);
        editable.removeSpan(span.clickable);
        editable.delete(start, end + span.sepLength);
        removeSpanInfo(span);
        setSelection(editable.length());
    }

    private void addSpanInfo(SpanInfo span) {
        mSpans.add(span);
        mLastSpan = span;
    }

    private void removeSpanInfo(SpanInfo span) {
        mSpans.remove(span);
        if (mSpans.isEmpty()) {
            mTokenizer.setStart(0);
        } else {
            SpanInfo s = mSpans.get(mSpans.size() - 1);
            int end = getText().getSpanEnd(s.image) + s.sepLength;
            mTokenizer.setStart(end);
        }
    }

    public static class DefaultTokenizer implements Tokenizer {
        protected int mStart;

        public void setStart(int start) {
            mStart = start;
        }

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = mStart;
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            return text.length();
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            return text;
        }
    }
}
