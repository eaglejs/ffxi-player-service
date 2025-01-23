<template>
  <div class="card">
    <div class="card-header">
      <div class="d-flex">
        <h3 class="p-0 m-0">Chat</h3>
        <section class="flex-grow-1">
          <section class="form-check form-switch float-end">
            <label class="form-check-label" for="flexSwitchCheckChecked">Timestamp</label>
            <input
              class="form-check-input"
              type="checkbox"
              role="switch"
              id="flexSwitchCheckChecked"
              @click="toggleTimeStamp"
              :checked="timeStampsEnabled"
            />
          </section>
          <button class="btn btn-sm arrow-btns float-end" @click="scrollToFirstChild('smooth')">
            <GenIcon :icon="mdiChevronUp" size="lg" />
          </button>
          <button class="btn btn-sm arrow-btns float-end" @click="scrollToLastChild('smooth')">
            <GenIcon :icon="mdiChevronDown" size="lg" />
          </button>
          <div class="dropdown float-end">
            <button
              class="btn btn-secondary btn-sm dropdown-toggle"
              type="button"
              data-bs-toggle="dropdown"
              aria-expanded="false"
            >
              {{ chatFilterValue }}
            </button>
            <ul class="dropdown-menu">
              <li><a class="dropdown-item" href="#" @click="setChatFilter('None')">None</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Say')">Say</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Tell')">Tell</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Party')">Party</a></li>
              <li>
                <a class="dropdown-item" href="#" @click="setChatFilter('Linkshell')">Linkshell</a>
              </li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Yell')">Yell</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Unity')">Unity</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Cutscene')">Cutscene</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Drops')">Drops</a></li>
              <li><a class="dropdown-item" href="#" @click="setChatFilter('Obtained')">Obtained</a></li>
            </ul>
          </div>
        </section>
      </div>
    </div>
    <div ref="chatLogEl" v-if="chatLog.length" class="card-body chat-log">
      <section ref="firstChildEl" />
      <pre v-for="item in chatLog" :key="item.timeStamp">
<code :class="chatColor(item?.messageType)"><span v-if="timeStampsEnabled">[{{ toLocalTime(item.timeStamp) }}]</span>{{ `${item?.message}` }}</code>
      </pre>
      <section ref="lastChildEl" />
    </div>
    <div v-else class="card-body chat-log">
      <p class="text-center mt-3">No {{ chatFilterValue }} messages found.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUpdated, ref } from 'vue'
import { mdiChevronUp, mdiChevronDown } from '@mdi/js'
import GenIcon from '@/components/GenIcon.vue'
import { useUserStore } from '@/stores/user'
import { isIPhone, isAndroid } from '@/helpers/utils'

const userStore = useUserStore()
const playerId = parseInt(window.location.pathname.split('/').pop() || '')

const instantFlag = ref(false)
const chatLogEl = ref<HTMLElement | undefined>()
const firstChildEl = ref<HTMLElement | undefined>()
const lastChildEl = ref<HTMLElement | undefined>()
const chatFilterValue = ref<string>('Chat Filter')
const timeStampsEnabled = ref<boolean>(
  localStorage.getItem('timeStampsEnabled') === 'true' || false
)
const autoScrollIsActive = ref<boolean>(true)

const messageTypeMap = {
  party: 'party',
  linkshell: 'linkshell',
  shout: 'shout',
  yell: 'shout',
  unity: 'unity',
  tell: 'tell',
  trial: 'trial',
  say: 'say',
  obtained: 'obtained',
  drops: 'obtained',
};

const chatLog = computed(() => {
  return userStore.chatLog.filter((item: any) => {
    if (chatFilterValue.value === 'None' || chatFilterValue.value === 'Chat Filter') {
      return true
    } else {
      console.log(item.messageType?.toLowerCase(), chatFilterValue.value?.toLowerCase())
      return item.messageType?.toLowerCase() === chatFilterValue.value?.toLowerCase()
    }
  })
})

function chatColor(messageType: string) {
  return messageTypeMap[messageType.toLowerCase() as keyof typeof messageTypeMap] || 'say';
}

const toggleTimeStamp = () => {
  timeStampsEnabled.value = !timeStampsEnabled.value
  localStorage.setItem('timeStampsEnabled', timeStampsEnabled.value.toString())
}

const toLocalTime = (timeStamp: string) => {
  const date = new Date(timeStamp)
  return date.toLocaleString()
}

const scrollToLastChild = (behavior: ScrollBehavior) => {
  autoScrollIsActive.value = true
  if (chatLog.value.length) {
    chatLogEl.value?.scrollTo({
      top: chatLogEl.value.scrollHeight,
      behavior
    })
  }
}

const scrollToFirstChild = (behavior: ScrollBehavior) => {
  autoScrollIsActive.value = false
  chatLogEl.value?.scrollTo({
    top: 0,
    behavior
  })
}

onMounted(() => {
  userStore.fetchChatLog(playerId)
  instantFlag.value = true
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      scrollToLastChild('instant')
    }
  })
  chatLogEl.value?.addEventListener('wheel', handleScrollEvent)
  if (isIPhone() || isAndroid()) {
    chatLogEl.value!.addEventListener('touchmove', handleScrollEvent)
  }
})

const handleScrollEvent = () => {
  if (chatLogEl.value!.scrollTop + chatLogEl.value!.clientHeight < chatLogEl.value!.scrollHeight) {
    autoScrollIsActive.value = false
  } else {
    autoScrollIsActive.value = true
  }
}

function setChatFilter(filter: string) {
  instantFlag.value = true
  if (filter === 'None') {
    chatFilterValue.value = 'Chat Filter'
  } else {
    chatFilterValue.value = filter ?? 'Chat Filter'
  }
  scrollToLastChild('instant')
  setTimeout(() => {
    instantFlag.value = false
  }, 300)
}

onUpdated(() => {
  if (isIPhone() || isAndroid()) {
    const newHeight = chatLogEl.value!.clientHeight === 500 ? 501 : 500
    chatLogEl.value!.setAttribute('style', `max-height: ${newHeight}px;`)
  }
  if (autoScrollIsActive.value) {
    scrollToLastChild(instantFlag.value ? 'instant' : 'smooth')
    instantFlag.value = false
  }
})

</script>
<style scoped lang="scss">

.arrow-btns {
  max-height: 23px;
  display: inline-flex;
  padding: 0 5px;
}

pre {
  text-wrap: wrap;
  margin: -15px;
  padding: 3px 5px;
}

code {
  span {
    color: var(--chat-log-white);
  }
}

.chat-log {
  background-color: var(--chat-log-bg);
  border-bottom-left-radius: 7px;
  border-bottom-right-radius: 7px;
  height: 490px;
  overflow-y: auto;
  p {
    color: var(--chat-log-white);
  }
}

.party {
  color: var(--chat-log-blue);
}

.tell {
  color: var(--chat-log-purple);
}

.linkshell {
  color: var(--chat-log-green);
}

.shout {
  color: var(--chat-log-orange);
}

.unity {
  color: var(--chat-log-red);
}

.say {
  color: var(--chat-log-white);
}

.trial {
  color: var(--chat-log-mint);
}

.obtained {
  color: var(--chat-log-lime);
}

</style>
