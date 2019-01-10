#git submodule update --recursive --init --force leihs-ui
cd leihs-ui
test -d node_modules && npm i || { npm ci || npm i ;}
npm run build
cd -

# cp leihs-ui/bootstrap-theme-leihs/build/bootstrap-leihs.css \
#    resources/all/public/my/css/site.css
#
# cp leihs-ui/dist/leihs-ui-client-side.js \
#    resources/all/public/my/leihs-shared-bundle.js
