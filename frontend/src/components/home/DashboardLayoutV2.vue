<template>
  <div class="wrapper">
    <SidebarV2 />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
import SidebarV2 from './SidebarV2.vue';

export default {
  components: { SidebarV2 },
};
</script>

<style scoped>
.wrapper { width: 100%; min-height: 100vh; padding: 10px; display: flex; gap: 12px; background: #f1f5f9; }
.content { flex: 1; min-height: calc(100vh - 20px); border-radius: 14px; background: #f8fafc; overflow: auto; }
</style>
