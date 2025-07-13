import React from 'react';
import { Result, Button } from 'antd';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Result
          status="500"
          title="오류가 발생했습니다"
          subTitle="예상치 못한 오류가 발생했습니다. 페이지를 새로고침해 주세요."
          extra={
            <Button type="primary" onClick={() => window.location.reload()}>
              새로고침
            </Button>
          }
        />
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;