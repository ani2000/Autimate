<script>
import AdminSidebar from './AdminSidebar.vue';

export default {
  components: {
    AdminSidebar,
  },
  data() {
    return {
      isModalVisible: false,
      isTestingModel: true,
    };
  },
  methods: {
    showModal() {
      this.isModalVisible = true;
      this.isTestingModel = false;
    },
    showAutismTestingModal() {
      this.isModalVisible = true;
      this.isTestingModel = true;
    }
  },
};

</script>

<template>

  <div class="wrapper">
    <AdminSidebar @openModal="showModal" @showAutismTestingModal="showAutismTestingModal"/>

    <div class="content_renderer">
      <router-view></router-view>
      
      <Modal v-if="isModalVisible" @close="isModalVisible = false" :isTestingModel="isTestingModel" />
    </div>
  </div>
</template>

<style lang="css" scoped>
.wrapper {
  /* width: calc(100vw - 30px); */
  /* min-height: calc(100vh-10px); */
  min-width: 100vw;
  height: calc(100vh - 100px);
  position: fixed;
  top: 0;
  left: 0;
  padding-top: 5px;
  padding-left: 15px;
  display: flex;
  flex-direction: column;
}

.content_renderer {
  /* width: calc(100vw - 230px - 15px - 30px); */
  /** sidebar is 230px + 25px padding from it. */
  /* height: calc(100vh-10px); */

  width: 99vw;
  margin-left: 15px;
  border-radius: var(--br-base);
  background-color: var(--color-whitesmoke-100);
  min-height: calc(98vh - 50px);
}
</style>