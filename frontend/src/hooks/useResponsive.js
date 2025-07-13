import { useState, useEffect } from 'react';

const useResponsive = () => {
  const [screenSize, setScreenSize] = useState({
    isMobile: false,
    isTablet: false,
    isDesktop: false,
    width: 0,
  });

  useEffect(() => {
    const updateScreenSize = () => {
      const width = window.innerWidth;
      
      setScreenSize({
        isMobile: width < 768,
        isTablet: width >= 768 && width < 1024,
        isDesktop: width >= 1024,
        width,
      });
    };

    // 초기 설정
    updateScreenSize();

    // 리사이즈 이벤트 리스너 추가
    window.addEventListener('resize', updateScreenSize);

    // 클린업
    return () => window.removeEventListener('resize', updateScreenSize);
  }, []);

  return screenSize;
};

export default useResponsive;