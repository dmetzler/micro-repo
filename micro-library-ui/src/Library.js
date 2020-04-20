import React from 'react';
import {
  List,
  Edit,
  Create,
  Filter,
  SimpleForm,
  TextInput,
  DisabledInput,
  Datagrid,
  TextField
} from 'react-admin';

import RichTextInput from 'ra-input-rich-text';


const LibraryFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn />
        <TextInput label="City" source="city" />
        <TextInput label="Country" source="country" />
    </Filter>
);

const LibraryTitle = ({ record }) => {
    return <span>Library {record ? `${record.name}` : ''}</span>;
};

export const LibraryList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField source="city" />
            <TextField source="country" />
            <TextField label="Creator" source="dc.creator" />
        </Datagrid>
    </List>
);



export const LibraryEdit = props => (
    <Edit title={<LibraryTitle />} {...props}>
        <SimpleForm>
            <DisabledInput label="Id" source="id" />
            <DisabledInput label="Name" source="name" />
            <TextInput source="city" />
            <TextInput source="country" />
            <RichTextInput source="description" />
        </SimpleForm>
    </Edit>
);

export const LibraryCreate = props => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="parentPath" />
            <TextInput source="name" />
            <TextInput source="city" />
            <TextInput source="country" />
            <RichTextInput source="description" />
        </SimpleForm>
    </Create>
);