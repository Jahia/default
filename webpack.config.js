const path = require('path');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const {ProvidePlugin} = require('webpack');
const packageJson = require('./package.json');

module.exports = (env, argv) => {
    const appsFolder = 'src/main/resources/javascript/apps';
    let config = {
        mode: 'development',
        entry: {
            main: path.resolve(__dirname, 'src/javascript/index')
        },
        output: {
            path: path.resolve(__dirname, appsFolder),
            filename: `${packageJson.name}.bundle.js`,
            library: 'MyLibrary'
        },
        plugins: [
            new ProvidePlugin({
                'window.jQuery': 'jquery',
                'window.$': 'jquery',
                'jQuery': 'jquery',
                '$': 'jquery',
            }),
            new CleanWebpackPlugin({
                cleanOnceBeforeBuildPatterns: [`${path.resolve(__dirname, appsFolder)}/**/*`],
                verbose: false
            })
        ],
        resolve: {
            alias: {
                'jquery': 'jquery/src/jquery',
                'jquery-validation': 'jquery-validation/dist/jquery.validate.js'
              },
              modules: [
                path.resolve(__dirname, 'src/javascript'),
                path.resolve(__dirname, 'node_modules')
              ]
        }
    };
    return config;
}
