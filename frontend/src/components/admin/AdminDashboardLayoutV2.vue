<template>
  <div class="layout">
    <AdminSidebarV2 />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script>
import AdminSidebarV2 from './AdminSidebarV2.vue';

export default {
  components: { AdminSidebarV2 },
};
</script>

<style scoped>
.layout { min-height: 100vh; background: #f1f5f9; padding: 10px; display: flex; gap: 12px; }
.content { flex: 1; min-height: calc(100vh - 20px); border-radius: 14px; background: #f8fafc; padding: 16px; overflow: auto; }
@media (max-width: 900px) { .layout { flex-direction: column; } }
</style>
