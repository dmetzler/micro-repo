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
  TextField,
  ListGuesser,
  EditGuesser
} from 'react-admin';


const RecordTitle = ({ record }) => {
    return <span>Record {record ? `${record.name}` : ''}</span>;
};

export const RecordList = ListGuesser;

// export const RecordList = props => (
//     <List {...props}>
//         <Datagrid rowClick="edit">
//             <TextField source="name" />
//             <TextField source="city" />
//             <TextField source="country" />
//             <TextField label="Creator" source="dc.creator" />
//         </Datagrid>
//     </List>
// );



export const RecordEdit = props => (
    <Edit title={<RecordTitle />} {...props}>
        <SimpleForm>
            <DisabledInput label="Id" source="id" />
            <DisabledInput label="Name" source="name" />
            <TextInput source="city" />
            <TextInput source="country" />
        </SimpleForm>
    </Edit>
);


export const RecordCreate = props => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="parentPath" />
            <TextInput source="name" />
            <TextInput source="city" />
            <TextInput source="country" />
        </SimpleForm>
    </Create>
);