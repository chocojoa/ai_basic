// 전화번호 포맷팅 유틸리티
export const formatPhoneNumber = (phoneNumber) => {
  if (!phoneNumber) return '';
  
  // 숫자만 추출
  const cleaned = phoneNumber.replace(/\D/g, '');
  
  // 한국 전화번호 형식에 맞춰 포맷팅
  if (cleaned.length === 11) {
    // 010-1234-5678 형식
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  } else if (cleaned.length === 10) {
    // 02-1234-5678 또는 031-123-4567 형식
    if (cleaned.startsWith('02')) {
      return cleaned.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3');
    } else {
      return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
    }
  } else if (cleaned.length === 9) {
    // 02-123-4567 형식
    if (cleaned.startsWith('02')) {
      return cleaned.replace(/(\d{2})(\d{3})(\d{4})/, '$1-$2-$3');
    } else {
      return cleaned.replace(/(\d{3})(\d{3})(\d{3})/, '$1-$2-$3');
    }
  }
  
  return phoneNumber;
};

// 전화번호 입력 시 실시간 포맷팅
export const handlePhoneInput = (value) => {
  if (!value) return '';
  
  // 숫자만 추출
  const cleaned = value.replace(/\D/g, '');
  
  // 길이에 따라 포맷팅
  if (cleaned.length <= 3) {
    return cleaned;
  } else if (cleaned.length <= 7) {
    if (cleaned.startsWith('02') && cleaned.length <= 6) {
      return cleaned.replace(/(\d{2})(\d{0,4})/, '$1-$2');
    } else {
      return cleaned.replace(/(\d{3})(\d{0,4})/, '$1-$2');
    }
  } else if (cleaned.length <= 11) {
    if (cleaned.startsWith('02')) {
      if (cleaned.length <= 10) {
        return cleaned.replace(/(\d{2})(\d{4})(\d{0,4})/, '$1-$2-$3');
      } else {
        return cleaned.substring(0, 10).replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3');
      }
    } else {
      return cleaned.replace(/(\d{3})(\d{4})(\d{0,4})/, '$1-$2-$3');
    }
  }
  
  return value;
};

// 전화번호 유효성 검증
export const validatePhoneNumber = (phoneNumber) => {
  if (!phoneNumber) return true; // 전화번호는 선택사항
  
  const cleaned = phoneNumber.replace(/\D/g, '');
  
  // 한국 전화번호 패턴 검증
  const patterns = [
    /^010\d{8}$/,  // 010-XXXX-XXXX
    /^02\d{7,8}$/,  // 02-XXX-XXXX 또는 02-XXXX-XXXX
    /^0\d{1,2}\d{7,8}$/  // 기타 지역번호
  ];
  
  return patterns.some(pattern => pattern.test(cleaned));
};