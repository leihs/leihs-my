import React from 'react'
import breakLinesReact from './break-lines-react'

export default {
  title: 'Lib/Break Lines'
}

export function breakLines() {
  return (
    <>
      <h1>`breakLinesReact` render function</h1>
      <p className="text-muted">Renders newlines as HTML breaks</p>
      <h2>undefined</h2>
      <p>{breakLinesReact()}</p>
      <h2>empty string</h2>
      <p>{breakLinesReact('')}</p>
      <h2>one line string</h2>
      <p>{breakLinesReact('hello')}</p>
      <h2>multi line</h2>
      <p>{breakLinesReact('hello\\nworld')}</p>
      <h2>multi line, tab, escaped character</h2>
      <p>{breakLinesReact('hello\nworld \\n \netc\np\tp')}</p>
    </>
  )
}
