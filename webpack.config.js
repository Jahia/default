const path = require('path');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const {ProvidePlugin} = require('webpack');
const {globSync} = require('glob');
const packageJson = require('./package.json');

module.exports = (env, argv) => {
    const appsFolder = 'src/main/resources/javascript/apps';
    const debug = argv.mode !== 'production';
    console.log(`mode: ${argv.mode}`);

    // Create entry for each src/javascript file
    const entry = globSync(`src/javascript/**js`, {withFileTypes: true}).reduce((obj, jsPath) => {
        const jsName = path.parse(jsPath.name).name; // remove file extension
        obj[jsName] = {
            import: path.resolve(__dirname, jsPath.relative()),
            dependOn: 'jquery'
        }
        return obj;
    }, {jquery: ['jquery']});

    let config = {
        mode: argv.mode || 'development',
        devtool: debug ? 'source-map' : undefined,
        entry,
        output: {
            path: path.resolve(__dirname, appsFolder),
            filename: `${packageJson.name}.[name].bundle.js`,
            library: '[name]Lib'
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
