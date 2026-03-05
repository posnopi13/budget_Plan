import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getBudgets, saveBudget, getTransactionsByMonth, formatAmount } from '../utils/storage';
import { EXPENSE_CATEGORIES } from '../constants/categories';

export default function BudgetScreen() {
  const [budgets, setBudgets] = useState({});
  const [categorySpent, setCategorySpent] = useState({});
  const [editingCategory, setEditingCategory] = useState(null);
  const [editValue, setEditValue] = useState('');

  const now = new Date();

  const loadData = useCallback(async () => {
    const savedBudgets = await getBudgets();
    setBudgets(savedBudgets);

    const transactions = await getTransactionsByMonth(now.getFullYear(), now.getMonth() + 1);
    const spent = {};
    transactions
      .filter(t => t.type === 'expense')
      .forEach(t => {
        spent[t.category] = (spent[t.category] || 0) + t.amount;
      });
    setCategorySpent(spent);
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const startEditing = (categoryId) => {
    setEditingCategory(categoryId);
    setEditValue(budgets[categoryId] ? budgets[categoryId].toString() : '');
  };

  const handleSave = async (categoryId) => {
    const amount = Number(editValue.replace(/[^0-9]/g, ''));
    if (isNaN(amount)) {
      Alert.alert('오류', '올바른 금액을 입력해주세요.');
      return;
    }
    await saveBudget(categoryId, amount);
    setBudgets(prev => ({ ...prev, [categoryId]: amount }));
    setEditingCategory(null);
    setEditValue('');
  };

  const totalBudget = Object.values(budgets).reduce((sum, v) => sum + (v || 0), 0);
  const totalSpent = Object.values(categorySpent).reduce((sum, v) => sum + (v || 0), 0);
  const totalRemaining = totalBudget - totalSpent;

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* 전체 예산 요약 */}
      {totalBudget > 0 && (
        <View style={styles.totalCard}>
          <Text style={styles.totalTitle}>{now.getMonth() + 1}월 전체 예산</Text>
          <Text style={styles.totalBudget}>{formatAmount(totalBudget)}</Text>
          <View style={styles.totalRow}>
            <View style={styles.totalItem}>
              <Text style={styles.totalItemLabel}>사용</Text>
              <Text style={[styles.totalItemValue, { color: '#FF6B6B' }]}>{formatAmount(totalSpent)}</Text>
            </View>
            <View style={styles.totalItem}>
              <Text style={styles.totalItemLabel}>남은 예산</Text>
              <Text style={[styles.totalItemValue, { color: totalRemaining >= 0 ? '#00B894' : '#FF6B6B' }]}>
                {totalRemaining >= 0 ? '' : '-'}{formatAmount(Math.abs(totalRemaining))}
              </Text>
            </View>
          </View>
          <View style={styles.totalProgressBg}>
            <View
              style={[
                styles.totalProgressBar,
                {
                  width: `${Math.min((totalSpent / totalBudget) * 100, 100)}%`,
                  backgroundColor: totalSpent > totalBudget ? '#FF6B6B' : '#6C5CE7',
                },
              ]}
            />
          </View>
          <Text style={styles.totalProgressText}>
            {Math.round((totalSpent / totalBudget) * 100)}% 사용
          </Text>
        </View>
      )}

      <Text style={styles.sectionTitle}>카테고리별 예산 설정</Text>

      {EXPENSE_CATEGORIES.map((category) => {
        const budget = budgets[category.id] || 0;
        const spent = categorySpent[category.id] || 0;
        const remaining = budget - spent;
        const percentage = budget > 0 ? Math.min((spent / budget) * 100, 100) : 0;
        const isOver = spent > budget && budget > 0;
        const isEditing = editingCategory === category.id;

        return (
          <View key={category.id} style={styles.budgetCard}>
            <View style={styles.budgetHeader}>
              <View style={styles.budgetLeft}>
                <View style={[styles.iconBg, { backgroundColor: category.color + '22' }]}>
                  <Text style={styles.icon}>{category.icon}</Text>
                </View>
                <Text style={styles.categoryName}>{category.name}</Text>
              </View>

              {isEditing ? (
                <View style={styles.editRow}>
                  <TextInput
                    style={styles.budgetInput}
                    value={editValue}
                    onChangeText={setEditValue}
                    keyboardType="numeric"
                    placeholder="예산 입력"
                    placeholderTextColor="#ADB5BD"
                    autoFocus
                  />
                  <TouchableOpacity
                    style={[styles.actionBtn, { backgroundColor: category.color }]}
                    onPress={() => handleSave(category.id)}
                  >
                    <Text style={styles.actionBtnText}>저장</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={styles.cancelBtn}
                    onPress={() => setEditingCategory(null)}
                  >
                    <Text style={styles.cancelBtnText}>취소</Text>
                  </TouchableOpacity>
                </View>
              ) : (
                <TouchableOpacity
                  style={styles.setBudgetBtn}
                  onPress={() => startEditing(category.id)}
                >
                  <Text style={styles.setBudgetText}>
                    {budget > 0 ? formatAmount(budget) : '예산 설정'}
                  </Text>
                </TouchableOpacity>
              )}
            </View>

            {budget > 0 && (
              <>
                <View style={styles.spentRow}>
                  <Text style={styles.spentLabel}>
                    사용: <Text style={{ color: '#FF6B6B', fontWeight: '700' }}>{formatAmount(spent)}</Text>
                  </Text>
                  <Text style={[styles.remainingLabel, { color: isOver ? '#FF6B6B' : '#00B894' }]}>
                    {isOver ? `초과: ${formatAmount(spent - budget)}` : `남음: ${formatAmount(remaining)}`}
                  </Text>
                </View>
                <View style={styles.progressBg}>
                  <View
                    style={[
                      styles.progressBar,
                      {
                        width: `${percentage}%`,
                        backgroundColor: isOver ? '#FF6B6B' : category.color,
                      },
                    ]}
                  />
                </View>
                {isOver && (
                  <Text style={styles.overBudgetText}>⚠️ 예산을 초과했습니다!</Text>
                )}
              </>
            )}
          </View>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
  },
  totalCard: {
    margin: 16,
    backgroundColor: '#6C5CE7',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#6C5CE7',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  totalTitle: {
    fontSize: 13,
    color: 'rgba(255,255,255,0.8)',
    fontWeight: '600',
    marginBottom: 4,
  },
  totalBudget: {
    fontSize: 28,
    fontWeight: '800',
    color: '#FFFFFF',
    marginBottom: 16,
  },
  totalRow: {
    flexDirection: 'row',
    marginBottom: 12,
  },
  totalItem: {
    flex: 1,
  },
  totalItemLabel: {
    fontSize: 12,
    color: 'rgba(255,255,255,0.7)',
    marginBottom: 4,
  },
  totalItemValue: {
    fontSize: 16,
    fontWeight: '700',
  },
  totalProgressBg: {
    height: 8,
    backgroundColor: 'rgba(255,255,255,0.3)',
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 6,
  },
  totalProgressBar: {
    height: 8,
    borderRadius: 4,
    backgroundColor: '#FFFFFF',
  },
  totalProgressText: {
    fontSize: 11,
    color: 'rgba(255,255,255,0.8)',
    textAlign: 'right',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#2D3436',
    marginHorizontal: 16,
    marginBottom: 12,
  },
  budgetCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    marginHorizontal: 16,
    marginBottom: 10,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  budgetHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  budgetLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBg: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  icon: {
    fontSize: 18,
  },
  categoryName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#2D3436',
  },
  setBudgetBtn: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#F1F3F5',
    borderRadius: 8,
  },
  setBudgetText: {
    fontSize: 13,
    color: '#6C757D',
    fontWeight: '600',
  },
  editRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  budgetInput: {
    width: 100,
    fontSize: 14,
    color: '#2D3436',
    borderBottomWidth: 2,
    borderBottomColor: '#6C5CE7',
    paddingBottom: 4,
    textAlign: 'right',
  },
  actionBtn: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 6,
  },
  actionBtnText: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700',
  },
  cancelBtn: {
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  cancelBtnText: {
    color: '#ADB5BD',
    fontSize: 13,
  },
  spentRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  spentLabel: {
    fontSize: 13,
    color: '#6C757D',
  },
  remainingLabel: {
    fontSize: 13,
    fontWeight: '600',
  },
  progressBg: {
    height: 8,
    backgroundColor: '#F1F3F5',
    borderRadius: 4,
    overflow: 'hidden',
  },
  progressBar: {
    height: 8,
    borderRadius: 4,
  },
  overBudgetText: {
    fontSize: 12,
    color: '#FF6B6B',
    fontWeight: '600',
    marginTop: 6,
    textAlign: 'right',
  },
});
