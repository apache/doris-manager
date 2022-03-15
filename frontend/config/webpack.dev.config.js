// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const webpackBaseConfig = require("./webpack.base.config");
const { merge } = require("webpack-merge");
const SERVER_RD = 'http://127.0.0.1:8880';

module.exports = merge(webpackBaseConfig, {
  mode: "development",
  devtool: "eval-source-map",
  // 开发服务配置
  devServer: {
    host: "localhost",
    port: 8084,
    publicPath: "/",
    disableHostCheck: true,
    useLocalIp: false,
    open: true,
    overlay: true,
    hot: true,
    stats: {
      assets: false,
      colors: true,
      modules: false,
      children: false,
      chunks: false,
      chunkModules: false,
      entrypoints: false,
    },
    proxy: {
      '/api': {
          target: SERVER_RD,
          changeOrigin: true,
          secure: false,
          // pathRewrite: {'^/api': '/api'}
      },
    },
    historyApiFallback: true,
  },
});
