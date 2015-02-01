package com.example.client;

import com.colinalworth.i18n.client.I18nLoader;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;

import java.util.Date;

/**
 * Created by colin on 1/28/15.
 */
public class App implements EntryPoint {
  @Override
  public void onModuleLoad() {
    new I18nLoader().load("en_US", new Callback<Void, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        Window.alert(reason.getMessage());
      }

      @Override
      public void onSuccess(Void result) {
        Strings s = GWT.create(Strings.class);
//        DateTimeFormat format = new DateTimeFormat(){};
        DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);


        Window.alert(s.foo() + ": " + format.format(new Date()));
      }
    });
  }
}
