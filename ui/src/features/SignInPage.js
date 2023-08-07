import React, { Component } from 'react'
import f from 'lodash'

import Navbar from '../components/navbar/Navbar'
import { CenterOnPage } from '../components/CardPage'
import { SignInCard } from '../components/SignInUI'
import { Translator as T } from '../locale/translate'

const defaultProps = {
  authFlow: {
    form: {
      method: 'POST',
      action: '/sign-in'
    },
    forgotPasswordLink: '/password-reset/forgot-password'
  }
}

class SignInPage extends Component {
  render(props = this.props) {
    const t = T(props.navbar.config.locales)
    const authSystems = f.get(props, 'authSystems')
    const flashMessages = f.map(f.get(props, 'flashMessages'), m =>
      f.merge(m, { message: m.messageID ? t(m.messageID) : m.message })
    )
    const authFlow = f.merge(f.get(props, 'authFlow'), {
      title: t('sign_in_title')
    })

    return (
      <div className="bg-paper h-100">
        <Navbar {...props.navbar} hideSignInField />

        <CenterOnPage>
          <SignInCard
            authFlow={f.merge(defaultProps.authFlow, authFlow)}
            authSystems={authSystems}
            messages={flashMessages}
            locales={props.navbar.config.locales}
            csrfToken={props.csrfToken}
          />
        </CenterOnPage>
      </div>
    )
  }
}

export default SignInPage
