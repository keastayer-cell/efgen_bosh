<template>
  <div class="app-shell">
    <header class="topbar">
      <router-link class="brand" to="/">Efgen Bosh</router-link>
      <span class="brand-sub">Управление кузовным автосервисом</span>
    </header>

    <main class="content-wrap">
      <router-view />
    </main>

    <footer class="global-footer">
      <span>Efgen Bosh</span>
      <span>Внутренняя система автосервиса</span>
    </footer>
  </div>
</template>
