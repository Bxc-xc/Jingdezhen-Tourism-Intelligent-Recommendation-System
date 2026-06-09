<template>
  <svg
    v-if="paths.length"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    class="brand-icon"
    aria-hidden="true"
  >
    <path
      v-for="(d, idx) in paths"
      :key="idx"
      :d="d"
      stroke="currentColor"
      stroke-width="1.7"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 }
})

const icons = {
  spark: [
    'M12 2l1.2 5.1L18 9l-4.8 1.9L12 16l-1.2-5.1L6 9l4.8-1.9L12 2z',
    'M19.5 13.2l.6 2.4 2.4.6-2.4.6-.6 2.4-.6-2.4-2.4-.6 2.4-.6.6-2.4z'
  ],
  route: [
    'M6 6.5a2.5 2.5 0 105 0 2.5 2.5 0 10-5 0z',
    'M13 17.5a2.5 2.5 0 105 0 2.5 2.5 0 10-5 0z',
    'M8.4 8.7c0 5.2 7.2 1.4 7.2 6.6'
  ],
  pin: [
    'M12 21s6-5.2 6-11a6 6 0 10-12 0c0 5.8 6 11 6 11z',
    'M10.5 10a1.5 1.5 0 103 0 1.5 1.5 0 10-3 0z'
  ],
  chat: [
    'M6.5 18.5L4 20v-9a7 7 0 017-7h2a7 7 0 017 7v1a7 7 0 01-7 7H9a7.6 7.6 0 01-2.5-.5z',
    'M8 11h8',
    'M8 14h5'
  ],
  shield: [
    'M12 2l7 4v6c0 5-3.3 9-7 10-3.7-1-7-5-7-10V6l7-4z',
    'M9.2 12l2 2 3.6-4'
  ],
  phone: [
    'M8.5 3.5h7a1.5 1.5 0 011.5 1.5v14a1.5 1.5 0 01-1.5 1.5h-7A1.5 1.5 0 017 19V5a1.5 1.5 0 011.5-1.5z',
    'M10 6h4',
    'M12 18.2h.01'
  ],
  key: [
    'M7.5 14.5a4 4 0 116.6-3H22v3h-2v2h-2v2h-3.2a4 4 0 01-7.3-4z',
    'M7.5 14.5h.01'
  ]
}

const paths = computed(() => icons[props.name] || [])
</script>

<style scoped>
.brand-icon {
  display: inline-block;
  vertical-align: -0.12em;
}
</style>

