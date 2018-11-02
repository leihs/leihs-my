#git submodule update --recursive --init --force leihs-ui
cd leihs-ui
npm ci || npm i
npm run build
npm run build-lib
cd -

cp leihs-ui/bootstrap-theme-leihs/build/bootstrap-leihs.css \
   resources/all/public/my/css/site.css

cp leihs-ui/dist/leihs-ui-client-side.js \
   resources/all/public/my/leihs-shared-bundle.js
