# My UI

- UI components (React) and theme (based on Bootstrap) `leihs-my` app.
- Storybook holding the components for `leihs-my`, as well as a few components (notably `Navbar`) which are distributed to other apps (`leihs-admin`, `procure`).

## Stack

- React 17
- Bootstrap 4
- Storybook 7
- Webpack 5
- Babel
- Prettier
- ESLint

## Artifact output paths

- `dist/my-ui.js`: Component library
- `dist/my-ui.css`: Theme (styles)

## Guide

### Basics

Modes of development:

- Start Storybook (`npm run storybook`)
- Start UI library in watch mode (`npm run watch`), then start `leihs-my` app (`../bin/cljs-watch`)  
  Changes in components will automatically reflect in `leihs-my` app. Changes in SCSS require a browser reload.
- (or both together)

Note that Storybook has its own build chain and is not affected by `webpack.config.js`.

### Storybook

- `npm run storybook`: Start storybook (http://localhost:6007)
- `npm run build-storybook`: Build deployable (goes to `storybook-static` folder)

### Lint and format

Note that currently only Prettier's rule definitions are configured in ESLint.

- `npm run lint`: Lint all files
- `npm run prettier`: Autoformat all files

### Library development and build

- `npm run watch`: Start theme and lib in watch/dev mode (use along with watch mode in My app)
- `npm run build`: Build theme and lib for production
