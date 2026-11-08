/** 停止容器内所有 HTML 视频的播放并释放资源 */
export function stopHtmlMediaPlayback(root: ParentNode | null | undefined) {
  if (!root) return
  root.querySelectorAll('video').forEach((video) => {
    video.pause()
    video.querySelectorAll('source').forEach((source) => {
      source.removeAttribute('src')
    })
    video.removeAttribute('src')
    video.load()
  })
}
