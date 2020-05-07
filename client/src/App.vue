<template>
  <v-app>
    <v-toolbar app>
      <router-link to='/'>
        <img src="./assets/logo.png" alt="" style="width:40px;height:40px">
      </router-link>
      <v-menu :nudge-width="100" :disabled = '!serverSelection'>
        <template v-slot:activator="{ on }">
          <v-toolbar-title v-on="on">
            <span  class='toolbar_title_text' v-if='currentserver!=null'>{{currentserver.type}} <small><small>{{currentserver.name}}</small></small></span>
            <!-- <v-icon dark>arrow_drop_down</v-icon> -->
          </v-toolbar-title>
        </template>
        <v-list>
          <v-list-tile
            v-for="(item, index) in servers"
            :key="index"
            @click="selectserver(index)"
          >
            <v-list-tile-title v-text="item.type.concat(' - ',item.name)" v-if='item.name!=""'></v-list-tile-title>
            <v-list-tile-title v-text="item.type.concat(' (Current Hosting Server) ')" v-else ></v-list-tile-title>
          </v-list-tile>
          <hr>
          <v-list-tile
            :key="servers.length"
            @click="dialog = true"
          >
            <v-list-tile-title>Add a server</v-list-tile-title>
          </v-list-tile>
        </v-list>
      </v-menu>
      <v-spacer></v-spacer>
      <v-toolbar-items class="hidden-sm-and-down">
        <router-link class='toolbar_link' to='/about'>关于</router-link>
        <!-- <router-link class='toolbar_link' to='/faq'>FAQ</router-link>
        <router-link class='toolbar_link' to='/contactus'>Contact Us</router-link> -->
      </v-toolbar-items>
    </v-toolbar>
    <v-content>
      <router-view/>
      <div v-bind:is='currentOLView'  v-if='showOverlay.show'></div>
    </v-content>
    <v-dialog v-model="dialog" max-width="800px">
      <v-card>
        <v-card-title>
          <span class="headline">Add a Server and Connect</span>
        </v-card-title>
        <v-card-text>
          <v-layout row wrap my-2 >
            <v-flex xs12>
              <v-text-field
                label="Server Name"
                box
                v-model='server.name'
              ></v-text-field>
            </v-flex>
          </v-layout>
          <v-layout row wrap my-2 >
            <v-flex xs12>
              <v-text-field
                label="Server URL"
                box
                v-model='server.host'
              ></v-text-field>
            </v-flex>
          </v-layout>
          <hr>
          <v-radio-group v-model="server.type" row>
            <template v-slot:label>
              <div>Server Type: </div>
            </template>
            <v-radio label="知识对象库" value="知识对象库"></v-radio>
            <v-radio label="Activator" value="Activator"></v-radio>
          </v-radio-group>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" flat @click="doneConfig">Done</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>
<script>
import login from './components/login';
import ioviewer from './components/ioviewer';
import objuploader from './components/objuploader';
import objact from './components/objact';
export default {
  name: 'App',
  data() {
    return {
      showOverlay: { show: false },
      currentOLView: 'login',
      dialog: false,
      servers:[
        {
          "host": ".",
          "shelf": "/kos",
          "type": "知识对象库",
          "name": ""
        },
        {
          "host": ".",
          "shelf": "/kos",
          "type": "Activator",
          "name": ""
        }
      ],
      server:{
        host:".",
        shelf:"/kos",
        type:"知识对象库",
        name:""
      },
      serverSelection:false
    };
  },
  created: function(){
    var self = this
    Promise.all([
      this.$http.get("./static/json/metadataschema.json"),
      this.$http.get("./static/json/config.json")
    ]).then(function(responses) {
      self.$store.commit('setmetaschema',responses[0].data);
      self.$store.commit('setdemourl',responses[1].data.demourl);
      self.serverSelection=responses[1].data.serverSelection
      self.$store.commit('setservers',self.servers);
      self.$store.commit('setcurrentServerIndex', 0);
      self.serverSelection=false;
    }).catch(error=>{
      console.log(error)
    })
    this.$eventBus.$on("viewio", function(s){
      self.currentOLView="ioviewer";
      self.showOverlay.show=true;
    });
    this.$eventBus.$on("addobj", function(s){
      self.currentOLView="objuploader";
      self.showOverlay.show=true;
    });
    this.$eventBus.$on("objactivation", function(s){
      self.currentOLView="objact";
      self.showOverlay.show=true;
    });
    this.$eventBus.$on('hideOverlay', function (layerid) {
      switch (layerid) {
        case '0':
          self.showOverlay.show = false;
          break;
        case '9':
          self.showOverlay.show = false;
          break;
      }
    });
    this.$eventBus.$on('objAdded', function(obj){
      self.showOverlay.show=false;
      self.$router.go(self.$router.currentRoute)
    });
  },
  methods: {
    login_click: function () {
      console.log(this.currentOLView)
      this.showOverlay.show = true;
      this.currentOLView = 'login';
    },
    doneConfig: function(){
      this.dialog = false
      if(this.server.type=='知识对象库'){
        this.server.shelf='/kos'
      }else {
        this.server.shelf = '/'
      }
      this.servers.push(this.server)
      this.$store.commit('setservers', this.servers)
      this.$store.commit('setcurrentServerIndex', this.servers.length-1)
      this.$router.push('/')
    },
    selectserver: function(index) {
      console.log("Selected Server: "+ index+ "    ===> "+this.servers[index].name)
      this.$store.commit('setcurrentServerIndex', index)
      this.$store.commit('setErrorStatus', '')
      this.server = JSON.parse(JSON.stringify(this.currentserver))
      this.$router.push('/')
    }
  },
  computed:{
    currentserver: function(){
      return this.$store.getters.getCurrentServer
    }
  },
  components: {
    login,
    ioviewer,
    objuploader,
    objact
  }
};
</script>
<style>
.theme--light.v-toolbar {
  background-color: #181818;
}
.noscroll {
  overflow: hidden;
  height:100%;
}
html {
  height:100%;
}
.theme--light.application {
  background: #e6e6e6;
}
.theme--light.primary--text {
  color: #0075bc;
}
.toolbar_title_text {
  color: #fff;
  font-size: 0.85em;
  display: inline;
  padding-left: 12px;
  line-height: 60px;
}
.toolbar_link {
  min-width:140px;
  text-align: center;
  line-height: 60px;
  font-size: 1.15em;
  padding: 0 1em;
  color: #ffff;
  text-decoration: none;
  transition: all ease 0.4s;
}
.toolbar_link:hover {
  background-image: linear-gradient(#4A63F6, #2D62E2);
}
.toolbar_link:first-of-type {
  margin-left: 150px;
}
/* CSS for InfoGrid */
.infogrid {
  background-color: transparent;
  width: 1028px;
  font-family: 'Open Sans', sans-serif;
  padding: 50px 0px;
  margin-top: 60px;
  min-height: 740px;
}
.block_title {
  width: 100%;
  padding-top: 25px;
  padding-bottom: 20px;
}
.line {
  display: inline-block;
  width: 50px;
  padding: 0px 16px;
  vertical-align: middle;
  color: #b3b3b3;
}
.inline {
  display: inline-block;
  margin:0px 0px 0px 50px;
  vertical-align: middle;
}
.line hr {
  border: 1px solid #fff;
  border-bottom-color: #555555;
  color: #fff;
}
.infoblock {
  padding-top: 10px;
  padding-bottom: 30px;
}
.block_info {
  line-height: 2em;
}
h4 {
  color: #555555;
  font-weight: 400;
  font-size: 14px;
  padding: 16px 16px;
  margin: 0 auto;
}
a {
  text-decoration: none;
}
::-webkit-scrollbar {  width: 10px;  }
::-webkit-scrollbar-track {  -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.1);  border-radius: 0px; }
::-webkit-scrollbar-thumb {   border-radius: 0px; }
::-webkit-scrollbar-track {   background-color: #fff;  } /* the new scrollbar will have a flat appearance with the set background color */
::-webkit-scrollbar-thumb {   background-color: #d5d5d5; } /* this will style the thumb, ignoring the track */
::-webkit-scrollbar-button {  display:none;  background-color: #d5d5d5;} /* optionally, you can style the top and the bottom buttons (left and right for horizontal bars) */
::-webkit-scrollbar-corner {  background-color: #fff;  } /* if both the vertical and the horizontal bars appear, then perhaps the right bottom corner also needs to be styled */
</style>
