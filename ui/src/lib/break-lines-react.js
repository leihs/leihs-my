import React, { Fragment as F } from 'react'
import f from 'lodash'

export default function (text) {
  // 'hello\nworld' => hello<br/>world
  return (
    !!text &&
    (f.isArray(text) ? text : text.split('\n')).map((item, i, a) => (
      <React.Fragment key={i}>
        {item}
        {/* no break after last line: */}
        {i + 1 < a.length && <br />}
      </React.Fragment>
    ))
  )
}
