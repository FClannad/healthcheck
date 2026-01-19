#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
健康体检系统 - 爬虫功能测试工具
独立测试爬虫服务，无需启动完整的Spring Boot应用
"""

import requests
import json
import time
import sys
from datetime import datetime

class CrawlerTestTool:
    def __init__(self, base_url="http://localhost:9090"):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'User-Agent': 'Health-Check-System-Crawler-Test/1.0'
        })

    def test_backend_health(self):
        """测试后端健康状态"""
        try:
            print("🔍 测试后端连接...")
            response = self.session.get(f"{self.base_url}/actuator/health", timeout=10)
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 后端服务正常: {data.get('status', 'UP')}")
                return True
            else:
                print(f"❌ 后端服务异常: HTTP {response.status_code}")
                return False
        except requests.exceptions.RequestException as e:
            print(f"❌ 后端连接失败: {e}")
            return False

    def test_crawler_service(self, keyword="人工智能", count=5):
        """测试爬虫服务"""
        try:
            print(f"🕷️ 测试爬虫服务 - 关键词: {keyword}, 数量: {count}")
            
            payload = {
                "keyword": keyword,
                "count": count
            }
            
            response = self.session.post(
                f"{self.base_url}/medical-literature/crawl",
                json=payload,
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"✅ 爬虫测试成功!")
                print(f"   - 响应状态: {data.get('code', 'unknown')}")
                print(f"   - 响应消息: {data.get('msg', 'no message')}")
                print(f"   - 爬取数量: {data.get('data', 0)}")
                return True
            else:
                print(f"❌ 爬虫测试失败: HTTP {response.status_code}")
                print(f"   响应内容: {response.text}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ 爬虫服务请求失败: {e}")
            return False

    def test_literature_list(self, page=1, size=5):
        """测试医疗文献列表查询"""
        try:
            print(f"📚 测试文献列表查询 - 页码: {page}, 大小: {size}")
            
            params = {
                "pageNum": page,
                "pageSize": size
            }
            
            response = self.session.get(
                f"{self.base_url}/medical-literature/list",
                params=params,
                timeout=15
            )
            
            if response.status_code == 200:
                data = response.json()
                literature_list = data.get('data', {}).get('list', [])
                total = data.get('data', {}).get('total', 0)
                
                print(f"✅ 文献列表查询成功!")
                print(f"   - 总数量: {total}")
                print(f"   - 当前页数量: {len(literature_list)}")
                
                if literature_list:
                    print("   - 最新文献:")
                    for i, lit in enumerate(literature_list[:3], 1):
                        title = lit.get('title', '无标题')[:50]
                        source = lit.get('crawlSource', '未知来源')
                        print(f"     {i}. {title}... (来源: {source})")
                
                return True
            else:
                print(f"❌ 文献列表查询失败: HTTP {response.status_code}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ 文献列表查询请求失败: {e}")
            return False

    def test_arxiv_direct(self, keyword="artificial intelligence", max_results=3):
        """直接测试arXiv API"""
        try:
            print(f"🔬 直接测试arXiv API - 关键词: {keyword}")
            
            # 构建arXiv查询URL
            query = f"all:{keyword}"
            url = f"http://export.arxiv.org/api/query?search_query={query}&start=0&max_results={max_results}"
            
            response = requests.get(url, timeout=15)
            
            if response.status_code == 200:
                print(f"✅ arXiv API测试成功!")
                print(f"   - 响应长度: {len(response.text)} 字符")
                
                # 简单解析XML内容
                content = response.text
                if "<entry>" in content:
                    entry_count = content.count("<entry>")
                    print(f"   - 找到论文条目: {entry_count} 篇")
                else:
                    print("   - 未找到论文条目")
                
                return True
            else:
                print(f"❌ arXiv API测试失败: HTTP {response.status_code}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ arXiv API请求失败: {e}")
            return False

    def run_comprehensive_test(self):
        """运行综合测试"""
        print("=" * 60)
        print("🏥 健康体检系统 - 爬虫功能综合测试")
        print("=" * 60)
        print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"目标服务: {self.base_url}")
        print()
        
        results = []
        
        # 1. 测试后端健康状态
        results.append(("后端健康检查", self.test_backend_health()))
        print()
        
        # 2. 测试arXiv API直连
        results.append(("arXiv API直连", self.test_arxiv_direct()))
        print()
        
        # 3. 测试爬虫服务
        test_keywords = ["人工智能", "machine learning", "医疗健康"]
        for keyword in test_keywords:
            result = self.test_crawler_service(keyword, 3)
            results.append((f"爬虫测试({keyword})", result))
            time.sleep(2)  # 避免请求过于频繁
            print()
        
        # 4. 测试文献列表查询
        results.append(("文献列表查询", self.test_literature_list()))
        print()
        
        # 输出测试总结
        print("=" * 60)
        print("📊 测试结果总结")
        print("=" * 60)
        
        success_count = 0
        for test_name, success in results:
            status = "✅ 通过" if success else "❌ 失败"
            print(f"{test_name:<20} {status}")
            if success:
                success_count += 1
        
        print()
        print(f"总测试项: {len(results)}")
        print(f"通过项数: {success_count}")
        print(f"成功率: {success_count/len(results)*100:.1f}%")
        
        if success_count == len(results):
            print("\n🎉 所有测试通过！爬虫系统运行正常！")
        elif success_count > len(results) // 2:
            print("\n⚠️ 部分测试通过，系统基本可用，建议检查失败项。")
        else:
            print("\n🚨 多项测试失败，请检查系统配置和服务状态。")

def main():
    """主函数"""
    import argparse
    
    parser = argparse.ArgumentParser(description="健康体检系统爬虫测试工具")
    parser.add_argument("--url", default="http://localhost:9090", 
                       help="后端服务地址 (默认: http://localhost:9090)")
    parser.add_argument("--keyword", default="人工智能", 
                       help="测试关键词 (默认: 人工智能)")
    parser.add_argument("--count", type=int, default=5, 
                       help="爬取数量 (默认: 5)")
    parser.add_argument("--quick", action="store_true", 
                       help="快速测试模式")
    
    args = parser.parse_args()
    
    tester = CrawlerTestTool(args.url)
    
    if args.quick:
        # 快速测试模式
        print("🚀 快速测试模式")
        tester.test_backend_health()
        tester.test_crawler_service(args.keyword, args.count)
    else:
        # 综合测试模式
        tester.run_comprehensive_test()

if __name__ == "__main__":
    main()
