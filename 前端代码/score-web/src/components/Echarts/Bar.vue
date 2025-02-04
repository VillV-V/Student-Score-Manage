<template>
  <div ref="myChartsRef" :class="className" :style="{ height: height, width: width }" :option="option"></div>
</template>

<script setup lang='ts'>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { ECharts, EChartsOption, init } from 'echarts';

// 定义props
interface Props {
  className?: string
  width?: string
  height?: string
  option: EChartsOption
}
const props = withDefaults(defineProps<Props>(), {
  className: 'chart',
  width: '100%',
  height: '100%',
  option: () => ({})
})

const myChartsRef = ref<HTMLDivElement>()
let myChart: ECharts
let timer: number

const initChart = (): void => {
  if (myChart !== undefined) {
    myChart.dispose()
  }
  myChart = init(myChartsRef.value as HTMLDivElement)
  myChart?.setOption(props.option, true)
}

const resizeChart = (): void => {
  timer = setTimeout(() => {
    if (myChart) {
      myChart.resize()
    }
  }, 500);
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  clearTimeout(timer)
  timer = 0
})

watch(props.option, () => {
  initChart()
}, {
  deep: true
})

</script>

<style lang="scss" scoped>
.chart {
  height: 40px;
  margin: 0 auto;
}
</style>
