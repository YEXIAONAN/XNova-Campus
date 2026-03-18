<template>
  <el-card>
    <template #header>TXT 导题</template>
    <el-upload :auto-upload="false" :limit="1" accept=".txt">
      <el-button type="primary">选择txt文件</el-button>
    </el-upload>
    <p style="margin-top:10px;color:#667085">上传后先预览，再确认导入。</p>
  </el-card>
</template>
