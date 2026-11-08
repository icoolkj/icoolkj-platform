import { ElDialog, ElDrawer } from 'element-plus'

export function applyElementPlusDefaults() {
  setBooleanPropDefault(ElDialog.props, 'closeOnClickModal', false)
  setBooleanPropDefault(ElDrawer.props, 'closeOnClickModal', false)
}

function setBooleanPropDefault(
  component: { props?: Record<string, unknown> },
  propName: string,
  value: boolean,
) {
  const prop = component.props?.[propName]
  if (prop && typeof prop === 'object' && prop !== null && 'default' in prop) {
    ;(prop as { default: boolean }).default = value
  }
}
