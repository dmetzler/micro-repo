import React, {Component} from 'react';
import { addField } from 'ra-core';
import PropTypes from 'prop-types';
import FormControl from '@material-ui/core/FormControl';
import { withStyles } from '@material-ui/core/styles';

import {Controlled as CodeMirror} from 'react-codemirror2'


import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/material.css';

require('./codemirror.nxl.mode');

const styles = {};




class NxlInput extends Component {


  static propTypes = {
    input: PropTypes.object,
    source: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.state = {
      value: ''
    };

    this.handleValueChange.bind(this)
  }

  componentDidMount() {

    const {
      input: {value}
    } = this.props;

    this.setState({value});


  }

  handleValueChange(value) {
    this.props.input.onChange(value);
  };

  render() {
    var options = {
      lineNumbers: true,
      mode: 'nxl',
      matchBrackets: true,
      autoCloseBrackets: true,
      extraKeys: {
        "Ctrl-Space": "autocomplete"
      }
    };
    return (
      <FormControl fullWidth={true} className='ra-input-nxl'>
      <CodeMirror value={ this.state.value }
                  options={ options }
                  onBeforeChange={(editor, data, value) => {
                    this.setState({value});
                  }}
                  onChange={ (editor, data, value) => this.handleValueChange(value)}
      />

      </FormControl>
    );
  }


}

const NxlInputWithField = addField(withStyles(styles)(NxlInput));

NxlInputWithField.defaultProps = {
  addLabel: true,
  fullWidth: true,
};



// (props) => (
//    <CodeMirror {...props} options={{
//       lineNumbers: true

//    }} value={props.record.schemaDef}/>
// )


export default NxlInputWithField ;