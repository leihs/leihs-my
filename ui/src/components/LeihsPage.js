import React from 'react'
import cx from 'classnames'

const LeihsPage = ({ className, children, ...props }) => (
  <div className={cx('bg-paper h-100', className)} {...props}>
    {children}
  </div>
)

export default LeihsPage
