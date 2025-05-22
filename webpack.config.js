const path = require('path');

module.exports = {
  mode: 'development',
  entry: {
    adminDashboard: './src/main/resources/static/js/admin/AdminDashboard.js',
  },
  output: {
    filename: '[name].bundle.js',
    path: path.resolve(__dirname, 'src/main/resources/static/dist'),
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env', '@babel/preset-react'],
          },
        },
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader', 'postcss-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
};
