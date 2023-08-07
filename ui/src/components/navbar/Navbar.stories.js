import React from 'react'

import Navbar from './Navbar'
import LeihsPage from '../LeihsPage'
import sampleNavbarProps from './sampleProps.json'

const { config: sampleConfig, csrfToken } = sampleNavbarProps
const { locales } = sampleConfig

const richNavbarConfig = {
  locales,

  me: {
    user: {
      id: '5443e104-d80e-497f-bc15-927ff5c2d396',
      firstname: 'Normin',
      lastname: 'Normalo',
      login: 'nnormalo',
      email: 'normin.normalo@zhdk.ch'
      // selectedLocale: '9a12cfd0-087d-56c4-ae4c-c6004f6adbf4'
    }
  },

  subApps: {
    borrow: true,
    admin: true,
    procure: true,
    manage: [
      {
        name: 'Ausleihe Toni-Areal',
        href: '/manage/7df25853-2590-452d-a35f-0a3a9c73e36b/daily'
      },
      {
        name: 'ITZ-Software',
        href: '/manage/b3030cf8-c920-4eee-8f01-90855b291e0e/daily'
      }
    ],
    styleguide: false
  },
  appTitle: 'LeihsApp',
  appMenu: [
    {
      title: 'AAA',
      href: '/aaa',
      icon: 'Settings',
      active: false,
      attr: { 'data-foo': 'bar' }
    },
    {
      title: 'BBB',
      href: '/bbb',
      icon: 'Categories',
      active: true
    },
    {
      title: 'CCC',
      href: '/ccc',
      icon: 'Users',
      active: false
    },
    {
      title: 'Kontakt',
      href: 'http://example.com',
      icon: 'Contact',
      active: false
    }
  ]
}

export default {
  title: 'Components/Navbar'
}

export const allConfigOptions = () => {
  return (
    <LeihsPage>
      <Navbar config={richNavbarConfig} csrfToken={csrfToken} />
      <div className="px-3 py-3 pt-md-5 pb-md-4 mx-auto text-center">
        <h1 className="display-4">Great App</h1>
        <p className="lead">This is a great app with an even greater navbar.</p>
        <small>those (example) params are rendered:</small>
        <pre className="text-bold text-left p-3 mx-5 my-3 card bg-body">{JSON.stringify(richNavbarConfig, 0, 2)}</pre>
      </div>{' '}
    </LeihsPage>
  )
}

export const whenLoggedOut = () => {
  return (
    <LeihsPage>
      <Navbar {...sampleNavbarProps} />
    </LeihsPage>
  )
}

export const withoutAppMenu = () => {
  const { appTitle, subApps, me, locales } = richNavbarConfig
  const navbarProps = { csrfToken, config: { appTitle, subApps, me, locales } }
  return (
    <LeihsPage>
      <Navbar {...navbarProps} />
    </LeihsPage>
  )
}

export const noUserDefaultLang = () => {
  return (
    <LeihsPage>
      <Navbar
        config={{
          me: false,
          locales: [
            {
              id: 1,
              locale: 'en',
              name: 'EN',
              isDefault: false,
              isSelected: false
            },
            {
              id: 2,
              locale: 'de',
              name: 'DE',
              isDefault: true,
              isSelected: false
            }
          ]
        }}
        csrfToken={csrfToken}
      />
    </LeihsPage>
  )
}
noUserDefaultLang.storyName = 'No user, default lang'

export const noUserWithSelectedLang = () => {
  return (
    <LeihsPage>
      <Navbar
        config={{
          me: false,
          locales: [
            {
              id: 1,
              locale: 'en',
              name: 'EN',
              isDefault: false,
              isSelected: true
            },
            {
              id: 2,
              locale: 'de',
              name: 'DE',
              isDefault: true,
              isSelected: false
            }
          ]
        }}
        csrfToken={csrfToken}
      />
    </LeihsPage>
  )
}
noUserWithSelectedLang.storyName = 'No user, selected lang'

export const userWithoutDefaultLang = () => {
  return (
    <LeihsPage>
      <Navbar
        config={{
          me: { user: { login: 'ME' } },
          locales: [
            {
              id: 1,
              locale: 'en',
              name: 'EN',
              isDefault: false,
              isSelected: false
            },
            {
              id: 2,
              locale: 'de',
              name: 'DE',
              isDefault: true,
              isSelected: false
            }
          ]
        }}
        csrfToken={csrfToken}
      />
    </LeihsPage>
  )
}
userWithoutDefaultLang.storyName = 'User without lang setting'

export const userWithLangSetting = () => {
  return (
    <LeihsPage>
      <Navbar
        config={{
          me: { user: { login: 'ME' } },
          locales: [
            {
              id: 1,
              locale: 'en',
              name: 'EN',
              isDefault: false,
              isSelected: true
            },
            {
              id: 2,
              locale: 'de',
              name: 'DE',
              isDefault: true,
              isSelected: false
            }
          ]
        }}
        csrfToken={csrfToken}
      />
    </LeihsPage>
  )
}
userWithLangSetting.storyName = 'User with lang setting'

export const usageInSharedApp = () => {
  return (
    <LeihsPage>
      <Navbar
        config={{
          appTitle: 'leihs',
          appMenu: [
            {
              title: 'one',
              href: '/1',
              icon: 'Settings',
              active: false,
              attr: { 'data-foo': 'bar' }
            },
            {
              title: 'two',
              href: '/2',
              icon: 'Categories',
              active: true
            }
          ],
          me: {
            user: {
              name: 'Normin Normalo'
            }
          }
        }}
        csrfToken={csrfToken}
      />
    </LeihsPage>
  )
}
