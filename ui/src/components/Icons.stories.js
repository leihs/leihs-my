import React from 'react'
import Icon from './Icons'
import f from 'lodash'

export default {
  title: 'Components/Icons'
}

export const all_icons = () => (
  <>
    <h2>List of all Icons</h2>
    <p>
      They are all in <b>SVG</b> format, so the components can be used anywhere and do not need FontAwesome <b>CSS</b>!
    </p>
    <hr />
    {f.map(Icon, (Icon, name) => (
      <React.Fragment key={name}>
        <h4>
          <pre>{`<Icon.${name} />`}</pre>
        </h4>
        <Icon size="3x" />
        <hr />
      </React.Fragment>
    ))}
  </>
)
all_icons.storyName = 'All Icons'

export const usage = () => (
  <React.Fragment>
    <h6>colors</h6>
    <Icon.LeihsProcurement size="2x" /> <Icon.LeihsProcurement size="2x" color="dark" />{' '}
    <Icon.LeihsProcurement size="2x" color="secondary" /> <Icon.LeihsProcurement size="2x" color="light" />{' '}
    <Icon.LeihsProcurement size="2x" color="primary" /> <Icon.LeihsProcurement size="2x" color="success" />{' '}
    <Icon.LeihsProcurement size="2x" color="warning" /> <Icon.LeihsProcurement size="2x" color="danger" />{' '}
    <Icon.LeihsProcurement size="2x" color="info" />
    <hr />
    <h6>spacing</h6>
    <Icon.LeihsProcurement size="2x" spaced />
    <Icon.LeihsProcurement size="2x" spaced="1" />
    <Icon.LeihsProcurement size="2x" spaced="2" />
    <Icon.LeihsProcurement size="2x" spaced="3" />
    <Icon.LeihsProcurement size="2x" spaced="4" />
    <Icon.LeihsProcurement size="2x" spaced="5" />
    <Icon.LeihsProcurement size="2x" spaced="6" />
  </React.Fragment>
)
