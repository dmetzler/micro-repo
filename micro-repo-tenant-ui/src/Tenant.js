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


const TenantFilter = (props) => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn />
        <TextInput label="City" source="city" />
        <TextInput label="Country" source="country" />
    </Filter>
);

const TenantTitle = ({ record }) => {
    return <span>Tenant
     {record ? `${record.name}` : ''}</span>;
};

export const TenantList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField source="Id" source="id"/>
        </Datagrid>
    </List>
);



export const TenantEdit = props => (
    <Edit title={<TenantTitle />} {...props}>
        <SimpleForm>
            <DisabledInput label="Id" source="id" />
            <DisabledInput label="Name" source="name" />
            <TextInput multiline source="schemaDef" />
        </SimpleForm>
    </Edit>
);

export const TenantCreate = props => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="name" />
            <TextInput label="Schema Definition" multiline source="schemaDef" />
        </SimpleForm>
    </Create>
);