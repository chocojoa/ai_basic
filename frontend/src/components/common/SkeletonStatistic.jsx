import React from 'react';
import { Card, Skeleton } from 'antd';

const SkeletonStatistic = ({ loading = true, children, ...props }) => {
  return (
    <Card {...props}>
      {loading ? (
        <div style={{ padding: '16px 0' }}>
          <Skeleton.Input 
            active 
            size="small" 
            style={{ width: '60%', marginBottom: '8px' }} 
          />
          <Skeleton.Input 
            active 
            size="large" 
            style={{ width: '80%' }} 
          />
        </div>
      ) : (
        children
      )}
    </Card>
  );
};

export default SkeletonStatistic;