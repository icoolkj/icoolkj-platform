import path from 'node:path'
import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import { shellConfig, shouldEnableBaiduHm } from './shell.config'

function buildBaiduHmScript(hmId: string): string {
  return `<script>
var _hmt = _hmt || [];
(function() {
  var hm = document.createElement("script");
  hm.src = "https://hm.baidu.com/hm.js?${hmId}";
  var s = document.getElementsByTagName("script")[0];
  s.parentNode.insertBefore(hm, s);
})();
</script>`
}

/**
 * open DevTools → about:blank（阈值来自 shell.config.json → devtools）。
 */
function buildDevtoolsGuardScript(): string {
  const { gapPx, debugLagMs, pollMs } = shellConfig.devtools
  const accessToken = shellConfig.storageKeys.accessToken
  const refreshToken = shellConfig.storageKeys.refreshToken
  return `<script>
(function () {
  var GAP = ${gapPx};
  var DEBUG_LAG_MS = ${debugLagMs};
  var POLL_MS = ${pollMs};
  function blank() {
    try {
      localStorage.removeItem(${JSON.stringify(accessToken)});
      localStorage.removeItem(${JSON.stringify(refreshToken)});
      sessionStorage.clear();
    } catch (e) {}
    location.replace('about:blank');
  }
  function sizeOpen() {
    return (window.outerWidth - window.innerWidth) > GAP
      || (window.outerHeight - window.innerHeight) > GAP;
  }
  function debugOpen() {
    var t0 = performance.now();
    try { (function () {}).constructor('debugger')(); } catch (e) {}
    return performance.now() - t0 > DEBUG_LAG_MS;
  }
  setInterval(function () {
    if (sizeOpen() || debugOpen()) blank();
  }, POLL_MS);
})();
</script>`
}

function htmlShellInject(mode: string): Plugin {
  return {
    name: 'html-shell-inject',
    transformIndexHtml(html) {
      const hmId = shellConfig.baiduHmId?.trim()
      const baiduScript =
        hmId && shouldEnableBaiduHm(mode === 'production') ? buildBaiduHmScript(hmId) : ''
      const devtoolsScript = shellConfig.blockDevtools === true ? buildDevtoolsGuardScript() : ''
      return html
        .split('%SHELL_LANG%')
        .join(shellConfig.defaultLang)
        .split('%SHELL_TITLE%')
        .join(shellConfig.defaultTitle)
        .split('%LOCALE_STORAGE_KEY%')
        .join(shellConfig.storageKeys.locale)
        .split('%BAIDU_HM_SCRIPT%')
        .join(baiduScript)
        .split('%DEVTOOLS_GUARD_SCRIPT%')
        .join(devtoolsScript)
    },
  }
}

export default defineConfig(({ mode }) => ({
  plugins: [vue(), htmlShellInject(mode)],
  resolve: {
    alias: {
      '@': path.resolve(process.cwd(), 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
}))
