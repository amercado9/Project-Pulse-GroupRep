<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="code">403</h1>
      <h2>Access Forbidden</h2>
      <p>You don't have permission to access this page.</p>
      <v-btn color="primary" @click="$router.push('/')">Go to Home</v-btn>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;

  .error-content {
    text-align: center;
    padding: 40px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

    .code {
      font-size: 80px;
      font-weight: 800;
      color: #f44336;
      margin: 0;
      line-height: 1;
    }

    h2 { margin: 8px 0 4px; color: #303133; }
    p { color: #909399; margin-bottom: 24px; }
  }
}
</style>
