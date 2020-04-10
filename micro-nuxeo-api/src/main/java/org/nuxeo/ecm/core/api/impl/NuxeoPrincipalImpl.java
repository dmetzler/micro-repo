package org.nuxeo.ecm.core.api.impl;

import java.util.List;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;

public class NuxeoPrincipalImpl implements NuxeoPrincipal {

    private static final long serialVersionUID = 1L;

    protected final String username;

    protected String firstName;
    protected String lastName;
    protected String company;
    protected String email;
    protected String originatingUser;

    protected boolean isAdministrator = false;

    private String tenantId;

    public NuxeoPrincipalImpl(String username, String tenantId) {
        this.username = username;
        this.tenantId = tenantId;
    }

    public void setAdministrator(boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public List<String> getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isMemberOf(String group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;

    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;

    }

    @Override
    public void setGroups(List<String> groups) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRoles(List<String> roles) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCompany(String company) {
        this.company = company;

    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public void setEmail(String email) {
        this.email = email;

    }

    @Override
    public String getPrincipalId() {
        return username;
    }

    @Override
    public void setPrincipalId(String principalId) {

    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String getOriginatingUser() {
        return username;
    }

    @Override
    public void setOriginatingUser(String originatingUser) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getActingUser() {
        return username;
    }

    @Override
    public boolean isTransient() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAdministrator() {
        return isAdministrator;
    }

}
