import React from 'react'

import HomePage from './HomePage'
import navbarProps from '../components/navbar/sampleProps.json'

export default {
  title: 'Features/HomePage',
  parameters: { layout: 'fullscreen' }
}
const splashImage = require('../images/splash.jpg')

export const loggedOut = () => (
  <HomePage navbar={navbarProps} splash={{ image: splashImage }} csrfToken={{ value: 'xxx' }} />
)

// loggedOut.storyName = 'to Storybook'
