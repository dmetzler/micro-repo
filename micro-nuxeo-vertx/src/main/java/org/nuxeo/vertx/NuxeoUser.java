package org.nuxeo.vertx;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class NuxeoUser extends AbstractUser {
    private final String name;
    private final List<String> permissions;

    public NuxeoUser(String name, List<String> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    @Override
    protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(permissions.contains(permission)));
    }

    @Override
    public JsonObject principal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {
        // TODO Auto-generated method stub

    }

}