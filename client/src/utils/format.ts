export function mealTypeLabel(type: string) {
  if (type === 'breakfast') return '아침'
  if (type === 'lunch') return '점심'
  if (type === 'dinner') return '저녁'
  if (type === 'snack') return '간식'
  return '기타'
}

export function goalLabel(goal: string) {
  if (goal === 'health') return '건강 유지'
  if (goal === 'diet') return '체중 감량'
  if (goal === 'muscle') return '근육 증가'
  return '사용자 목표'
}

export function genderLabel(gender: string) {
  return gender === 'female' ? '여성' : '남성'
}

export function formatDate(value: string | null | undefined) {
  if (!value) {
    return '-'
  }

  const parts = value.split('-')
  if (parts.length !== 3) {
    return value
  }

  return `${parts[0]}.${parts[1]}.${parts[2]}`
}

export function intValue(value: number | null | undefined) {
  return Math.round(value || 0)
}

export function oneDecimal(value: number | null | undefined) {
  return Math.round((value || 0) * 10) / 10
}
