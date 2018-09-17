html-minifier __src/index.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/index.ejs
html-minifier __src/map.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/map.ejs
html-minifier __src/sign-up.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/sign-up.ejs
html-minifier __src/help.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/help.ejs
html-minifier __src/brand.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/brand.ejs
html-minifier __src/contact.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/contact.ejs
html-minifier __src/about.html --collapse-whitespace --conservative-collapse --remove-comments --minify-css --minify-js -> views/about.ejs

git add . && git commit -m "whatever" && git push