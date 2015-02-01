package com.colinalworth.i18n.client.impl;

import com.google.gwt.i18n.client.Dictionary;

public final class AbstractConstantsImpl {
//  private final String dictionaryName;
//
//  public AbstractConstantsImpl(String dictionaryName) {
//    this.dictionaryName = dictionaryName;
//  }

  // presently we are reading the values out as a decoded property string, and are putting them in the JSON map
  // for later use, so at runtime we need to parse them as ints, arrays, messages, etc. As such, these methods
  // read out values from the dictionary as strings and decode at runtime

  public static int getValueByNameAsInt(String dictionaryName, String name) {
    return Integer.parseInt(Dictionary.getDictionary(dictionaryName).get(name));
  }
  public static String getValueByNameAsString(String dictionaryName, String name) {
    return Dictionary.getDictionary(dictionaryName).get(name);
  }
  public static String[] getValueByNameAsStringArray(String dictionaryName, String name) {
    String contents = Dictionary.getDictionary(dictionaryName).get(name);

    //TODO look for escaped, don't have a space, use trim, etc
    return contents.split(", ");
  }
}
