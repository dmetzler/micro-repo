import React from 'react';
import {
  List,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  DisabledInput,
  Datagrid,
  TextField,
  UrlField
} from 'react-admin';
import PropTypes from 'prop-types';
import NxlInput from './ra-codemirror-input';

const TenantTitle = ({ record }) => {
    return <span>Tenant {record ? `${record.name}` : ''}</span>;
};


const GraphQLEndpointField = ({ source, record={} }) => {
  const url = [ window._env_.MICRO_REPO_ENDPOINT, record['name'],"graphiql/"].join("/");
  return <a target="_blank" href={url}>{url}</a>;
}
GraphQLEndpointField.propTypes = {
    label: PropTypes.string,
    record: PropTypes.object,
    source: PropTypes.string.isRequired,
};


export const TenantList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="name" />
            <TextField label="Id" source="id"/>
            <GraphQLEndpointField label="URL" />
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
            <NxlInput label="Schema Definition" source="schemaDef"/>

        </SimpleForm>
    </Create>
);