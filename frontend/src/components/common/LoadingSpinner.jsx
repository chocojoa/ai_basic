import React from 'react';
import { Spin } from 'antd';

const LoadingSpinner = ({ 
  size = 'large', 
  tip = '로딩 중...', 
  style = {} 
}) => {
  const defaultStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '200px',
    ...style
  };

  return (
    <div style={defaultStyle}>
      <Spin size={size} tip={tip}>
        <div style={{ minHeight: '50px', minWidth: '50px' }} />
      </Spin>
    </div>
  );
};

export default LoadingSpinner;