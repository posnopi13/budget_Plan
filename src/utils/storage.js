import AsyncStorage from '@react-native-async-storage/async-storage';

const TRANSACTIONS_KEY = 'transactions';
const BUDGETS_KEY = 'budgets';

// 거래 내역 관련
export const getTransactions = async () => {
  try {
    const data = await AsyncStorage.getItem(TRANSACTIONS_KEY);
    return data ? JSON.parse(data) : [];
  } catch (error) {
    console.error('거래 내역 불러오기 실패:', error);
    return [];
  }
};

export const saveTransaction = async (transaction) => {
  try {
    const transactions = await getTransactions();
    const newTransaction = {
      ...transaction,
      id: Date.now().toString(),
      createdAt: new Date().toISOString(),
    };
    const updated = [newTransaction, ...transactions];
    await AsyncStorage.setItem(TRANSACTIONS_KEY, JSON.stringify(updated));
    return newTransaction;
  } catch (error) {
    console.error('거래 내역 저장 실패:', error);
    throw error;
  }
};

export const deleteTransaction = async (id) => {
  try {
    const transactions = await getTransactions();
    const updated = transactions.filter(t => t.id !== id);
    await AsyncStorage.setItem(TRANSACTIONS_KEY, JSON.stringify(updated));
  } catch (error) {
    console.error('거래 내역 삭제 실패:', error);
    throw error;
  }
};

export const getTransactionsByMonth = async (year, month) => {
  const transactions = await getTransactions();
  return transactions.filter(t => {
    const date = new Date(t.date);
    return date.getFullYear() === year && date.getMonth() + 1 === month;
  });
};

// 예산 관련
export const getBudgets = async () => {
  try {
    const data = await AsyncStorage.getItem(BUDGETS_KEY);
    return data ? JSON.parse(data) : {};
  } catch (error) {
    console.error('예산 불러오기 실패:', error);
    return {};
  }
};

export const saveBudget = async (categoryId, amount) => {
  try {
    const budgets = await getBudgets();
    budgets[categoryId] = amount;
    await AsyncStorage.setItem(BUDGETS_KEY, JSON.stringify(budgets));
  } catch (error) {
    console.error('예산 저장 실패:', error);
    throw error;
  }
};

// 월별 합계 계산
export const calculateMonthlyTotal = (transactions) => {
  let income = 0;
  let expense = 0;
  transactions.forEach(t => {
    if (t.type === 'income') income += t.amount;
    else expense += t.amount;
  });
  return { income, expense, balance: income - expense };
};

// 카테고리별 지출 합계
export const calculateCategoryTotals = (transactions) => {
  const totals = {};
  transactions
    .filter(t => t.type === 'expense')
    .forEach(t => {
      totals[t.category] = (totals[t.category] || 0) + t.amount;
    });
  return totals;
};

// 금액 포맷
export const formatAmount = (amount) => {
  return amount.toLocaleString('ko-KR') + '원';
};
