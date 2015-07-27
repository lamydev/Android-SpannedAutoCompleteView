# Android SpannedAutoCompleteView

`SpannedAutoCompleteView` is a subclass of `MultiAutoCompleteTextView`.

The auto-complete text is converted into a image span.

## Demo

Sample code is also available in this repository.

![demo](https://github.com/lamydev/Android-SpannedAutoCompleteView/blob/master/samples/demo/demo.gif)

## Programming Guide

### Compatibility
* minSdkVersion: 11

### Gradle

Android-SpannedAutoCompleteView library is pushed to Maven Central as a AAR, so you just need to declare the following dependency to your `build.gradle`.

``` xml
dependencies {
    compile 'com.github.lamydev:android-spannedautocompleteview:1.0'
}
```

### Span Layers

The image span is constructed by a stack of `SpannAutoCompleteView#SpanLayer`.

#### Background

The span background resides at the bottom of the span layer stack.

``` java
public void setSpanBackground(int resId);
public void setSpanBackground(Drawable drawable);
```

#### SpanLayer Construction/Destruction

To create a new span layer:

``` java
public SpanLayer createSpanLayer();
```

To destroy a span layer:

``` java
public void destroySpanLayer(SpanLayer layer);
```

#### SpanLayer Configuration (Methods of `SpannedAutoCompleteView#SpanLayer`)

To set a `Drawable` to a span layer:

``` java
public SpanLayer setDrawable(int resId);
public SpanLayer setDrawable(Drawable drawable);
```

To set gravity of a span layer (default is CENTER):

     ____________________________
    |                            |
    |            Top             |
    |       --------------       |
    |       |            |       |
    |  Left |   Center   | Right |
    |       |            |       |
    |       --------------       |
    |           Bottom           |
    |____________________________|

``` java
public SpanLayer setGravity(int gravity);
```

To set margins of a span layer:

``` java
public SpanLayer setMargin(int l, int t, int r, int b);
```

#### Span Removal

To remove a span associated with the dropdownItem:

``` java
public void removeSpan(Object dropdownItem);
```

To remove a span automatically when the associated dropdownItem gets clicked:

``` java
public void setAutoRemove(boolean auto);
```

### Tokenizer

By default, the tokenizer is automatically set up during view construction.

You don't need to consider about it. However, if you do, please remember that:

The parameter `Tokenizer` passed to `setTokenizer` MUST be an instance of `SpannedAutoCompleteView#DefaultTokenizer`.

Otherwise, exception `IllegalArgumentException` will be thrown.

### Separator

By default, the separator is a white space `' '`.

``` java
public void setSeparator(char separator);
public void setSeparator(CharSequence separator);
```

### Callback

By setting a callback, you can monitor any events occurred on a span.

``` java
public void setCallback(Callback cb);
```

Called when the user clicks on a dropdownItem to complete the text:

``` java
void onSpanCreate(SpannedAutoCompleteView view, Object dropdownItem);
```

Called when a span is added:

``` java
void onSpanAdded(SpannedAutoCompleteView view, Object dropdownItem);
```

Called when a span is removed:

``` java
void onSpanRemoved(SpannedAutoCompleteView view, Object dropdownItem);
```

Called when the user clicks on a span:

``` java
void onSpanClick(SpannedAutoCompleteView view, Object dropdownItem);
```

## Developers
* Zemin Liu (lam2dev@gmail.com)

Any questions, contributions, bug fixes, and patches are welcomed. ^\_^

## License

```
Copyright (C) 2015 Zemin Liu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
