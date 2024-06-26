import React, { Fragment as F } from 'react'
import f from 'lodash'
import Let from '../lib/Let'
import { Card } from '../components/CardPage'
import FlashMessages from './FlashMessages'
import CsrfTokenField from '../components/CsrfTokenField'
import { Translator as T } from '../locale/translate'

export const SignInCard = ({ authFlow, authSystems, messages, locales, csrfToken, autoFocusUserField = true }) => {
  const t = T(locales)
  const userParam = f.get(authFlow, 'user')
  const returnTo = f.get(authFlow, 'returnTo') || '/'
  const extAuths = f.filter(authSystems, { type: 'external' })
  const pwAuth = f.find(authSystems, { type: 'password' })
  const hasFailed = !!userParam && f.some(messages, ({ level }) => level === 'error')

  const step = !userParam || hasFailed ? 1 : 2

  const firstStep = step === 1 && (
    <F>
      <form className="ui-form-signin" method={authFlow.form.method} action={authFlow.form.action}>
        <label htmlFor={'inputEmail'} className="sr-only">
          {t('sign_in_userparam_label')}
        </label>
        <input
          id={'inputEmail'}
          name="user"
          required
          placeholder={t('sign_in_userparam_label')}
          className="form-control"
          defaultValue={userParam || ''}
          autoFocus={autoFocusUserField}
          autoCapitalize="off"
          autoCorrect="off"
          onChange={e => (e.target.value = e.target.value.trim())}
        />

        <CsrfTokenField {...csrfToken} />
        {returnTo && <input type="hidden" name="return-to" value={returnTo} />}

        <button className="btn btn-success btn-block mt-3" type="submit">
          {t('sign_in_btn_continue')}
        </button>
      </form>
      {/*
        {false && <ResetUser userName={userParam} resetLink={'./sign-in'} />}
      */}
    </F>
  )

  const secondStep = step === 2 && (
    <F>
      {!f.isEmpty(extAuths) && (
        <div className="mb-4">
          {/* <h2 className="h5 mb-2 font-weight-normal">
            {'Anmelden mit externem Dienst'}
          </h2> */}
          {f.map(extAuths, ({ id, name, description, ...o }) => (
            <form
              className="ui-form-signin mt-3"
              key={id}
              method="POST"
              action={`/sign-in/external-authentication/${id}/request`}
            >
              <CsrfTokenField {...csrfToken} />
              <input type="hidden" name="user-unique-id" value={userParam || ''} />
              {returnTo && <input type="hidden" name="return-to" value={returnTo} />}
              <button className="btn btn-lg btn-success btn-block" href={o.external_url} type="submit">
                {/* {'ZHdK-Login'} */}
                {name}
              </button>
              <small>{description}</small>
            </form>
          ))}
        </div>
      )}

      {!f.isEmpty(extAuths) && !f.isEmpty(pwAuth) && (
        <div className="hr-text">
          <hr />
          oder
        </div>
      )}

      {!f.isEmpty(pwAuth) && (
        <Let {...pwAuth}>
          {({ id, description, title }) => (
            <form key={id} className="ui-form-signin mt-4" method={authFlow.form.method} action={authFlow.form.action}>
              <CsrfTokenField {...csrfToken} />
              <h2 className="h5 mb-4 font-weight-normal">
                {title || t('sign_in_pwauth_default_title')}
                <small>{description}</small>
              </h2>
              <label htmlFor={'inputEmail'} className="sr-only">
                {t('sign_in_userparam_label')}
              </label>
              <input
                id={'inputEmail'}
                name="user"
                className="form-control"
                required
                value={userParam || ''}
                readOnly
                style={{
                  marginBottom: '-1px',
                  borderBottomRightRadius: 0,
                  borderBottomLeftRadius: 0
                }}
              />

              <label htmlFor={'inputPassword'} className="sr-only">
                {t('sign_in_password')}
              </label>
              <input
                type="password"
                name="password"
                id={'inputPassword'}
                placeholder={t('sign_in_password')}
                required
                autoComplete="current-password"
                className="form-control"
                style={{
                  borderTopRightRadius: 0,
                  borderTopLeftRadius: 0
                }}
              />
              {returnTo && <input type="hidden" name="return-to" value={returnTo} />}

              <button className="btn btn-success btn-block mt-3" type="submit">
                {t('sign_in_btn_continue')}
              </button>
            </form>
          )}
        </Let>
      )}

      {/* <ResetUser userName={userParam} resetLink={'./sign-in'} /> */}
      {false && (
        <p className="mt-4">
          <a href={returnTo} className="btn btn-block btn-outline-secondary">
            {t('sign_in_btn_cancel_back')}
          </a>
        </p>
      )}

      {!!authFlow.passwordButtonText && (
        <form method="GET" action={authFlow.passwordLink}>
          <div className="TMP mt-4 h3 font-monospace">
            <input type="hidden" name="user" value={userParam} />
            <button type="submit" className="btn btn-large btn-block btn-warning">
              {t(authFlow.passwordButtonText)}
            </button>
          </div>
        </form>
      )}
    </F>
  )

  return (
    <Card title={authFlow.title}>
      <FlashMessages messages={messages} className="rounded" messageClasses="h5 rounded" />
      {firstStep}
      {secondStep}
    </Card>
  )
}
