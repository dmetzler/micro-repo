import React, { Component } from 'react';
import {
  List,
  Edit,
  Create,
  Filter,
  SimpleForm,
  TextInput,
  Datagrid,
  TextField
} from 'react-admin';


const LibraryFilter = (props) => (
    <Filter {...props}>
        <TextInput label="City" source="city" alwaysOn />
        <TextInput label="Country" source="country" alwaysOn />
    </Filter>
);

export const LibraryList = props => (
    <List filters={<LibraryFilter/>} {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField source="city" />
            <TextField source="country" />
            <TextField source="creator" />
        </Datagrid>
    </List>
);

export const LibraryEdit = props => (
    <Edit {...props}>
        <SimpleForm>
            <TextInput source="city" />
            <TextInput source="country" />
        </SimpleForm>
    </Edit>
);

export const LibraryCreate = props => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="name" />
            <TextInput source="city" />
            <TextInput source="country" />
        </SimpleForm>
    </Create>
);