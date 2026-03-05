import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import {
  getTransactionsByMonth,
  calculateMonthlyTotal,
  calculateCategoryTotals,
  formatAmount,
} from '../utils/storage';
import { getCategoryById, EXPENSE_CATEGORIES } from '../constants/categories';

const SCREEN_WIDTH = Dimensions.get('window').width;

export default function StatisticsScreen() {
  const [transactions, setTransactions] = useState([]);
  const [totals, setTotals] = useState({ income: 0, expense: 0, balance: 0 });
  const [categoryTotals, setCategoryTotals] = useState({});
  const [currentDate, setCurrentDate] = useState(new Date());
  const [activeTab, setActiveTab] = useState('expense');

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth() + 1;

  const loadData = useCallback(async () => {
    const data = await getTransactionsByMonth(year, month);
    setTransactions(data);
    setTotals(calculateMonthlyTotal(data));
    setCategoryTotals(calculateCategoryTotals(data));
  }, [year, month]);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const changeMonth = (direction) => {
    const newDate = new Date(currentDate);
    newDate.setMonth(newDate.getMonth() + direction);
    setCurrentDate(newDate);
  };

  // 카테고리별 지출 정렬
  const sortedCategories = Object.entries(categoryTotals)
    .sort(([, a], [, b]) => b - a)
    .filter(([, amount]) => amount > 0);

  const maxAmount = sortedCategories.length > 0 ? sortedCategories[0][1] : 1;

  // 일별 지출 계산
  const getDailyData = () => {
    const daily = {};
    const filtered = transactions.filter(t => t.type === activeTab);
    filtered.forEach(t => {
      const day = new Date(t.date).getDate();
      daily[day] = (daily[day] || 0) + t.amount;
    });
    return daily;
  };

  const dailyData = getDailyData();
  const daysInMonth = new Date(year, month, 0).getDate();
  const maxDailyAmount = Math.max(...Object.values(dailyData), 1);

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* 월 선택 */}
      <View style={styles.monthHeader}>
        <TouchableOpacity onPress={() => changeMonth(-1)} style={styles.arrowBtn}>
          <Text style={styles.arrowText}>‹</Text>
        </TouchableOpacity>
        <Text style={styles.monthText}>{year}년 {month}월</Text>
        <TouchableOpacity onPress={() => changeMonth(1)} style={styles.arrowBtn}>
          <Text style={styles.arrowText}>›</Text>
        </TouchableOpacity>
      </View>

      {/* 요약 */}
      <View style={styles.summaryRow}>
        <View style={[styles.summaryCard, { borderLeftColor: '#00B894' }]}>
          <Text style={styles.summaryLabel}>총 수입</Text>
          <Text style={[styles.summaryAmount, { color: '#00B894' }]}>
            {formatAmount(totals.income)}
          </Text>
        </View>
        <View style={[styles.summaryCard, { borderLeftColor: '#FF6B6B' }]}>
          <Text style={styles.summaryLabel}>총 지출</Text>
          <Text style={[styles.summaryAmount, { color: '#FF6B6B' }]}>
            {formatAmount(totals.expense)}
          </Text>
        </View>
      </View>

      {/* 일별 막대 그래프 */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>일별 현황</Text>
          <View style={styles.tabRow}>
            <TouchableOpacity
              style={[styles.tab, activeTab === 'expense' && styles.tabActive]}
              onPress={() => setActiveTab('expense')}
            >
              <Text style={[styles.tabText, activeTab === 'expense' && styles.tabTextActive]}>지출</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.tab, activeTab === 'income' && styles.tabActiveIncome]}
              onPress={() => setActiveTab('income')}
            >
              <Text style={[styles.tabText, activeTab === 'income' && styles.tabTextActive]}>수입</Text>
            </TouchableOpacity>
          </View>
        </View>

        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <View style={styles.barChart}>
            {Array.from({ length: daysInMonth }, (_, i) => i + 1).map((day) => {
              const amount = dailyData[day] || 0;
              const barHeight = amount > 0 ? Math.max((amount / maxDailyAmount) * 80, 4) : 2;
              return (
                <View key={day} style={styles.barContainer}>
                  <View style={styles.barWrapper}>
                    <View
                      style={[
                        styles.bar,
                        {
                          height: barHeight,
                          backgroundColor: activeTab === 'expense' ? '#FF6B6B' : '#00B894',
                          opacity: amount > 0 ? 1 : 0.2,
                        },
                      ]}
                    />
                  </View>
                  <Text style={styles.barLabel}>{day}</Text>
                </View>
              );
            })}
          </View>
        </ScrollView>
      </View>

      {/* 카테고리별 지출 */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>카테고리별 지출</Text>
        {sortedCategories.length === 0 ? (
          <Text style={styles.emptyText}>이번 달 지출 내역이 없습니다.</Text>
        ) : (
          sortedCategories.map(([categoryId, amount]) => {
            const category = getCategoryById(categoryId);
            const percentage = Math.round((amount / totals.expense) * 100);
            const barWidth = (amount / maxAmount) * (SCREEN_WIDTH - 80);

            return (
              <View key={categoryId} style={styles.categoryRow}>
                <View style={styles.categoryHeader}>
                  <View style={styles.categoryLeft}>
                    <Text style={styles.categoryEmoji}>{category.icon}</Text>
                    <Text style={styles.categoryName}>{category.name}</Text>
                  </View>
                  <View style={styles.categoryRight}>
                    <Text style={styles.categoryAmount}>{formatAmount(amount)}</Text>
                    <Text style={styles.categoryPercent}>{percentage}%</Text>
                  </View>
                </View>
                <View style={styles.progressBg}>
                  <View
                    style={[
                      styles.progressBar,
                      { width: barWidth, backgroundColor: category.color },
                    ]}
                  />
                </View>
              </View>
            );
          })
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
  },
  monthHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E9ECEF',
  },
  arrowBtn: {
    paddingHorizontal: 20,
    paddingVertical: 8,
  },
  arrowText: {
    fontSize: 28,
    color: '#6C757D',
    fontWeight: '300',
  },
  monthText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#2D3436',
    minWidth: 120,
    textAlign: 'center',
  },
  summaryRow: {
    flexDirection: 'row',
    padding: 16,
    gap: 12,
  },
  summaryCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    borderLeftWidth: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  summaryLabel: {
    fontSize: 12,
    color: '#6C757D',
    fontWeight: '600',
    marginBottom: 6,
  },
  summaryAmount: {
    fontSize: 16,
    fontWeight: '800',
  },
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    marginHorizontal: 16,
    marginBottom: 16,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 3,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#2D3436',
    marginBottom: 16,
  },
  tabRow: {
    flexDirection: 'row',
    backgroundColor: '#F8F9FA',
    borderRadius: 8,
    padding: 2,
  },
  tab: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
  },
  tabActive: {
    backgroundColor: '#FF6B6B',
  },
  tabActiveIncome: {
    backgroundColor: '#00B894',
  },
  tabText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#ADB5BD',
  },
  tabTextActive: {
    color: '#FFFFFF',
  },
  barChart: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingBottom: 4,
    minHeight: 110,
  },
  barContainer: {
    width: 22,
    alignItems: 'center',
    marginHorizontal: 3,
  },
  barWrapper: {
    height: 90,
    justifyContent: 'flex-end',
  },
  bar: {
    width: 14,
    borderRadius: 4,
  },
  barLabel: {
    fontSize: 9,
    color: '#ADB5BD',
    marginTop: 4,
  },
  categoryRow: {
    marginBottom: 16,
  },
  categoryHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  categoryLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  categoryEmoji: {
    fontSize: 18,
    marginRight: 8,
  },
  categoryName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#2D3436',
  },
  categoryRight: {
    alignItems: 'flex-end',
  },
  categoryAmount: {
    fontSize: 14,
    fontWeight: '700',
    color: '#2D3436',
  },
  categoryPercent: {
    fontSize: 11,
    color: '#6C757D',
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
  emptyText: {
    textAlign: 'center',
    color: '#ADB5BD',
    fontSize: 14,
    paddingVertical: 20,
  },
});
