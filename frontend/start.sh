#!/bin/bash

echo "启动景德镇旅游智能推荐系统前端..."
echo

echo "检查 Node.js 环境..."
if ! command -v node &> /dev/null; then
    echo "错误: 未找到 Node.js，请先安装 Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

echo "检查 npm 环境..."
if ! command -v npm &> /dev/null; then
    echo "错误: 未找到 npm，请检查 Node.js 安装"
    exit 1
fi

echo "检查项目依赖..."
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "错误: 依赖安装失败"
        exit 1
    fi
fi

echo "启动开发服务器..."
echo "前端地址: http://localhost:5173"
echo "请确保后端服务运行在 http://localhost:8888"
echo
echo "按 Ctrl+C 停止服务器"
echo

npm run dev