/*
 * Copyright (C) 2013 Square, Inc.
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
package com.squareup.picasso3;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;
import static com.squareup.picasso3.Picasso.LoadedFrom.MEMORY;
import static com.squareup.picasso3.TestUtils.NO_HANDLERS;
import static com.squareup.picasso3.TestUtils.NO_TRANSFORMERS;
import static com.squareup.picasso3.TestUtils.RESOURCE_ID_1;
import static com.squareup.picasso3.TestUtils.UNUSED_CALL_FACTORY;
import static com.squareup.picasso3.TestUtils.URI_KEY_1;
import static com.squareup.picasso3.TestUtils.makeBitmap;
import static com.squareup.picasso3.TestUtils.mockCallback;
import static com.squareup.picasso3.TestUtils.mockImageViewTarget;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ImageViewActionTest {

  @Test(expected = AssertionError.class)
  public void throwsErrorWithNullResult() {
    ImageViewAction action =
        new ImageViewAction(mock(Picasso.class), mockImageViewTarget(), null, 0, 0,
            null, URI_KEY_1, null, null, false);
    action.complete(null);
  }

  @Test
  public void returnsIfTargetIsNullOnComplete() {
    Bitmap bitmap = makeBitmap();
    Picasso picasso = mock(Picasso.class);
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, null, URI_KEY_1, null,
            callback, false);
    request.target.clear();
    request.complete(new RequestHandler.Result(bitmap, MEMORY));
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void returnsIfTargetIsNullOnError() {
    Picasso picasso = mock(Picasso.class);
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, null, URI_KEY_1, null,
            callback, false);
    request.target.clear();
    request.error(new RuntimeException());
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void invokesTargetAndCallbackSuccessIfTargetIsNotNull() {
    Bitmap bitmap = makeBitmap();
    Dispatcher dispatcher = mock(Dispatcher.class);
    PlatformLruCache cache = new PlatformLruCache(0);
    Picasso picasso =
        new Picasso(RuntimeEnvironment.application, dispatcher, UNUSED_CALL_FACTORY, null, cache,
            null, NO_TRANSFORMERS, NO_HANDLERS, mock(Stats.class), Bitmap.Config.ARGB_8888, false,
            false);
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, null, URI_KEY_1, null, callback, false);
    request.complete(new RequestHandler.Result(bitmap, MEMORY));
    verify(target).setImageDrawable(any(PicassoDrawable.class));
    verify(callback).onSuccess();
  }

  @Test
  public void invokesTargetAndCallbackErrorIfTargetIsNotNullWithErrorResourceId() {
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, RESOURCE_ID_1, null, null, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageResource(RESOURCE_ID_1);
    verify(callback).onError(e);
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorResourceId() {
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, RESOURCE_ID_1, null, null, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageResource(RESOURCE_ID_1);
    verify(callback).onError(e);
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorDrawable() {
    Drawable errorDrawable = mock(Drawable.class);
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, 0, 0, errorDrawable, URI_KEY_1, null,
            callback, false);
    Exception e = new RuntimeException();
    request.error(e);
    verify(target).setImageDrawable(errorDrawable);
    verify(callback).onError(e);
  }

  @Test
  public void clearsCallbackOnCancel() {
    Picasso picasso = mock(Picasso.class);
    ImageView target = mockImageViewTarget();
    Callback callback = mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, null, URI_KEY_1, null,
            callback, false);
    request.cancel();
    assertThat(request.callback).isNull();
  }

  @Test
  public void stopPlaceholderAnimationOnError() {
    Picasso picasso = mock(Picasso.class);
    AnimationDrawable placeholder = mock(AnimationDrawable.class);
    ImageView target = mockImageViewTarget();
    when(target.getDrawable()).thenReturn(placeholder);
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, 0, 0, null, URI_KEY_1, null,
            null, false);
    request.error(new RuntimeException());
    verify(placeholder).stop();
  }
}
