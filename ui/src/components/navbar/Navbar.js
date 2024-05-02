import f from 'lodash'
import assert from 'assert'
import React, { Fragment as F } from 'react'
import cx from 'classnames'
import {
  Collapse,
  NavbarToggler,
  NavbarBrand,
  Nav,
  NavItemLink,
  UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem as BsDropdownItem
} from '../Bootstrap'
import { Navbar as BsNavbar } from 'reactstrap'
import Icon from '../Icons'
import { Translator as T } from '../../locale/translate'

const defaults = {
  homeUrl: '/'
}

const DropdownItem = p => <BsDropdownItem data-trigger {...p} />

const Brand = ({ title }) => (
  <F>
    {/* <Icon.LeihsLogo className="mr-2" /> */}
    {title}
  </F>
)

export default class Navbar extends React.Component {
  static defaultProps = {
    config: {}
  }

  state = {
    isOpen: false
  }
  toggleOpen() {
    this.setState({
      isOpen: !this.state.isOpen
    })
  }

  render({ props, state } = this) {
    const { hideSignInField = false, config, brand, children, csrfToken } = props
    const { me, appTitle, appMenu, subApps, returnTo } = config
    const user = f.get(me, 'user')
    const isLoggedIn = f.get(user, 'id')
    const { homeUrl } = defaults
    const t = T(config.locales)

    // NOTE: when not logged in, navbar is always expanded (so login button is always visible)
    // FIXME: <BsNavbar expand={…}/> does not work as expected, results in `navbar-expand-true`(!) <https://github.com/reactstrap/reactstrap/blob/master/src/Navbar.js#L28>
    const expandClass = isLoggedIn ? 'navbar-expand-sm' : 'navbar-expand'

    return (
      <BsNavbar dark color="dark" className={cx('navbar-leihs ui-main-nav', expandClass, props.className)}>
        {brand ? (
          brand
        ) : (
          <NavbarBrand href={homeUrl}>
            <Brand title={appTitle} />
          </NavbarBrand>
        )}

        <NavbarToggler onClick={e => this.toggleOpen()} />

        <Collapse isOpen={state.isOpen} navbar>
          <Nav className="mr-auto" navbar>
            {children}
            {f.map(appMenu, ({ title, href, icon, active, submenu, attr }, i) => {
              const IconEl = Icon[icon]
              return (
                <NavItemLink key={i} href={href} active={active} {...attr}>
                  {IconEl ? <IconEl fixedWidth spaced /> : false} {title}
                </NavItemLink>
              )
            })}
          </Nav>

          <Nav className="ml-auto" navbar>
            <SubAppDropdown t={t} subApps={subApps} />

            {f.isEmpty(user) ? (
              !!hideSignInField || <NavbarLogin locales={config.locales} returnTo={returnTo} csrfToken={csrfToken} />
            ) : (
              <UserMenu t={t} user={user} csrfToken={csrfToken} />
            )}

            <LocalesDropdown locales={config.locales} isLoggedIn={isLoggedIn} csrfToken={csrfToken} />
          </Nav>
        </Collapse>
      </BsNavbar>
    )
  }
}

const UserMenu = ({ t, user, csrfToken }) => (
  <UncontrolledDropdown nav inNavbar>
    <DropdownToggle nav caret>
      <Icon.User size="lg" />
    </DropdownToggle>

    <DropdownMenu right>
      <DropdownItem tag="span" disabled className="text-body">
        <b>{decorateUser(user)}</b>
      </DropdownItem>
      <DropdownItem divider />
      <DropdownItem tag="a" href="/borrow/current-user">
        {t('navbar_user_mydata')}
      </DropdownItem>
      <DropdownItem tag="a" href="/borrow/current-user">
        {t('navbar_user_mydocs')}
      </DropdownItem>
      <DropdownItem divider />
      <form action="/sign-out" method="POST">
        <DropdownItem tag="button" type="submit">
          <CsrfTokenField {...csrfToken} />
          {t('navbar_user_logout')}
        </DropdownItem>
      </form>

      {/* <DropdownItem>{tmpUserInfo({ me })}</DropdownItem> */}
    </DropdownMenu>
  </UncontrolledDropdown>
)

const SubAppDropdown = ({ t, subApps }) => {
  const otherPermittedSubapps = f.filter(f.toPairs(subApps), kv => {
    if (f.isArray(kv[1])) {
      return kv[1].length > 0
    } else {
      return kv[1]
    }
  })

  return (
    otherPermittedSubapps.length > 0 && (
      <UncontrolledDropdown nav inNavbar>
        <DropdownToggle nav caret>
          <Icon.LeihsProcurement />
        </DropdownToggle>
        <DropdownMenu right>
          {f.map(f.keys(f.fromPairs(f.filter(f.toPairs(subApps), '1'))), (subApp, i, a) => {
            const withDivider = i + 1 < a.length // not if last
            let item

            if (subApp === 'borrow')
              item = (
                <DropdownItem href="/borrow">
                  <Icon.LeihsBorrow /> {t('app_name_borrow')}
                </DropdownItem>
              )

            if (subApp === 'admin')
              item = (
                <DropdownItem href="/admin/">
                  <Icon.LeihsAdmin /> {t('app_name_admin')}
                </DropdownItem>
              )

            if (subApp === 'procure')
              item = (
                <DropdownItem href="/procure">
                  <Icon.LeihsProcurement /> {t('app_name_procure')}
                </DropdownItem>
              )

            if (subApp === 'manage')
              item = f.isEmpty(subApps['manage']) ? (
                // <DropdownItem href="/manage">
                //   <Icon.LeihsManage /> {t('app_name_manage')}
                // </DropdownItem>
                false
              ) : (
                <F>
                  <DropdownItem header>
                    <Icon.LeihsManage /> {t('app_name_manage')}
                  </DropdownItem>
                  {f.map(subApps.manage, ({ name, href }, i) => (
                    <DropdownItem tag="a" href={href} key={i}>
                      {name}
                    </DropdownItem>
                  ))}
                </F>
              )

            if (subApp === 'styleguide')
              item = (
                <DropdownItem href="/">
                  <Icon.LeihsStyleguide /> {t('app_name_styleguide')}
                </DropdownItem>
              )

            return (
              <F key={i}>
                {item}
                {withDivider && <DropdownItem divider />}
              </F>
            )
          })}
        </DropdownMenu>
      </UncontrolledDropdown>
    )
  )
}

const LocalesDropdown = ({ locales, isLoggedIn, csrfToken }) => {
  // NOTE: `locale` is the pkey (instead of `id`)
  if (f.isEmpty(locales)) return false
  const currentLang = f.find(locales, l => l.isSelected) || f.find(locales, l => l.isDefault)
  return (
    <form method="POST" action={isLoggedIn ? '/my/user/me' : '/my/language'} className="ui-lang-selection">
      <UncontrolledDropdown nav inNavbar>
        <DropdownToggle nav caret>
          <Icon.Language />
        </DropdownToggle>
        <DropdownMenu right>
          {/* <DropdownItem divider >Sprachen</DropdownItem> */}
          {f.map(locales, lang => {
            const isCurrent = !!currentLang && lang.locale === currentLang.locale
            return (
              <DropdownItem
                key={lang.locale}
                tag="button"
                type="submit"
                name="locale"
                value={lang.locale}
                disabled={isCurrent}
                className={cx({ 'text-dark ui-selected-lang': isCurrent })}
              >
                {isCurrent ? <b>{lang.name}</b> : lang.name}
              </DropdownItem>
            )
          })}
        </DropdownMenu>
      </UncontrolledDropdown>
      <CsrfTokenField {...csrfToken} />
    </form>
  )
}

const CsrfTokenField = ({ name, value, isOptional = false }) => {
  if (!isOptional) assert(value)
  return <input type="hidden" name={name || 'csrf-token'} value={value} />
}

export const NavbarLogin = ({ returnTo, formAction, requireUserInput = false, locales, csrfToken }) => {
  const t = T(locales)

  return (
    <form className="ui-form-signin form-inline my-0" action={formAction} method="POST">
      <div className="input-group">
        <input
          name="user"
          type="text"
          className="form-control"
          placeholder={t('sign_in_nav_userparam_label')}
          aria-label={t('sign_in_nav_userparam_label')}
          aria-describedby="button-addon2"
          required={requireUserInput}
          autoCapitalize="off"
          autoCorrect="off"
          onChange={e => (e.target.value = e.target.value.trim())}
        />
        <div className="input-group-append">
          <button className="btn btn-success" type="submit" id="button-addon2">
            Login
          </button>
        </div>
      </div>
      <CsrfTokenField {...csrfToken} />
      {returnTo && <input type="hidden" name="return-to" value={returnTo} />}
    </form>
  )
}
NavbarLogin.defaultProps = {
  formAction: '/sign-in'
}

function decorateUser(u) {
  if (u.firstname && u.lastname) {
    return `${f.first(u.firstname)}. ${u.lastname}`
  }
  return f.first(
    f.filter(
      ['lastname', 'login', 'email', 'id'].map(key => f.get(u, key)),
      i => !f.isEmpty(i)
    )
  )
}
