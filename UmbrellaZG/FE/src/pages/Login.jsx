import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { ArrowLeft, User, Lock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { adminLogin } from '@/services/api-adjusted';

const Login = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // 调用登录接口
      const response = await adminLogin({ username, password });
      // 存储token
      localStorage.setItem('token', response.token);
      // 登录成功后跳转到首页
      navigate('/');
    } catch (error) {
      console.error('登录失败:', error);
      alert('登录失败，请检查用户名和密码');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <Card className="w-full max-w-md">
        <CardHeader>
          <Button 
            variant="ghost" 
            className="w-fit p-0 mb-4"
            onClick={() => navigate('/')}
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            返回首页
          </Button>
          <CardTitle className="text-2xl text-center">登录</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <div className="relative">
                <User className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                <Input 
                  type="text" 
                  placeholder="用户名" 
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>
            <div className="space-y-2">
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                <Input 
                  type="password" 
                  placeholder="密码" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
            </div>
            <Button type="submit" className="w-full" disabled={!username || !password}>
              登录
            </Button>
          </form>
          <div className="mt-4 text-center text-sm text-gray-600">
            <p>默认游客身份访问，无需登录</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Login;
