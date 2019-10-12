package org.nuxeo.micro.dsl.parser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.DslModel.DslModelBuilder;
import org.nuxeo.micro.dsl.features.DocumentTypeFeature;
import org.nuxeo.micro.dsl.features.DslFeature;
import org.nuxeo.micro.dsl.features.DslSourceFeature;
import org.nuxeo.micro.dsl.features.SchemaFeature;

import com.sun.xml.xsom.impl.SchemaSetImpl;

public class DslParserImpl implements DslParser {

    private static final Log log = LogFactory.getLog(DslParserImpl.class);

    private ScriptEngine engine;

    private Set<Class<? extends DslFeature>> featureClasses = new HashSet<>();

    private DslModel.DslModelBuilder modelBuilder;

    public void init() {

        log.info("Instanticating parser");
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            // Nashorn polyfill
            importJs(engine, "/js/lib/global-polyfill.js");
            importJs(engine, "/js/lib/chevrotain.min.js");

            importJs(engine, "/js/nuxeo_dsl.js");
            importJs(engine, "/js/nuxeo_dsl_javainterpreter.js");

            engine.eval("var parse = function(dsl) { return global.nuxeo_dsl_javainterpreter.parse(dsl)}");
            log.info("DSL compilator compiled");
        } catch (ScriptException | FileNotFoundException e) {

            log.error("Unable to compile DSL compilator", e);
            throw new NuxeoException(e);
        }

        featureClasses.add(DocumentTypeFeature.class);
        featureClasses.add(DslSourceFeature.class);
        featureClasses.add(SchemaFeature.class);
    }

    /**
     * @param engine
     * @param string
     * @throws ScriptException
     * @throws FileNotFoundException
     * @since TODO
     */
    private void importJs(ScriptEngine engine, String file) throws FileNotFoundException, ScriptException {
        InputStream is = getClass().getResourceAsStream(file);
        engine.eval(new InputStreamReader(is));
    }

    public void registerFeature(FeatureDescriptor featureDescriptor) {
        featureClasses.add(featureDescriptor.klass);
        modelBuilder = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DslModel parse(String dsl) {

        // TODO make the builder pluggable
        DslModel model = getDslModelBuilder().build();
        try {
            Map<String, Object> result = (Map<String, Object>) ((Invocable) engine).invokeFunction("parse", dsl);
            Map<String, Object> ast = (Map<String, Object>) result.get("value");

            model.setSource(dsl);
            model.visit(ast);
            return model;

        } catch (NoSuchMethodException | ScriptException e) {
            throw new NuxeoException(e);
        }
    }

    private DslModelBuilder getDslModelBuilder() {
        if (modelBuilder == null) {
            modelBuilder = DslModel.builder();

            for (Class<? extends DslFeature> featureKlass : featureClasses) {
                modelBuilder.with(featureKlass);
            }
        }
        return modelBuilder;
    }
}
