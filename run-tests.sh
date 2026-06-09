#!/bin/bash

echo "========================================"
echo "运行单元测试和生成覆盖率报告"
echo "========================================"

echo "1. 清理之前的构建..."
mvn clean

echo "2. 运行测试并生成覆盖率报告..."
mvn test jacoco:report

echo "3. 生成测试报告..."
if [ -d "target/surefire-reports" ]; then
    echo "测试报告已生成在: target/surefire-reports/"
else
    echo "未找到测试报告目录"
fi

echo "4. 生成覆盖率报告..."
if [ -d "target/site/jacoco" ]; then
    echo "覆盖率报告已生成在: target/site/jacoco/index.html"
    echo "可以在浏览器中打开查看覆盖率详情"
else
    echo "未找到覆盖率报告目录"
fi

echo "5. 显示测试结果摘要..."
if ls target/surefire-reports/TEST-*.xml 1> /dev/null 2>&1; then
    echo "测试执行完成，详细结果请查看报告文件"
else
    echo "测试可能未正常执行"
fi

echo "========================================"
echo "测试运行完成！"
echo "========================================"