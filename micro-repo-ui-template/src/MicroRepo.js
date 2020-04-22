import React, { Component } from 'react';
import buildGraphQLProvider from 'ra-data-graphql-simple';
import { AUTH_LOGIN } from 'react-admin';
import authProvider from "./authProvider"

import { createHttpLink } from 'apollo-link-http';
import { setContext } from 'apollo-link-context';

import LibraryIcon from '@material-ui/icons/LibraryBooks';

class MicroRepo extends Component {
    constructor() {
        super();
        this.state = { dataProvider: null };
    }

    getUrlVars() {
        var vars = {};
        window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
            vars[key] = value;
        });
        return vars;
    }

    getUrlParam(parameter, defaultvalue) {
      var urlparameter = defaultvalue;
      if(window.location.href.indexOf(parameter) > -1){
          urlparameter = this.getUrlVars()[parameter];
          }
      return urlparameter;
    }

    async getToken() {
      const token = JSON.parse(localStorage.getItem('token'));

      if (!token) {
        await authProvider(AUTH_LOGIN, { code: this.getUrlParam("code",""), state: this.getUrlParam("state","")});
        return JSON.parse(localStorage.getItem('token'));
      } else {
        return token;
      }
    }

    getGraphQLEndpoint() {
      var microRepoUrl = window._env_.MICRO_REPO_URL;
      var tenantId = window._env_.TENANT_ID;
      return [microRepoUrl,tenantId,"graphql"].join("/");
    }


    componentDidMount() {
      // buildGraphQLProvider({ clientOptions: { uri: 'https://micro.apps.prod.nuxeo.io/library/graphql' }})
      //       .then(dataProvider => this.setState({ dataProvider }));

      const httpLink = createHttpLink({
        uri: this.getGraphQLEndpoint(),
      });



      this.getToken().then( token => {
        const authLink = setContext((_, { headers }) => {
          return {
            headers: {
              ...headers,
              authorization: token ? `Bearer ${token.id_token}` : "",
            }
          }
        });

        buildGraphQLProvider({ clientOptions: { link: authLink.concat(httpLink) }})
          .then(dataProvider => this.setState({ dataProvider }));
      })


    }


}

export default MicroRepo;