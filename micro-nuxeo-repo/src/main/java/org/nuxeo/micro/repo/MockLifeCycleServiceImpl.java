package org.nuxeo.micro.repo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.LifeCycleException;
import org.nuxeo.ecm.core.lifecycle.LifeCycle;
import org.nuxeo.ecm.core.lifecycle.LifeCycleService;
import org.nuxeo.ecm.core.model.Document;

public class MockLifeCycleServiceImpl implements LifeCycleService {

    @Override
    public void initialize(Document doc) throws LifeCycleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize(Document doc, String initialStateName) throws LifeCycleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void followTransition(Document doc, String transitionName) throws LifeCycleException {
        // TODO Auto-generated method stub

    }

    @Override
    public LifeCycle getLifeCycleByName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<LifeCycle> getLifeCycles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getTypesFor(String lifeCycleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLifeCycleNameFor(String typeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getNonRecursiveTransitionForDocType(String docTypeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getTypesMapping() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LifeCycle getLifeCycleFor(Document doc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reinitLifeCycle(Document doc) throws LifeCycleException {
        // TODO Auto-generated method stub

    }

}
