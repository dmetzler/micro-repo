// in App.js
import React, { Component } from 'react';
import buildGraphQLProvider from 'ra-data-graphql-simple';
import { Admin, Resource, AUTH_LOGIN, ListGuesser } from 'react-admin';
import authProvider from "./authProvider"
import LoginPage from "./LoginPage"
import { LibraryList, LibraryCreate, LibraryEdit } from "./Library"

import { createHttpLink } from 'apollo-link-http';
import { setContext } from 'apollo-link-context';

import LibraryIcon from '@material-ui/icons/LibraryBooks';

class App extends Component {
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


    componentDidMount() {

      const httpLink = createHttpLink({
        uri: 'http://localhost:8080/graphql/',
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

    render() {
        const { dataProvider } = this.state;

        if (!dataProvider) {
            return <div>Loading</div>;
        }

        return (
            <Admin dataProvider={dataProvider}
                   authProvider={authProvider}
                   loginPage={LoginPage}>

                <Resource name="Library"
                          list={LibraryList}
                          edit={LibraryEdit}
                          create={LibraryCreate}
                          icon={LibraryIcon}/>
            </Admin>
        );
    }
}

export default App;