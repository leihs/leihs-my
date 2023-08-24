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

// NOTE: this lives in "legacy manage app" and will not exist when running `my` or `my/ui` standalone.
const splashImageDefault = '/assets/leihs-62b57b03ec5abd5e5fa3e6c35fde8a782419982d2cdd771fa8fba6cd0ab63d41.png'

const defaultProps = {
  navbar: {
    config: { ...exampleNavbarParams }
  },
  splash: {
    title: 'Title',
    text: 'Subtitle',
    image: splashImageDefault
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
