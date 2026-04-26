<template>
  <div class="main-menu">
    <div>
      <h1>Fun with Alphabet</h1>
      <br>
      <button @click="startLearn">Learn</button>
      <button @click="startFind">Find</button>
      
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    startLearn() {
      this.$router.push('/dashboard/therapy/funalphabet/learn');
    },
    startFind() {
      this.$router.push('/dashboard/therapy/funalphabet/find');
    }
  }
};
</script>

<style scoped>
.main-menu {
  width: 100%;
  min-height: calc(100dvh - 120px);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 50px;
}
button {
  margin: 20px;
  padding: 10px 20px;
  font-size: 18px;
  cursor: pointer;
}

@media (max-width: 640px) {
  .main-menu {
    min-height: calc(100dvh - 84px);
    padding: 20px 12px;
  }

  button {
    width: 100%;
    max-width: 260px;
    margin: 10px 0;
    font-size: 17px;
  }
}
</style>
