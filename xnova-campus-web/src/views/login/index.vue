<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="brand">XNova Campus</div>
      <p class="sub">智慧校园管理系统</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item label="身份" prop="loginType">
          <el-select v-model="form.loginType" style="width: 100%">
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const router = useRouter()
const authStore = useAuthStore()
const permissionStore = usePermissionStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: 'student1',
  password: '123456',
  loginType: 'STUDENT'
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  loginType: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

const onSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authStore.login(form)
    await authStore.fetchMe()
    permissionStore.buildRoutes(authStore.roles)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #eef4ff, #f7faff);
}

.login-panel {
  width: 380px;
  padding: 28px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 35, 95, 0.08);
}

.brand {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

.sub {
  color: var(--sub);
  margin-top: 0;
  margin-bottom: 20px;
}
</style>
