// in App.js
import React, { Component } from 'react';
import MicroRepo from './MicroRepo'
import authProvider from "./authProvider"
import LoginPage from "./LoginPage"
import { Admin, Resource, ListGuesser } from 'react-admin';
import { RecordList, RecordCreate, RecordEdit } from "./Resource"

import LibraryIcon from '@material-ui/icons/LibraryBooks';

class App extends MicroRepo {

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
                          list={RecordList}
                          edit={RecordEdit}
                          create={RecordCreate}
                          icon={LibraryIcon}/>

            </Admin>
        );
    }
}

export default App;