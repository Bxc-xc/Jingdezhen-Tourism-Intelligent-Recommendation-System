<template>
  <div class="mosaic">
    <div class="main" v-if="images && images.length">
      <img :src="images[0]" alt="main" @click="$emit('preview', 0)" />
    </div>
    <div class="thumbs" v-if="images && images.length > 1">
      <div
        class="thumb"
        v-for="(img, idx) in smallImages"
        :key="idx"
        @click="$emit('preview', idx + 1)"
      >
        <img :src="img" :alt="`thumb-${idx+1}`" />
        <div v-if="idx === maxThumbs - 1 && remainCount > 0" class="more">
          +{{ remainCount }}
        </div>
      </div>
    </div>
  </div>
  </template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  maxThumbs: {
    type: Number,
    default: 4
  }
})
defineEmits(['preview'])

const maxThumbs = computed(() => props.maxThumbs)
const smallImages = computed(() => props.images.slice(1, 1 + maxThumbs.value))
const remainCount = computed(() => Math.max(0, props.images.length - 1 - maxThumbs.value))
</script>

<style scoped>
@import '../../styles/tokens.css';

.mosaic {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-12);
  margin-bottom: var(--space-16);
}
.main {
  border-radius: var(--radius-16);
  overflow: hidden;
  position: relative;
  box-shadow: var(--shadow-sm);
}
.main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .25s ease;
  cursor: zoom-in;
}
.main:hover img { transform: scale(1.02); }
.thumbs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-auto-rows: 1fr;
  gap: var(--space-12);
}
.thumb {
  position: relative;
  border-radius: var(--radius-12);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: zoom-in;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .25s ease, filter .2s ease;
}
.thumb:hover img { transform: scale(1.03); }
.more {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,.45);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}
</style>

