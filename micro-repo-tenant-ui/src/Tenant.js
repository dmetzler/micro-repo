import React from 'react';
import {
  List,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  DisabledInput,
  Datagrid,
  TextField
} from 'react-admin';

import NxlInput from './ra-codemirror-input';

const TenantTitle = ({ record }) => {
    return <span>Tenant
     {record ? `${record.name}` : ''}</span>;
};

export const TenantList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField label="Id" source="id"/>
        </Datagrid>
    </List>
);



export const TenantEdit = props => (
    <Edit title={<TenantTitle />} {...props}>
        <SimpleForm>

            <DisabledInput label="Id" source="id" />
            <DisabledInput label="Name" source="name" />

            <NxlInput label="Schema Definition" source="schemaDef"/>
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