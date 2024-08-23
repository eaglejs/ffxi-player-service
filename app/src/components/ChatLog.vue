<template>
  <div class="card">
    <div class="card-header">
      <div class="d-flex">
        <h3 class="p-0 m-0">Chat</h3>
        <section class="flex-grow-1">
          <button class="btn btn-sm arrow-btns float-end" @click="scrollToFirstChild('smooth')">
            <GenIcon :icon="mdiChevronUp" size="lg" />
          </button>
          <button class="btn btn-sm arrow-btns float-end" @click="scrollToLastChild('smooth')">
            <GenIcon :icon="mdiChevronDown" size="lg" />
          </button>
        </section>
      </div>
    </div>
    <div ref="chatLogEl" class="card-body chat-log">
      <section ref="firstChildEl" />
      <pre v-for="item in chatLog" :key="item.timeStamp" encoding="shift-jis" font="Meiryo">
<code :class="chatColor(item?.messageType)" >{{ `${item?.message}` }}</code>
      </pre>
      <section ref="lastChildEl" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { mdiChevronUp, mdiChevronDown } from '@mdi/js';
import GenIcon from '@/components/GenIcon.vue';

const props = defineProps<{
  chatLog: { messageType: string; message: string; timeStamp: string }[]
}>();

const chatLogEl = ref<HTMLElement | undefined>();
const firstChildEl = ref<HTMLElement | undefined>();
const lastChildEl = ref<HTMLElement | undefined>();

const chatColor = (messageType: string) => {
  switch (messageType?.toLowerCase()) {
    case 'party':
      return 'party'
    case 'linkshell':
      return 'linkshell'
    case 'shout':
      return 'shout'
    case 'yell':
      return 'shout'
    case 'unity':
      return 'unity'
    case 'tell':
      return 'tell'
    default:
      return 'say'
  }
};

const toLocalTime = (timeStamp: string) => {
  const date = new Date(timeStamp);
  return date.toLocaleString();
};

const scrollToLastChild = (behavior: ScrollBehavior) => {
  chatLogEl.value?.scrollTo({
    top: chatLogEl.value.scrollHeight,
    behavior
  });
};

const scrollToFirstChild = (behavior: ScrollBehavior) => {
  chatLogEl.value?.scrollTo({
    top: 0,
    behavior
  });
};

onMounted(() => {
  if (props.chatLog.length === 0) return;
  scrollToLastChild('instant');
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      scrollToLastChild('instant');
    }
  })
});

watch(props.chatLog, () => {
  if (props.chatLog.length <= 10) return;
  setTimeout(() => {
    scrollToLastChild('smooth');
  }, 1000);
});

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
    color: var(--bs-primary-bg-subtle);
  }
}

.chat-log {
  background-color: #031633;
  max-height: 500px;
  overflow-y: auto;
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
  color: var(--chat-log-brown);
}
</style>
