<template>
  <v-container grid-list-md text-xs-center>
    <v-layout row wrap justify-center>
      <div class="infogrid">
        <div class="infoblock">
          <div class="block_title title font-weight-light">
            <div class="line">
              <hr />
            </div>我们是谁
            <div class="line">
              <hr />
            </div>
          </div>
          <div class="subheading font-weight-thin block_info">
            为了让新的健康医学知识能够更快地被人们使用，美国密歇根大学医学院开发了 K-grid 网站，用于存储和管理大量快速变化的可计算医学知识，让有需要的人们随时随地都能接触到这些知识。
            <br />作为中国 MCBK 行动（Moillizing Computable Biomedical Knowledge，推动医学知识可计算行动）的领先尝试，中国可计算医学知识智能应用平台由北京大学健康医疗大数据国家研究院发起，浙江省北大信息技术高等研究院智慧医疗研究中心提供技术支持。
          </div>
        </div>
        <div class="infoblock">
          <div class="block_title title font-weight-light">
            <div class="line">
              <hr />
            </div>知识对象库是什么
            <div class="line">
              <hr />
            </div>
          </div>
          <div
            class="subheading font-weight-thin block_info"
          >知识对象库（K-grid library）是管理可计算医学知识的开源网站，您可以查阅并扩展代码，将其作用最大化。知识对象库完全免费，由美国密歇根大学发起，通过北京大学健康医疗大数据国家研究院、浙江省北大信息技术高等研究院智慧医疗研究中心在中国进行本地化开发与推广。</div>
        </div>
        <div class="infoblock">
          <div class="block_title title font-weight-light">
            <div class="line">
              <hr />
            </div>联系我们
            <div class="line">
              <hr />
            </div>
          </div>
          <div class="subheading font-weight-thin block_info">
            单位：浙江省北大信息技术高等研究院 智慧医疗研究中心
            <br />邮箱：zhyl@aiit.org.cn
            <br />地址：中国浙江省杭州市萧山区永晖路230号
          </div>
        </div>
        <div class="infoblock" v-if="isAdmin">
          <div class="block_title title font-weight-light">
            <div class="line">
              <hr />
            </div>Build Information
            <div class="line">
              <hr />
            </div>
          </div>
          <div class="subheading font-weight-thin block_info">
            Version: {{version}}
            <br />
            Build TIme: {{buildtime}}
            <br />
          </div>
        </div>
      </div>
    </v-layout>
  </v-container>
</template>
<script>
export default {
  name: "about",
  data: function() {
    return {};
  },
  created: function() {},
  mounted: function() {},
  computed: {
    isLoggedIn: function() {
      return this.$store.getters.isLoggedIn;
    },
    libraryname: function() {
      return this.$store.getters.getLibraryName;
    },
    version: function() {
      return this.$store.getters.getVersion;
    },
    buildtime: function() {
      return this.$store.getters.getBuildTime;
    },
    isAdmin: function() {
      return this.$store.getters.isAdmin;
    }
  }
};
</script>
