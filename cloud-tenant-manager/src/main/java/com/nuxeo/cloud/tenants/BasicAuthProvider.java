package com.nuxeo.cloud.tenants;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class BasicAuthProvider implements AuthProvider {

    private final class SimpleUser extends AbstractUser {
        private final String username;

        private SimpleUser(String username) {
            this.username = username;
        }

        @Override
        public JsonObject principal() {
            return new JsonObject().put("username", username);
        }

        @Override
        public void setAuthProvider(AuthProvider authProvider) {
            // TODO Auto-generated method stub

        }

        @Override
        protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
            resultHandler.handle(Future.succeededFuture("admin".equals(username)));

        }
    }

    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("username");

        resultHandler.handle(Future.succeededFuture(new SimpleUser(username)));

    }

}
