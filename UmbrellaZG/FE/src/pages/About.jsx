import React from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowLeft, Github, Mail, Twitter } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const About = () => {
  const navigate = useNavigate();

  return (
    <div className="container mx-auto px-2 sm:px-4 py-6 sm:py-8 max-w-2xl sm:max-w-4xl">
      <Button
        variant="outline"
        className="mb-6 flex items-center text-sm sm:text-base px-3 sm:px-6 py-1 sm:py-2"
        onClick={() => navigate('/')}
      >
        <ArrowLeft className="w-4 h-4 mr-2" />
        返回首页
      </Button>

      <div className="space-y-8">
        <div className="text-center">
          <img
            src="https://nocode.meituan.com/photo/search?keyword=developer,portrait&width=200&height=200"
            alt="个人头像"
            className="mx-auto object-cover w-24 h-24 sm:w-32 sm:h-32 rounded-full mb-4 shadow-lg border"
          />
          <h1 className="text-2xl sm:text-3xl font-bold mb-2">关于我</h1>
          <p className="text-lg sm:text-xl text-gray-600">全栈开发者 & 技术博主</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>个人简介</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-700 leading-relaxed text-sm sm:text-base">
              我是一名热爱技术的全栈开发者，专注于前端和后端技术的研究与实践。
              在这个博客中，我会分享我在开发过程中遇到的问题、解决方案以及对技术的思考。
              希望能够通过分享帮助到更多的开发者，同时也欢迎大家与我交流讨论。
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>技术栈</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 sm:gap-4">
              <div className="text-center p-3 sm:p-4 bg-blue-50 rounded-lg">
                <h3 className="font-semibold text-blue-800">前端</h3>
                <p className="text-xs sm:text-sm text-blue-600 mt-2">React, Vue, TypeScript</p>
              </div>
              <div className="text-center p-3 sm:p-4 bg-green-50 rounded-lg">
                <h3 className="font-semibold text-green-800">后端</h3>
                <p className="text-xs sm:text-sm text-green-600 mt-2">Node.js, Python, Java</p>
              </div>
              <div className="text-center p-3 sm:p-4 bg-purple-50 rounded-lg">
                <h3 className="font-semibold text-purple-800">数据库</h3>
                <p className="text-xs sm:text-sm text-purple-600 mt-2">MySQL, MongoDB, Redis</p>
              </div>
              <div className="text-center p-3 sm:p-4 bg-orange-50 rounded-lg">
                <h3 className="font-semibold text-orange-800">云服务</h3>
                <p className="text-xs sm:text-sm text-orange-600 mt-2">AWS, Docker, Kubernetes</p>
              </div>
              <div className="text-center p-3 sm:p-4 bg-red-50 rounded-lg">
                <h3 className="font-semibold text-red-800">工具</h3>
                <p className="text-xs sm:text-sm text-red-600 mt-2">Git, VS Code, Figma</p>
              </div>
              <div className="text-center p-3 sm:p-4 bg-indigo-50 rounded-lg">
                <h3 className="font-semibold text-indigo-800">其他</h3>
                <p className="text-xs sm:text-sm text-indigo-600 mt-2">GraphQL, REST API</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>联系方式</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3 sm:gap-4">
              <Button variant="outline" className="flex items-center text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2">
                <Mail className="w-4 h-4 mr-2" />
                邮箱联系
              </Button>
              <Button variant="outline" className="flex items-center text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2">
                <Github className="w-4 h-4 mr-2" />
                GitHub
              </Button>
              <Button variant="outline" className="flex items-center text-xs sm:text-base px-3 sm:px-6 py-1 sm:py-2">
                <Twitter className="w-4 h-4 mr-2" />
                Twitter
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>博客统计</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 text-center">
              <div>
                <div className="text-xl sm:text-2xl font-bold text-blue-600">50+</div>
                <div className="text-xs sm:text-sm text-gray-600">技术文章</div>
              </div>
              <div>
                <div className="text-xl sm:text-2xl font-bold text-green-600">10K+</div>
                <div className="text-xs sm:text-sm text-gray-600">总阅读量</div>
              </div>
              <div>
                <div className="text-xl sm:text-2xl font-bold text-purple-600">2年</div>
                <div className="text-xs sm:text-sm text-gray-600">写作经验</div>
              </div>
              <div>
                <div className="text-xl sm:text-2xl font-bold text-orange-600">100+</div>
                <div className="text-xs sm:text-sm text-gray-600">技术分享</div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default About;
