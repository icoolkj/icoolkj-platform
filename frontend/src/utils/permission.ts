export function hasPermission(permissions: string[], value: string | string[]) {
  if (!permissions.length) {
    return false
  }
  const required = Array.isArray(value) ? value : [value]
  return required.some((perm) => permissions.includes(perm))
}
