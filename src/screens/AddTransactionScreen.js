import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  ScrollView,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { saveTransaction } from '../utils/storage';
import { EXPENSE_CATEGORIES, INCOME_CATEGORIES } from '../constants/categories';

export default function AddTransactionScreen({ navigation }) {
  const [type, setType] = useState('expense');
  const [amount, setAmount] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [memo, setMemo] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);

  const categories = type === 'expense' ? EXPENSE_CATEGORIES : INCOME_CATEGORIES;

  const handleAmountChange = (text) => {
    const numeric = text.replace(/[^0-9]/g, '');
    setAmount(numeric);
  };

  const getFormattedAmount = () => {
    if (!amount) return '';
    return Number(amount).toLocaleString('ko-KR');
  };

  const handleSave = async () => {
    if (!amount || Number(amount) === 0) {
      Alert.alert('입력 오류', '금액을 입력해주세요.');
      return;
    }
    if (!selectedCategory) {
      Alert.alert('입력 오류', '카테고리를 선택해주세요.');
      return;
    }

    try {
      await saveTransaction({
        type,
        amount: Number(amount),
        category: selectedCategory,
        memo: memo.trim(),
        date,
      });
      navigation.goBack();
    } catch (error) {
      Alert.alert('오류', '저장에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scroll}>
        {/* 수입/지출 탭 */}
        <View style={styles.typeSelector}>
          <TouchableOpacity
            style={[styles.typeBtn, type === 'expense' && styles.typeBtnExpenseActive]}
            onPress={() => { setType('expense'); setSelectedCategory(''); }}
          >
            <Text style={[styles.typeBtnText, type === 'expense' && styles.typeBtnTextActive]}>
              지출
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.typeBtn, type === 'income' && styles.typeBtnIncomeActive]}
            onPress={() => { setType('income'); setSelectedCategory(''); }}
          >
            <Text style={[styles.typeBtnText, type === 'income' && styles.typeBtnTextActive]}>
              수입
            </Text>
          </TouchableOpacity>
        </View>

        {/* 금액 입력 */}
        <View style={styles.amountSection}>
          <Text style={styles.label}>금액</Text>
          <View style={styles.amountInputWrapper}>
            <TextInput
              style={styles.amountInput}
              value={getFormattedAmount()}
              onChangeText={handleAmountChange}
              keyboardType="numeric"
              placeholder="0"
              placeholderTextColor="#ADB5BD"
            />
            <Text style={styles.amountUnit}>원</Text>
          </View>
        </View>

        {/* 날짜 입력 */}
        <View style={styles.section}>
          <Text style={styles.label}>날짜</Text>
          <TextInput
            style={styles.dateInput}
            value={date}
            onChangeText={setDate}
            placeholder="YYYY-MM-DD"
            placeholderTextColor="#ADB5BD"
          />
        </View>

        {/* 카테고리 선택 */}
        <View style={styles.section}>
          <Text style={styles.label}>카테고리</Text>
          <View style={styles.categoryGrid}>
            {categories.map((cat) => (
              <TouchableOpacity
                key={cat.id}
                style={[
                  styles.categoryItem,
                  selectedCategory === cat.id && styles.categoryItemSelected,
                  { borderColor: selectedCategory === cat.id ? cat.color : '#E9ECEF' },
                ]}
                onPress={() => setSelectedCategory(cat.id)}
              >
                <View style={[styles.categoryIconBg, { backgroundColor: cat.color + '22' }]}>
                  <Text style={styles.categoryIcon}>{cat.icon}</Text>
                </View>
                <Text
                  style={[
                    styles.categoryName,
                    { color: selectedCategory === cat.id ? cat.color : '#6C757D' },
                  ]}
                >
                  {cat.name}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* 메모 입력 */}
        <View style={styles.section}>
          <Text style={styles.label}>메모 (선택)</Text>
          <TextInput
            style={styles.memoInput}
            value={memo}
            onChangeText={setMemo}
            placeholder="메모를 입력하세요"
            placeholderTextColor="#ADB5BD"
            maxLength={50}
          />
        </View>

        {/* 저장 버튼 */}
        <TouchableOpacity
          style={[
            styles.saveBtn,
            { backgroundColor: type === 'expense' ? '#FF6B6B' : '#00B894' },
          ]}
          onPress={handleSave}
        >
          <Text style={styles.saveBtnText}>저장하기</Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
  },
  scroll: {
    padding: 16,
    paddingBottom: 40,
  },
  typeSelector: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 4,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  typeBtn: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  typeBtnExpenseActive: {
    backgroundColor: '#FF6B6B',
  },
  typeBtnIncomeActive: {
    backgroundColor: '#00B894',
  },
  typeBtnText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ADB5BD',
  },
  typeBtnTextActive: {
    color: '#FFFFFF',
  },
  amountSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6C757D',
    marginBottom: 10,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  amountInputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  amountInput: {
    flex: 1,
    fontSize: 32,
    fontWeight: '800',
    color: '#2D3436',
  },
  amountUnit: {
    fontSize: 20,
    fontWeight: '600',
    color: '#6C757D',
    marginLeft: 8,
  },
  section: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  dateInput: {
    fontSize: 16,
    color: '#2D3436',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#E9ECEF',
  },
  categoryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginHorizontal: -4,
  },
  categoryItem: {
    width: '30%',
    margin: '1.5%',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 8,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#E9ECEF',
    backgroundColor: '#FFFFFF',
  },
  categoryItemSelected: {
    backgroundColor: '#FAFAFE',
  },
  categoryIconBg: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  categoryIcon: {
    fontSize: 20,
  },
  categoryName: {
    fontSize: 11,
    fontWeight: '600',
    textAlign: 'center',
  },
  memoInput: {
    fontSize: 16,
    color: '#2D3436',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#E9ECEF',
  },
  saveBtn: {
    paddingVertical: 18,
    borderRadius: 14,
    alignItems: 'center',
    marginTop: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 5,
  },
  saveBtnText: {
    fontSize: 17,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
