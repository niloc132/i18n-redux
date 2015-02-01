package com.colinalworth.i18n.client;

import com.google.gwt.core.client.Callback;

/**
 * Created by colin on 1/28/15.
 */
public class I18nLoader {

  public void load(String locale, Callback<Void, Throwable> callback) {
    //..lookup locale, dont take it as a param, and load the 'permutation' of strings
    callback.onSuccess(null);
  }
}
