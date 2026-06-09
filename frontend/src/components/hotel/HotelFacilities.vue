<template>
  <section class="hotel-card">
    <h2 class="card-title">酒店设施</h2>
    <div class="grid">
      <div v-for="(f, i) in displayList" :key="i" class="item">
        <span class="dot"></span>
        <span class="txt">{{ f || '暂无信息' }}</span>
      </div>
    </div>
    <el-button v-if="moreCount > 0" link type="primary" @click="expanded = !expanded">
      {{ expanded ? '收起' : `查看全部设施（+${moreCount}）` }}
    </el-button>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  facilities: { type: Array, default: () => [] }
})
const expanded = ref(false)
const baseCount = 8

const displayList = computed(() => {
  const list = props.facilities?.length ? props.facilities : ['暂无信息']
  return expanded.value ? list : list.slice(0, baseCount)
})
const moreCount = computed(() => Math.max(0, (props.facilities?.length || 0) - baseCount))
</script>

<style scoped>
.hotel-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef1f6;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
}
.card-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1f2937;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px 14px;
  margin-bottom: 6px;
}
.item {
  display: flex; align-items: center; gap: 8px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 8px 10px;
  color: #4b5563;
}
.dot { width: 6px; height: 6px; border-radius: 50%; background: #009688; display: inline-block; }
</style>

