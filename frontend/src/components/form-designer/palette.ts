import type { FormComponentType, FormFieldType } from '@/types/form-designer/schema'

export interface PaletteItem {
  key: string
  /** useGenI18n page key */
  labelKey: string
  category: 'basic' | 'layout'
  /** Element Plus icon name (PascalCase) */
  icon: string
  /** basic field controls */
  componentType?: FormComponentType
  fieldType?: FormFieldType
  /** layout presentation nodes */
  layoutKind?: 'row' | 'card' | 'divider'
}

export const FORM_DESIGNER_PALETTE: PaletteItem[] = [
  {
    key: 'input',
    componentType: 'input',
    fieldType: 'string',
    labelKey: 'paletteInput',
    category: 'basic',
    icon: 'EditPen',
  },
  {
    key: 'textarea',
    componentType: 'textarea',
    fieldType: 'string',
    labelKey: 'paletteTextarea',
    category: 'basic',
    icon: 'Document',
  },
  {
    key: 'richText',
    componentType: 'richText',
    fieldType: 'richText',
    labelKey: 'paletteRichText',
    category: 'basic',
    icon: 'Edit',
  },
  {
    key: 'number',
    componentType: 'number',
    fieldType: 'int',
    labelKey: 'paletteNumber',
    category: 'basic',
    icon: 'Histogram',
  },
  {
    key: 'select',
    componentType: 'select',
    fieldType: 'string',
    labelKey: 'paletteSelect',
    category: 'basic',
    icon: 'ArrowDown',
  },
  {
    key: 'radio',
    componentType: 'radio',
    fieldType: 'string',
    labelKey: 'paletteRadio',
    category: 'basic',
    icon: 'CircleCheck',
  },
  {
    key: 'date',
    componentType: 'date',
    fieldType: 'date',
    labelKey: 'paletteDate',
    category: 'basic',
    icon: 'Calendar',
  },
  {
    key: 'datetime',
    componentType: 'datetime',
    fieldType: 'datetime',
    labelKey: 'paletteDatetime',
    category: 'basic',
    icon: 'Clock',
  },
  {
    key: 'dateRange',
    componentType: 'dateRange',
    fieldType: 'dateRange',
    labelKey: 'paletteDateRange',
    category: 'basic',
    icon: 'Calendar',
  },
  {
    key: 'switch',
    componentType: 'switch',
    fieldType: 'bool',
    labelKey: 'paletteSwitch',
    category: 'basic',
    icon: 'Switch',
  },
  {
    key: 'checkbox',
    componentType: 'checkbox',
    fieldType: 'bool',
    labelKey: 'paletteCheckbox',
    category: 'basic',
    icon: 'Finished',
  },
  {
    key: 'file',
    componentType: 'file',
    fieldType: 'file',
    labelKey: 'paletteFile',
    category: 'basic',
    icon: 'Paperclip',
  },
  {
    key: 'user',
    componentType: 'user',
    fieldType: 'ref',
    labelKey: 'paletteUser',
    category: 'basic',
    icon: 'User',
  },
  {
    key: 'dept',
    componentType: 'dept',
    fieldType: 'ref',
    labelKey: 'paletteDept',
    category: 'basic',
    icon: 'OfficeBuilding',
  },
  {
    key: 'row',
    layoutKind: 'row',
    labelKey: 'paletteRow',
    category: 'layout',
    icon: 'Grid',
  },
  {
    key: 'card',
    layoutKind: 'card',
    labelKey: 'paletteCard',
    category: 'layout',
    icon: 'Postcard',
  },
  {
    key: 'divider',
    layoutKind: 'divider',
    labelKey: 'paletteDivider',
    category: 'layout',
    icon: 'Minus',
  },
]

export function findPaletteItem(key: string): PaletteItem | undefined {
  return FORM_DESIGNER_PALETTE.find((p) => p.key === key)
}
