import { HomeIcon, FileText, LogIn, User } from "lucide-react";
import Index from "./pages/Index.jsx";
import Article from "./pages/Article.jsx";
import Login from "./pages/Login.jsx";
import About from "./pages/About.jsx";

/**
* Central place for defining the navigation items. Used for navigation components and routing.
*/
export const navItems = [
  {
    title: "首页",
    to: "/",
    icon: <HomeIcon className="h-4 w-4" />,
    page: <Index />,
  },
  {
    title: "文章",
    to: "/article/:id",
    icon: <FileText className="h-4 w-4" />,
    page: <Article />,
  },
  {
    title: "登录",
    to: "/login",
    icon: <LogIn className="h-4 w-4" />,
    page: <Login />,
  },
  {
    title: "关于我",
    to: "/about",
    icon: <User className="h-4 w-4" />,
    page: <About />,
  },
];
