import f from 'lodash'
import React, { Component } from 'react'

import RootPage from './RootPage'
import { Translator as T } from '../locale/translate'

const exampleNavbarParams = {
  appTitle: 'leihs',
  me: false,
  appMenu: null,
  subApps: false
}

const defaultSplashImage = require('../images/splash.jpg')

const defaultProps = {
  navbar: {
    config: { ...exampleNavbarParams }
  },
  splash: {
    title: 'Title',
    text: 'Subtitle',
    image: defaultSplashImage
  },
  footer: {
    appName: 'leihs',
    appVersion: 'dev',
    appVersionLink: '/release'
  }
}

class HomePage extends Component {
  render(props = this.props) {
    const t = T(props.navbar.config.locales)
    const rootProps = f.merge(defaultProps, props, {
      splash: {
        title: t('homepage_hero_title'),
        text: t('homepage_hero_subtitle')
      }
    })
    return (
      <React.Fragment>
        <RootPage {...rootProps} />
      </React.Fragment>
    )
  }
}

export default HomePage
