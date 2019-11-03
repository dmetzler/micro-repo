// in App.js
import React, { Component } from 'react';
import buildGraphQLProvider from 'ra-data-graphql-simple';
import { Admin, Resource, ListGuesser, EditGuesser, CreateGuesser} from 'react-admin';


import { LibraryList, LibraryCreate, LibraryEdit } from "./Library"

//import { PostCreate, PostEdit, PostList } from './posts';

class App extends Component {
    constructor() {
        super();
        this.state = { dataProvider: null };
    }
    componentDidMount() {
        buildGraphQLProvider({ clientOptions: { uri: 'http://localhost:8080/graphql/' }})
            .then(dataProvider => this.setState({ dataProvider }));
    }

    render() {
        const { dataProvider } = this.state;

        if (!dataProvider) {
            return <div>Loading</div>;
        }

        return (
            <Admin dataProvider={dataProvider}>
                <Resource name="Library" list={LibraryList} edit={LibraryEdit} create={LibraryCreate} />
            </Admin>
        );
    }
}

export default App;