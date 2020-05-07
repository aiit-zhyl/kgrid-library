import Vue from 'vue';
import Vuex from 'vuex';
import 'es6-promise/auto';
import VuexPersistence from 'vuex-persist';
import api from './api';

Vue.use(Vuex);
const vuexLocal = new VuexPersistence({
  key: 'kgridlib',
  storage: window.localStorage,
  reducer: state => ({
    searchsources: state.searchsources,
    filterStrings: state.filterStrings,
    kgselect: state.kgselect,
    paths: state.paths,
    currentKOId: state.currentKOId,
    currentKO: state.currentKO,
    currentObject: state.currentObject,
    servers:state.servers,
    currentServerIndex:state.currentServerIndex
  }),
});
export default new Vuex.Store({
  plugins: [vuexLocal.plugin],
  state: {
    searchsources: ['Title'],
    filterStrings: [],
    newfilterStrings: [],
    kgselect: { text: 'Title - A to Z', v: 'title', order: 'asc' },
    currentKOId: { naan: '', name: '', version: '' },
    currentKO: {},
    currentObject: {},
    currentIOFileurl: '',
    kolist: [],
    paths: {
      default_act_url_index: 0,
      activator_urls: ['https://activator.kgrid-china.net']
    },
    metaschema: {},
    // New Properties
    servers:[],
    currentServerIndex:2,
    hasError: false,
    errorMessage: '',
    demourl:'https://editor.swagger.io'
  },
  getters: {
    getobjectlist: state => state.kolist,
    getCurrentKOId: state => state.currentKOId,
    getCurrentKO: state => state.currentKO,
    getCurrentObject: state => state.currentObject,
    getcurrenturl: (state) => {
      let s = `${state.currentKOId.naan}/${state.currentKOId.name}`;
      if (state.currentKOId.version !== '') {
        s = `${s}/${state.currentKOId.version}`;
      }
      return s;
    },
    getkoiourl: state => state.currentIOFileurl,
    getschemabysection: state => (section) => {
      if(state.metaschema){
        if(state.metaschema.properties){
          if (state.metaschema.properties[section]) {
            return state.metaschema.properties[section];
          }
        }
      }
      return {};
    },
    getcheck: state => state.filtercheck,
    getfilters: state => state.filterStrings,
    getnewfilters: state => state.newfilterStrings,
    getSortKey: state => (state.kgselect.value),
    getSortOrder: state => (state.kgselect.order),
    getSearchSources: state => state.searchsources,
    getKGSelect: state => state.kgselect,
    getactivatorurls: state => state.paths.activator_urls,
    getdemourl: state => state.demourl,
    getdefaultactivatorurlindex: (state) => {
      const i = state.paths.default_act_url_index;
      return i;
    },
    // New Getters
    getservers: state => state.servers,
    getCurrentServerIndex: state => state.currentServerIndex,
    getCurrentServer: state => state.servers[state.currentServerIndex],
    getErrorStatus : state => state.hasError,
    getbaseurl: state => {
      var url = './kos'
      if(state.servers.length!=0){
        var server = state.servers[state.currentServerIndex];
        url = server.host+server.shelf
      }
      if(!url.endsWith('/')){
        url=url+'/'
      }
      return url
    }
  },
  mutations: {
    setmetaschema(state, schema) {
      state.metaschema = schema;
    },
    setkoiourl(state, uri) {
      state.currentIOFileurl = uri;
    },
    setCurrentKO(state, ko) {
      state.currentKO = ko;
    },
    setCurrentObject(state, obj) {
      const s = obj.identifier.replace('ark:/', '').split(/[/-]+/);
      state.currentKOId.naan = s[0];
      state.currentKOId.name = s[1];
      state.currentKOId.version = obj.version;
      state.currentObject = obj;
    },
    setkolist(state, list) {
      state.kolist = [];
      list.forEach((e) => {
        state.kolist.push(e);
      });
      console.log('Retrieving KO List ...');
      console.log(state.kolist);
    },
    setcheck(state, obj) {
      Object.assign(state.filtercheck, obj);
    },
    setkgselect(state, obj) {
      Object.assign(state.kgselect, obj);
    },
    setFilterStrings(state, arr) {
      state.filterStrings = arr;
    },
    addFilterString(state, s) {
      var s_low=s.toLowerCase()
      if(state.filterStrings.indexOf(s_low)==-1){
        state.filterStrings.push(s_low)
      }
    },
    removeFilterString(state, s) {
      var i = state.filterStrings.indexOf(s)
      if(i!=-1){
        state.filterStrings.splice(i,1)
      }
    },
    setSearchSources(state, arr) {
      state.searchsources = arr;
    },
    addSearchSources(state, s) {
      if(state.searchsources.indexOf(s)==-1){
        state.searchsources.push(s)
      }
    },
    setactivatorurls(state, urls) {
      if (urls.length > 0) {
        state.paths.activator_urls = [];
        urls.forEach((e) => {
          state.paths.activator_urls.push(e);
        });
      }
    },
    setdefaultactivatorurlindex(state, i) {
      state.paths.default_act_url_index = i;
    },
    // New setters
    setservers(state, list) {
      state.servers = []
      list.forEach(function(e){
        state.servers.push(e)
      })
    },
    setcurrentServerIndex(state, i){
      state.currentServerIndex =i
    },
    setErrorStatus(state, text){
      state.hasError = (text!='')
      state.errorMessage = text
    },
    setdemourl(state, url){
      state.demourl = url
    },
  },
  actions: {
    fetchkolist(context) {
      const server = context.getters.getCurrentServer;
      var url = server.host+server.shelf
      return api.get(url)
        .then((response) => {
          console.log('[ACTION] fetch ko list')
          console.log(response)
          context.commit('setkolist', response.data);
        })
        .catch((error) => {
          context.commit('setErrorStatus', 'API_FAIL')
        });
    },
    fetchko(context, param) {
      console.log('Fetch KO:');
      console.log(param);
      const baseurl = context.getters.getbaseurl;
      var arkid = param.uri.split('/')
      var endpoint = baseurl+arkid[0]+'/'+arkid[1]
      console.log(endpoint);
      return api.get(endpoint)
        .then((response) => {
          context.commit('setCurrentKO', response.data);
          response.data.forEach(function(e){
            if(arkid[2]){
              if(e.version==arkid[2]){
                context.commit('setCurrentObject', e);
              }
            }
          })
        })
        .catch(error => {
          context.commit('setErrorStatus', 'API_FAIL')
        });
    },
    fetchkoversion(context, param) {
      console.log('Fetch KO:');
      console.log(param);
      const baseurl = context.getters.getbaseurl;
      var endpoint = baseurl+param.uri
      console.log(endpoint);
      return api.get(endpoint)
        .then((response) => {
          context.commit('setCurrentObject', response.data);
        })
        .catch(error => {
          context.commit('setErrorStatus', 'API_FAIL')
        });
    },
    saveMetadata(context, data) {
      const bundle = JSON.parse(data);
      console.log(bundle);
      const baseurl = context.getters.getbaseurl;
      const endpoint = baseurl + context.getters.getcurrenturl;
      console.log(endpoint);
      return api.put(endpoint, bundle)
        .then((respnse) => {
          console.log(respnse);
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
});
