package org.nuxeo.micro;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.SimpleDocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocBasedNuxeoPrincipalImpl implements NuxeoPrincipal {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(DocBasedNuxeoPrincipalImpl.class);

    public final List<String> roles = new LinkedList<>();

    // group not stored in the backend and added at login time
    public List<String> virtualGroups = new LinkedList<>();

    // transitive closure of the "member of group" relation
    public List<String> allGroups = new LinkedList<>();

    public boolean isAnonymous;

    public boolean isAdministrator;

    public String principalId;

    public DocumentModel model;

    public DataModel dataModel;

    public String origUserName;

    protected UserConfig config = UserConfig.DEFAULT;

    private DocBasedNuxeoPrincipalImpl() {
    }

    public static class Builder {
        private DocBasedNuxeoPrincipalImpl principal;

        private SchemaManager sm;

        private boolean isAnonymous = false;

        private boolean isAdministrator = false;

        private boolean updateAllGroups = true;

        private String name;

        public Builder(SchemaManager sm) {
            this.sm = sm;
            principal = new DocBasedNuxeoPrincipalImpl();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder isAnomymous(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        public Builder isAdministrator(boolean isAdministrator) {
            this.isAdministrator = isAdministrator;
            return this;
        }

        public Builder updateAllGroups(boolean updateAllGroups) {
            this.updateAllGroups = updateAllGroups;
            return this;
        }

        public DocBasedNuxeoPrincipalImpl build() {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("Name property is mandatory");
            }
            principal.isAnonymous = this.isAnonymous;
            principal.isAdministrator = this.isAdministrator;
            UserConfig config = UserConfig.DEFAULT;
            principal.setModel(new SimpleDocumentModel(this.sm, config.schemaName));
            principal.dataModel.setData(config.nameKey, this.name);
            return principal;
        }

    }

    protected DocBasedNuxeoPrincipalImpl(DocBasedNuxeoPrincipalImpl other) {
        config = other.config;
        try {
            model = other.model.clone();
            model.copyContextData(other.model);
        } catch (CloneNotSupportedException cause) {
            throw new NuxeoException("Cannot clone principal " + this);
        }
        dataModel = model.getDataModel(config.schemaName);
        roles.addAll(other.roles);
        allGroups = new ArrayList<>(other.allGroups);
        virtualGroups = new ArrayList<>(other.virtualGroups);
        isAdministrator = other.isAdministrator;
        isAnonymous = other.isAnonymous;
        origUserName = other.origUserName;
        principalId = other.principalId;
    }

    public void setConfig(UserConfig config) {
        this.config = config;
    }

    public UserConfig getConfig() {
        return config;
    }

    @Override
    public String getCompany() {
        try {
            return (String) dataModel.getData(config.companyKey);
        } catch (PropertyException e) {
            return null;
        }
    }

    @Override
    public void setCompany(String company) {
        dataModel.setData(config.companyKey, company);
    }

    @Override
    public String getFirstName() {
        try {
            return (String) dataModel.getData(config.firstNameKey);
        } catch (PropertyException e) {
            return null;
        }
    }

    @Override
    public void setFirstName(String firstName) {
        dataModel.setData(config.firstNameKey, firstName);
    }

    @Override
    public String getLastName() {
        try {
            return (String) dataModel.getData(config.lastNameKey);
        } catch (PropertyException e) {
            return null;
        }
    }

    @Override
    public void setLastName(String lastName) {
        dataModel.setData(config.lastNameKey, lastName);
    }

    // impossible to modify the name - it is PK
    @Override
    public void setName(String name) {
        dataModel.setData(config.nameKey, name);
    }

    @Override
    public void setRoles(List<String> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    @Override
    public void setGroups(List<String> groups) {
        if (virtualGroups != null && !virtualGroups.isEmpty()) {
            List<String> groupsToWrite = new ArrayList<>();
            for (String group : groups) {
                if (!virtualGroups.contains(group)) {
                    groupsToWrite.add(group);
                }
            }
            dataModel.setData(config.groupsKey, groupsToWrite);
        } else {
            dataModel.setData(config.groupsKey, groups);
        }
    }

    @Override
    public String getName() {
        try {
            return (String) dataModel.getData(config.nameKey);
        } catch (PropertyException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getGroups() {
        List<String> groups = new LinkedList<>();
        List<String> storedGroups;
        try {
            storedGroups = (List<String>) dataModel.getData(config.groupsKey);
        } catch (PropertyException e) {
            return null;
        }
        if (storedGroups != null) {
            groups.addAll(storedGroups);
        }
        groups.addAll(virtualGroups);
        return groups;
    }

    @Deprecated
    @Override
    public List<String> getRoles() {
        return new ArrayList<>(roles);
    }

    @Override
    public void setPassword(String password) {
        dataModel.setData(config.passwordKey, password);
    }

    @Override
    public String getPassword() {
        // password should never be read at the UI level for safety reasons
        // + backend directories usually only store hashes that are useless
        // except to check authentication at the directory level
        return null;
    }

    @Override
    public String toString() {
        return (String) dataModel.getData(config.nameKey);
    }

    @Override
    public String getPrincipalId() {
        return principalId;
    }

    @Override
    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    @Override
    public String getEmail() {
        try {
            return (String) dataModel.getData(config.emailKey);
        } catch (PropertyException e) {
            return null;
        }
    }

    @Override
    public void setEmail(String email) {
        dataModel.setData(config.emailKey, email);
    }

    public DocumentModel getModel() {
        return model;
    }

    /**
     * Sets model and recomputes all groups.
     */
    public void setModel(DocumentModel model, boolean updateAllGroups) {
        this.model = model;
        dataModel = model.getDataModels().values().iterator().next();
        if (updateAllGroups) {
            updateAllGroups();
        }
    }

    public void setModel(DocumentModel model) {
        setModel(model, true);
    }

    @Override
    public boolean isMemberOf(String group) {
        return allGroups.contains(group);
    }

    @Override
    public List<String> getAllGroups() {
        return new ArrayList<>(allGroups);
    }

    public void updateAllGroups() {
        // UserManager userManager = Framework.getService(UserManager.class);
        // Set<String> checkedGroups = new HashSet<>();
        // List<String> groupsToProcess = new ArrayList<>();
        // List<String> resultingGroups = new ArrayList<>();
        // groupsToProcess.addAll(getGroups());
        //
        // while (!groupsToProcess.isEmpty()) {
        // String groupName = groupsToProcess.remove(0);
        // if (!checkedGroups.contains(groupName)) {
        // checkedGroups.add(groupName);
        // NuxeoGroup nxGroup = null;
        // if (userManager != null) {
        // try {
        // nxGroup = userManager.getGroup(groupName);
        // } catch (DirectoryException de) {
        // if (virtualGroups.contains(groupName)) {
        // // do not fail while retrieving a virtual group
        // log.warn("Failed to get group '" + groupName + "' due to '" + de.getMessage()
        // + "': permission resolution involving groups may not be correct");
        // nxGroup = null;
        // } else {
        // throw de;
        // }
        // }
        // }
        // if (nxGroup == null) {
        // if (virtualGroups.contains(groupName)) {
        // // just add the virtual group as is
        // resultingGroups.add(groupName);
        // } else if (userManager != null) {
        // // XXX this should only happens in case of
        // // inconsistency in DB
        // log.error("User " + getName() + " references the " + groupName + " group that does not exists");
        // }
        // } else {
        // groupsToProcess.addAll(nxGroup.getParentGroups());
        // // fetch the group name from the returned entry in case
        // // it does not have the same case than the actual entry in
        // // directory (for case insensitive directories)
        // resultingGroups.add(nxGroup.getName());
        // // XXX: maybe remove group from virtual groups if it
        // // actually exists? otherwise it would be ignored when
        // // setting groups
        // }
        // }
    }

    public List<String> getVirtualGroups() {
        return new ArrayList<>(virtualGroups);
    }

    public void setVirtualGroups(List<String> virtualGroups, boolean updateAllGroups) {
        this.virtualGroups = new ArrayList<>(virtualGroups);
        if (updateAllGroups) {
            updateAllGroups();
        }
    }

    /**
     * Sets virtual groups and recomputes all groups.
     */
    public void setVirtualGroups(List<String> virtualGroups) {
        setVirtualGroups(virtualGroups, true);
    }

    @Override
    public boolean isAdministrator() {
        return isAdministrator;
    }

    @Override
    public String getTenantId() {
        return null;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Principal) {
            String name = getName();
            String otherName = ((Principal) other).getName();
            if (name == null) {
                return otherName == null;
            } else {
                return name.equals(otherName);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        String name = getName();
        return name == null ? 0 : name.hashCode();
    }

    @Override
    public String getOriginatingUser() {
        return origUserName;
    }

    @Override
    public void setOriginatingUser(String originatingUser) {
        origUserName = originatingUser;
    }

    @Override
    public String getActingUser() {
        return getOriginatingUser() == null ? getName() : getOriginatingUser();
    }

    @Override
    public boolean isTransient() {
        String name = getName();
        return name != null && name.startsWith(TRANSIENT_USER_PREFIX);
    }

    // protected NuxeoPrincipal cloneTransferable() {
    // return new TransferableClone(this);
    // }

    // /**
    // * Provides another implementation which marshall the user id instead of transferring the whole content and
    // resolve
    // * it when unmarshalled.
    // */
    // static protected class TransferableClone extends NuxeoPrincipalImpl {
    //
    // protected TransferableClone(NuxeoPrincipalImpl other) {
    // super(other);
    // }
    //
    // static class DataTransferObject implements Serializable {
    //
    // private static final long serialVersionUID = 1L;
    //
    // final String username;
    //
    // final String originatingUser;
    //
    // DataTransferObject(NuxeoPrincipal principal) {
    // username = principal.getName();
    // originatingUser = principal.getOriginatingUser();
    // }
    //
    // private Object readResolve() throws ObjectStreamException {
    // UserManager userManager = Framework.getService(UserManager.class);
    // // look up principal as system user to avoid permission checks in directories
    // NuxeoPrincipal principal = Framework.doPrivileged(() -> userManager.getPrincipal(username));
    // if (principal == null) {
    // throw new NullPointerException("No principal: " + username);
    // }
    // principal.setOriginatingUser(originatingUser);
    // return principal;
    // }
    //
    // }
    //
    // private Object writeReplace() throws ObjectStreamException {
    // return new DataTransferObject(this);
    // }
    // }
}
