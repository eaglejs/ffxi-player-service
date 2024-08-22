<template>
  <div class="card">
    <div class="card-header">
      <h3 class="p-0 m-0">Chat</h3>
    </div>
    <div ref="chatLogEl" class="card-body chat-log">
      <pre v-for="item in chatLog" :key="item.timeStamp">
<code :class="chatColor(item?.messageType)" >{{ `${item?.message}` }}</code>
      </pre>
      <section ref="lastChildEl" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';

const props = defineProps<{
  chatLog: { messageType: string; message: string; timeStamp: string }[]
}>();

const chatLogEl = ref<HTMLElement | undefined>();
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

onMounted(() => {
  if (props.chatLog.length === 0) return;
  lastChildEl.value?.scrollIntoView({
    behavior: 'smooth',
  });
});

watch(() => props.chatLog, () => {
  if (props.chatLog.length <= 10) return;
  setTimeout(() => {
    lastChildEl.value?.scrollIntoView({
      behavior: 'smooth',
    });
  }, 1000);
});

</script>
<style scoped lang="scss">

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
