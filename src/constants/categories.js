export const EXPENSE_CATEGORIES = [
  { id: 'food', name: '식비', icon: '🍚', color: '#FF6B6B' },
  { id: 'transport', name: '교통', icon: '🚌', color: '#4ECDC4' },
  { id: 'shopping', name: '쇼핑', icon: '🛍️', color: '#45B7D1' },
  { id: 'entertainment', name: '여가', icon: '🎮', color: '#96CEB4' },
  { id: 'health', name: '의료/건강', icon: '💊', color: '#FFEAA7' },
  { id: 'education', name: '교육', icon: '📚', color: '#DDA0DD' },
  { id: 'housing', name: '주거', icon: '🏠', color: '#F0A500' },
  { id: 'communication', name: '통신', icon: '📱', color: '#74B9FF' },
  { id: 'beauty', name: '미용', icon: '💄', color: '#FD79A8' },
  { id: 'other_expense', name: '기타', icon: '💸', color: '#B2BEC3' },
];

export const INCOME_CATEGORIES = [
  { id: 'salary', name: '급여', icon: '💰', color: '#00B894' },
  { id: 'freelance', name: '부업/프리랜서', icon: '💻', color: '#6C5CE7' },
  { id: 'investment', name: '투자/이자', icon: '📈', color: '#FDCB6E' },
  { id: 'gift', name: '용돈/선물', icon: '🎁', color: '#E17055' },
  { id: 'other_income', name: '기타', icon: '💵', color: '#B2BEC3' },
];

export const ALL_CATEGORIES = [...INCOME_CATEGORIES, ...EXPENSE_CATEGORIES];

export const getCategoryById = (id) => {
  return ALL_CATEGORIES.find(cat => cat.id === id) || { name: '기타', icon: '💰', color: '#B2BEC3' };
};
