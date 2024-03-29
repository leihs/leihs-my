import '../src/styles/styles.scss'
import './fake-time'

/** @type { import('@storybook/react').Preview } */
const preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/
      }
    },
    options: {
      storySort: {
        order: ['Overview', 'Features', 'Components', 'Lib']
      }
    }
  }
}

export default preview
