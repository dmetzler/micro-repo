package org.nuxeo.micro.dsl.parser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.nuxeo.micro.dsl.DslModel;
import org.nuxeo.micro.dsl.features.DslFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DslParserImpl implements DslParser {

    private static final Logger log = LoggerFactory.getLogger(DslParserImpl.class);

    private ScriptEngine engine;

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
            throw new ParserException(e);
        }

    }

    /**
     * @param engine
     * @param string
     * @throws ScriptException
     * @throws FileNotFoundException
     */
    private void importJs(ScriptEngine engine, String file) throws FileNotFoundException, ScriptException {
        InputStream is = getClass().getResourceAsStream(file);
        engine.eval(new InputStreamReader(is));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DslModel parse(String dsl, Class<? extends DslFeature>... dslFeatures) {

        DslModel model = DslModel.builder().with(dslFeatures).build();
        Map<String, Object> ast = getAbstractSyntaxTree(dsl);

        model.visit(ast);
        return model;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getAbstractSyntaxTree(String dsl) {
        try {
            Map<String, Object> result = (Map<String, Object>) ((Invocable) engine).invokeFunction("parse", dsl);

            Map<String, Object> ast = (Map<String, Object>) result.get("value");
            ast.put("src", dsl);
            return ast;

        } catch (NoSuchMethodException | ScriptException e) {
            throw new ParserException(e);
        }

    }
}
