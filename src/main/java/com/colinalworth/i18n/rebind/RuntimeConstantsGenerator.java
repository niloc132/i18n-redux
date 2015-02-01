package com.colinalworth.i18n.rebind;

import com.colinalworth.i18n.client.impl.AbstractConstantsImpl;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.i18n.rebind.AbstractResource;
import com.google.gwt.i18n.rebind.ResourceFactory;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Runtime version of com.google.gwt.i18n.rebind.LocalizableGenerator that will
 * generate a constant file in the generated output so that it can be loaded on
 * its own, and avoiding a permutation explosion.
 */
public class RuntimeConstantsGenerator extends Generator {
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    TypeOracle oracle = context.getTypeOracle();

    PropertyOracle propertyOracle = context.getPropertyOracle();

    JClassType toGenerate = oracle.findType(typeName);

    if (toGenerate == null) {
      logger.log(TreeLogger.ERROR, typeName + " is not an interface type");
      throw new UnableToCompleteException();
    }

    String packageName = toGenerate.getPackage().getName();
    String simpleSourceName = toGenerate.getName().replace('.', '_') + "_Impl";
    PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
    if (pw == null) {
      return packageName + "." + simpleSourceName;
    }

    ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleSourceName);

    if (toGenerate.isInterface() != null) {
      factory.addImplementedInterface(typeName);
    } else {
      assert toGenerate.isClass() != null;
      factory.setSuperclass(typeName);
    }

    factory.addImport(AbstractConstantsImpl.class.getName());
    SourceWriter sw = factory.createSourceWriter(context, pw);

    sw.println("private String dictionaryName = \"%1$s\";", escape(typeName));

    boolean isConstants = true;
    Map<GwtLocale, OutputStream> outputStreams = new HashMap<GwtLocale, OutputStream>();
    Map<GwtLocale, AbstractResource.ResourceList> bundles = new HashMap<GwtLocale, AbstractResource.ResourceList>();
    try {
      //read all possible locales and their values, save or write for later use
      GwtLocaleFactoryImpl localeFactory = new GwtLocaleFactoryImpl();
      for (String localeString : propertyOracle.getConfigurationProperty("rt.locale").getValues()) {
        GwtLocale locale = localeFactory.fromString(localeString);
        AbstractResource.ResourceList bundle = ResourceFactory.getBundle(logger, toGenerate, locale, isConstants, context);
        GwtLocale generatedLocale = bundle.findLeastDerivedLocale(logger, locale);
//        bundles.put(generatedLocale, bundle);
        OutputStream stream = context.tryCreateResource(logger, typeName + "-" + generatedLocale.getAsString() + "");
        assert stream != null : "first time generating this interface, but resource already exists?";
//        outputStreams.put(generatedLocale, stream);

        JSONObject obj = new JSONObject();
        for (JMethod method : toGenerate.getOverridableMethods()) {
          if (method.getEnclosingType().equals(oracle.getJavaLangObject())) {
            continue;
          }
          String name = method.getName();
          String value = bundle.getRequiredStringExt(name, null);
          obj.put(name, value);
        }
        new PrintWriter(stream).println(obj.toString());
        context.commitResource(logger, stream).setVisibility(EmittedArtifact.Visibility.Public);
      }
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.Type.ERROR, "Failed to read locale value, could not continue", e);
      throw new UnableToCompleteException();
    } catch (JSONException e) {
      logger.log(TreeLogger.Type.ERROR, "Failed to write JSON, could not continue", e);
      throw new UnableToCompleteException();
    }

    //ctor, not needed for now
//    sw.println("public %1$s() {", simpleSourceName);
//    sw.indent();
//    sw.println("super(\"%1$s\");", escape(typeName));
//    sw.outdent();
//    sw.println("}");

    for (JMethod method : toGenerate.getOverridableMethods()) {
      if (method.getEnclosingType().equals(oracle.getJavaLangObject())) {
        continue;
      }

      String name = method.getName();

      sw.println("%1$s {", method.getReadableDeclaration(false, true, true, true, true));
      sw.indent();

      if (method.getReturnType().isArray() != null) {
        JType component = method.getReturnType().isArray().getComponentType();
        if (component.isClass() != null && component.isClass().getQualifiedSourceName().equals("java.lang.String")) {
          sw.println("return AbstractConstantsImpl.getValueByNameAsStringArray(dictionaryName, \"%1$s\");", name);
        }
      } else if (method.getReturnType() == JPrimitiveType.INT) {
        sw.println("return AbstractConstantsImpl.getValueByNameAsInt(dictionaryName, \"%1$s\");", name);
      } else {

        //fall back to string
        sw.println("return AbstractConstantsImpl.getValueByNameAsString(dictionaryName, \"%1$s\");", name);
      }

      sw.outdent();
      sw.println("}");
    }

    sw.commit(logger);

    return factory.getCreatedClassName();
  }
}
