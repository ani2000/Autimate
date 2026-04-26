<template>
  <div class="find-page">
    <h2>Find the Alphabet</h2>
    <div v-if="currentImage">
      <img :src="currentImage" alt="guess letter" />
      <input type="text" v-model="userAnswer" placeholder="Enter the letter" />
      <button @click="checkAnswer">Submit</button>
      <p v-if="feedback">{{ feedback }}</p>
    </div>
    <button v-if="endGame" @click="restartGame">Replay</button>
    <button v-if="endGame" @click="backToMenu">Main Menu</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      letters: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''),
      images: [],
      currentImageIndex: 0,
      userAnswer: '',
      score: 0,
      feedback: '',
      endGame: false
    };
  },
  computed: {
    currentImage() {
      return this.images[this.currentImageIndex];
    }
  },
  mounted() {
    this.loadRandomImages();
  },
  methods: {
    loadRandomImages() {
      this.letters.forEach((letter, index) => {
        this.images.push({
          letter,
          image: require(`/funalp/asset${index + 1}/10.png`)
        });
      });
      this.randomizeImage();
    },
    randomizeImage() {
      const randomIndex = Math.floor(Math.random() * this.images.length);
      this.currentImageIndex = randomIndex;
    },
    checkAnswer() {
      const correctAnswer = this.images[this.currentImageIndex].letter.toLowerCase();
      if (this.userAnswer.toLowerCase() === correctAnswer) {
        this.score += 10;
        this.feedback = 'Correct!';
      } else {
        this.feedback = `Wrong! The correct answer was ${correctAnswer.toUpperCase()}`;
      }
      if (this.score >= 100) {
        this.endGame = true;
      } else {
        this.randomizeImage();
      }
      this.userAnswer = '';
    },
    restartGame() {
      this.score = 0;
      this.endGame = false;
      this.randomizeImage();
    },
    backToMenu() {
      this.$router.push('/dashboard/therapy/funalp');
    }
  }
};
</script>

<style scoped>
.find-page img {
  max-width: 100%;
}
</style>
