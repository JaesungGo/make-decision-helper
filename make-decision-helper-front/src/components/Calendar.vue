<template>
    <div class="calendar-container">
      <FullCalendar
        :options="calendarOptions"
        class="calendar"
      />
      <div v-if="selectedDates.length > 0" class="selected-dates">
        <h4>선택된 날짜</h4>
        <div class="date-chips">
          <div
            v-for="date in selectedDates"
            :key="date"
            class="date-chip"
          >
            {{ formatDate(date) }}
            <button @click="removeDate(date)" class="remove-date">&times;</button>
          </div>
        </div>
      </div>
    </div>
  </template>

  <script setup>
  import { ref, computed } from 'vue'
  import FullCalendar from '@fullcalendar/vue3'
  import dayGridPlugin from '@fullcalendar/daygrid'
  import interactionPlugin from '@fullcalendar/interaction'

  const selectedDates = ref([])

  const calendarOptions = computed(() => ({
    plugins: [dayGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    selectable: true,
    locale: 'ko',
    headerToolbar: {
      left: 'prev',
      center: 'title',
      right: 'next'
    },
    select: handleDateSelect,
    height: 'auto'
  }))

  const handleDateSelect = (selectInfo) => {
    const dateStr = selectInfo.startStr
    if (!selectedDates.value.includes(dateStr)) {
      selectedDates.value.push(dateStr)
    }
  }

  const removeDate = (date) => {
    selectedDates.value = selectedDates.value.filter(d => d !== date)
  }

  const formatDate = (dateStr) => {
    const date = new Date(dateStr)
    return new Intl.DateTimeFormat('ko-KR', {
      month: 'long',
      day: 'numeric',
      weekday: 'short'
    }).format(date)
  }
  </script>

  <style scoped>
  .calendar-container {
    padding: 1rem;
  }

  .calendar {
    background: white;
    border-radius: 8px;
    padding: 1rem;
  }

  .selected-dates {
    margin-top: 1rem;
  }

  .date-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-top: 0.5rem;
  }

  .date-chip {
    background: #e5e7eb;
    padding: 0.5rem;
    border-radius: 4px;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
  }

  .remove-date {
    border: none;
    background: none;
    color: #666;
    cursor: pointer;
  }
  </style>
