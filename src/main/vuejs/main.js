import Vue from 'vue'
import App from './App'
require('bootstrap')
import {getCurrentUser, overlayHeightResize, retrieveObject, retrieveObjectList} from './ot.js';
/* eslint-disable no-new */
// eslint-disable-line
var userModel = {user: {username: '', passwd: '', id: -1, first_name: '', last_name: '', role: ''}} // eslint-disable-line
var vm = new Vue({ // eslint-disable-line
  el: '#app',
  template: '<App/>',
  components: { App },
})
