<template>
  <v-row class="d-flex justify-space-around">
    <v-col
      class="d-flex justify-space-around align-center"
      v-for="article in articles"
      :key="article.id"
      xl4
      lg4
      md6
      sm12
      xs12
    >
      <div class="content">
        <v-card class="d-inline-block my-3" :min-width="moblieWidth">
          <v-img class="white--text align-end" height="168px" :src="article.imageSrc"></v-img>
          <v-card-title
            @click="moveToArticle(article)"
            class="card-title justify-center"
          >{{ article.title }}</v-card-title>

          <v-card-text class="card-text text--primary">
            <div class="text-center">
              {{ article.content.slice(0, 50)
              }}{{ article.content.length > 50 ? "..." : "" }}
            </div>
          </v-card-text>
          <v-footer class="d-flex justify-space-around">
            <div>
              <b>{{ article.editdate | dateToString }}</b>
            </div>
            <div>
              <b>|</b>
            </div>
            <div>
              <b>{{ article.editornickname }}</b>
            </div>
          </v-footer>

          <!-- <v-card-actions class="justify-space-around">
              <v-btn color="orange" icon @click="copyLink(article.id)">
                <v-icon middle color>mdi-share</v-icon>
              </v-btn>
              <v-btn
                color="red accent-4"
                icon
                v-if="article.isLiked"
                @click="changeLiked(article.id)"
              >
                <v-icon middle color="red accent-4">mdi-heart</v-icon>
              </v-btn>
              <v-btn color="red accent-4" icon v-else @click="changeLiked(article.id)">
                <v-icon middle color="red accent-4">mdi-heart-outline</v-icon>
              </v-btn>
          </v-card-actions>-->
        </v-card>
      </div>
    </v-col>
  </v-row>
</template>

<script>
import { mapActions } from "vuex";
export default {
  props: { data: Array },
  computed: {
    articles() {
      return this.data;
    },
    moblieWidth() {
      return window.innerWidth <= 500 ? "220px" : "290px";
    },
  },
  filters: {
    dateToString(date) {
      try {
        return date.slice(0, 10);
      } catch (e) {
        console.log("");
      }
    },
  },
  methods: {
    copyLink(id) {
      const copyURL = document.createElement("input");
      copyURL.value = document.URL + id;
      document.body.appendChild(copyURL);
      copyURL.select();
      document.execCommand("copy");
      document.body.removeChild(copyURL);
      alert("링크가 복사되었습니다!");
    },

    ...mapActions(["setCurrentArticleId"]),

    moveToArticle(article) {
      this.setCurrentArticleId(article.articleid);
      this.$router.push({
        name: "Article",
        params: { articleId: article.articleid },
      });
    },
  },
};
</script>

<style scoped>
.content {
  width: calc(100% - 2rem);
  transition-duration: 0.6s;
}

.content:hover {
  transform: translate3d(-4px, -4px, -4px);
  box-shadow: rgba(0, 0, 0, 0.08);
}
</style>