import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotifyStore = defineStore('notify', () => {
  const visible = ref(false)
  const message = ref('')
  const color = ref('error')
  const timeout = ref(4000)

  function show(msg: string, snackColor = 'error', duration = 4000) {
    message.value = msg
    color.value = snackColor
    timeout.value = duration
    visible.value = true
  }

  function success(msg: string) { show(msg, 'success') }
  function error(msg: string) { show(msg, 'error') }
  function warning(msg: string) { show(msg, 'warning') }
  function info(msg: string) { show(msg, 'info') }

  return { visible, message, color, timeout, show, success, error, warning, info }
})
