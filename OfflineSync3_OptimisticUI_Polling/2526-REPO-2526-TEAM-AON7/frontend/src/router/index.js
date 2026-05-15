import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import EventOverviewView from "../views/EventOverviewView.vue";
import EventLayoutView from "../views/EventLayoutView.vue";
import DashboardView from "../views/DashboardView.vue";
import BedrijvenView from "../views/BedrijvenView.vue";
import StudentenOverzichtView from "../views/StudentenOverzichtView.vue";
import RegisterInviteView from "../views/RegisterInviteView.vue";
import StudentProfileView from "../views/StudentProfileView.vue";
import StudentQRView from "../views/StudentQRView.vue";
import StudentBedrijvenView from "../views/StudentBedrijvenView.vue";
import StudentScansView from "../views/StudentScansView.vue";
import { useEventStore } from "../stores/eventStore.js";
import RegisterView from "@/views/RegisterView.vue";
import StudentEventView from "@/views/StudentEventView.vue";
import BedrijfEventView from "@/views/BedrijfEventView.vue";
import BedrijfScanView from '@/views/BedrijfScanView.vue'
import BedrijfStudentenView from '@/views/BedrijfStudentenView.vue'
import BedrijfProfileView from '@/views/BedrijfProfileView.vue'
import ScanConfirmView from "@/views/ScanConfirmView.vue";
import BedrijfStudentInfo from "@/views/BedrijfStudentInfo.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "login",
      component: LoginView,
    },
    {
      path: "/register",
      name: "register",
      component: RegisterView
    },
    {
      path: "/events",
      name: "events",
      component: EventOverviewView,
    },
    {
      path: "/student/events",
      name: "student-eventview",
      component: StudentEventView,
    },
    {
      path: "/student/profiel",
      name: "student-profiel",
      component: StudentProfileView,
    },
    {
      path: "/bedrijf/events",
      name: "bedrijf-events",
      component: BedrijfEventView
    },
    {
      path: "/student/qr",
      name: "student-qr",
      component: StudentQRView,
    },
    {
      path: "/student/bedrijven",
      name: "student-bedrijven",
      component: StudentBedrijvenView,
    },
    {
      path: "/student/scans",
      name: "student-scans",
      component: StudentScansView,
    },
    {
      path: "/events/:id",
      component: EventLayoutView,
      meta: { requiresEvent: true },
      children: [
        {
          path: "dashboard",
          name: "event-dashboard",
          component: DashboardView,
        },
        {
          path: "bedrijven",
          name: "event-bedrijven",
          component: BedrijvenView,
        },
        {
          path: "studenten",
          name: "event-studenten",
          component: StudentenOverzichtView,
        },
      ],
    },
    {
      path: "/register/invite/:token",
      name: "register-invite",
      component: RegisterInviteView,
    },
    {
      path: "/dashboard",
      redirect: "/events",
    },
    {
      path: '/bedrijf/scan',
      name: 'bedrijf-scan',
      component: BedrijfScanView,
    },
    {
      path: '/bedrijf/studenten',
      name: 'bedrijf-studenten',
      component: BedrijfStudentenView,
    },
    {
      path: '/bedrijf/profiel',
      name: 'bedrijf-profiel',
      component: BedrijfProfileView,
    },
    {
      path: '/scan-confirm',
      name: 'scan-confirm',
      component: ScanConfirmView,
    },
    {
      path: '/bedrijf/studentinfo/:email',
      name: 'bedrijf-studentinfo',
      component: BedrijfStudentInfo,
    }
  ],
});

router.beforeEach((to) => {
  if (to.meta.requiresEvent) {
    const { hasSelectedEvent } = useEventStore();
    if (!hasSelectedEvent()) {
      return { name: "events" };
    }
  }
  return true;
});

export default router;
