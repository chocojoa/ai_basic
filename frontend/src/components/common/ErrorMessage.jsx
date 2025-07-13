import React from 'react';
import { Alert, Button } from 'antd';

const ErrorMessage = ({ 
  error, 
  onRetry, 
  showRetry = true,
  type = 'error',
  style = {} 
}) => {
  // error가 null, undefined, 빈 문자열인 경우 렌더링하지 않음
  if (!error) {
    return null;
  }

  const getErrorMessage = (error) => {
    if (typeof error === 'string') return error;
    if (error?.message) return error.message;
    if (error?.response?.data?.message) return error.response.data.message;
    return '알 수 없는 오류가 발생했습니다.';
  };

  return (
    <div style={{ margin: '20px 0', ...style }}>
      <Alert
        message="오류 발생"
        description={getErrorMessage(error)}
        type={type}
        showIcon
        action={
          showRetry && onRetry && (
            <Button size="small" danger onClick={onRetry}>
              다시 시도
            </Button>
          )
        }
      />
    </div>
  );
};

export default ErrorMessage;