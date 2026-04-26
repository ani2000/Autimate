<template>
  <div class="learn-page">
    <h2>Let's Learn Alphabets</h2>
    <div class="alphabet-options">
      <button v-for="letter in letters" :key="letter" @click="showImages(letter)">
        {{ letter }}
      </button>
    </div>

    <div v-if="selectedLetter">
      <h3>Learning {{ selectedLetter }}</h3>
      <div class="image-container">
        <img :src="currentImage" alt="alphabet" />
        <div class="navigation">
          <button @click="prevImage">Previous</button>
          <button @click="nextImage">Next</button>
        </div>
        <button @click="endLearning">End</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      letters: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''),
      selectedLetter: null,
      images: [],
      currentImageIndex: 0
    };
  },
  computed: {
    currentImage() {
      return this.images[this.currentImageIndex];
    }
  },
  methods: {
    showImages(letter) {
      this.selectedLetter = letter;
      this.loadImagesForLetter(letter);
    },
    loadImagesForLetter(letter) {
      const assetNumber = this.letters.indexOf(letter) + 1;
      this.images = [
        require(`/funalp/asset${assetNumber}/10.png`),
        require(`/funalp/asset${assetNumber}/11.png`),
        require(`/funalp/asset${assetNumber}/12.png`)
      ];
      this.currentImageIndex = 0;
    },
    nextImage() {
      if (this.currentImageIndex < this.images.length - 1) {
        this.currentImageIndex++;
      }
    },
    prevImage() {
      if (this.currentImageIndex > 0) {
        this.currentImageIndex--;
      }
    },
    endLearning() {
      this.$router.push('/dashboard/therapy/funalp');
    }
  }
};
</script>

<style scoped>
.alphabet-options button {
  margin: 5px;
  padding: 10px;
  font-size: 16px;
}
.image-container img {
  max-width: 100%;
  height: auto;
}
</style>
