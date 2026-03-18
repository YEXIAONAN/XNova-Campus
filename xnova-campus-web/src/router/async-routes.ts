import type { RouteRecordRaw } from 'vue-router'

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/student',
    name: 'StudentRoot',
    component: () => import('@/layout/index.vue'),
    meta: { title: '学生端', icon: 'Reading', roles: ['STUDENT'] },
    children: [
      {
        path: 'exams',
        name: 'StudentExams',
        component: () => import('@/views/student/exams/index.vue'),
        meta: { title: '在线考试', roles: ['STUDENT'] }
      },
      {
        path: 'exams/:publishId',
        name: 'StudentExamDetail',
        component: () => import('@/views/student/exam-detail/index.vue'),
        meta: { title: '考试作答', roles: ['STUDENT'], hidden: true }
      },
      {
        path: 'result',
        name: 'StudentResult',
        component: () => import('@/views/student/result/index.vue'),
        meta: { title: '考试结果', roles: ['STUDENT'] }
      },
      {
        path: 'ranking',
        name: 'StudentRanking',
        component: () => import('@/views/student/ranking/index.vue'),
        meta: { title: '班级/年级排名', roles: ['STUDENT'] }
      }
    ]
  },
  {
    path: '/teacher',
    name: 'TeacherRoot',
    component: () => import('@/layout/index.vue'),
    meta: { title: '教师端', icon: 'User', roles: ['TEACHER'] },
    children: [
      {
        path: 'question',
        name: 'TeacherQuestion',
        component: () => import('@/views/teacher/question/index.vue'),
        meta: { title: '题库管理', roles: ['TEACHER'] }
      },
      {
        path: 'paper',
        name: 'TeacherPaper',
        component: () => import('@/views/teacher/paper/index.vue'),
        meta: { title: '试卷管理', roles: ['TEACHER'] }
      },
      {
        path: 'import',
        name: 'TeacherImport',
        component: () => import('@/views/teacher/import/index.vue'),
        meta: { title: 'TXT导题', roles: ['TEACHER'] }
      },
      {
        path: 'review',
        name: 'TeacherReviewPending',
        component: () => import('@/views/teacher/review-pending/index.vue'),
        meta: { title: '待批改', roles: ['TEACHER'] }
      },
      {
        path: 'scores',
        name: 'TeacherScores',
        component: () => import('@/views/teacher/scores/index.vue'),
        meta: { title: '成绩管理', roles: ['TEACHER'] }
      },
      {
        path: 'stats',
        name: 'TeacherStats',
        component: () => import('@/views/teacher/stats/index.vue'),
        meta: { title: '考试统计', roles: ['TEACHER'] }
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminRoot',
    component: () => import('@/layout/index.vue'),
    meta: { title: '管理员', icon: 'Setting', roles: ['ADMIN'] },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      }
    ]
  }
]
