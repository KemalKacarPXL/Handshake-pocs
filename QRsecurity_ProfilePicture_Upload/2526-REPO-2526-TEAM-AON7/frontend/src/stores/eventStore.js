import { reactive, toRefs } from "vue";

const state = reactive({
  selectedEvent: null,
});

export function useEventStore() {
  const selectEvent = (event) => {
    state.selectedEvent = event;
  };

  const clearEvent = () => {
    state.selectedEvent = null;
  };

  const hasSelectedEvent = () => {
    return state.selectedEvent !== null;
  };

  return {
    ...toRefs(state),
    selectEvent,
    clearEvent,
    hasSelectedEvent,
  };
}
