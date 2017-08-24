import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
  strict: debug,
  state:{
    debugEnabled:true,
    baseurl:'',
    currentUser: {username:"",password:"",admin:false},
    currentObject: { metadata:{title:"",keywords:"",contributors:"",published:"",citations:[],license:{licenseName:"",licenseLink:""}}, payload:{functionName:"",engineType:"",content:""},inputMessage:"", outputMessage:"", uri:"",published:false,lastModified:0,createdOn:0} ,
    activeTab:'METADATA',
    libraryEnv: {},
    libraryname: ''
  },
  mutations: {
    seturl(state, url) {
      state.baseurl = url;
    },
    setuser(state, user){
      state.currentUser =user;
    },
    setactivetab(state, tab){
      state.activeTab = tab;
    },
    setobject(state, obj) {
      state.currentObject = obj;
    },
    setenv(state, env){
      console.log(env);
      state.libraryEnv = env;
      state.libraryname= env.fcrepo["library.name"];
    }
  },
  getters: {
    isLoggedIn: state => {
     return (state.currentUser.username!='')
    },
    isAdmin: state => {
     return (state.currentUser.admin)
    },
    getfirstname: state => {
      return (state.currentUser.first_name || '')
    },
    getuser: state => {
       return state.currentUser
    },
    getactivetab: state => {
      return state.activeTab
    },
    getobject: state =>{
      return state.currentObject
    },
    getLibraryName: state =>{
        return (state.libraryname || '')
    }
  }
  // plugins: debug ? [createLogger()] : []
})