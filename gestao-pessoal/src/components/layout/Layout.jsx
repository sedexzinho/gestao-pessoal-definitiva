import { Outlet, Navigate } from "react-router-dom";
import { useContext, useState } from "react";
import { AuthContext } from "../../App";
import Header from "./Header";
import Sidebar from "./Sidebar";

export default function Layout() {
  const { user } = useContext(AuthContext);
  const [menuVisible, setMenuVisible] = useState(true);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Botão escMenu (3 linhas) sempre visível */}
      <button
        onClick={() => setMenuVisible(!menuVisible)}
        className="fixed top-4 left-4 z-50 p-2 rounded-lg text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700 bg-white dark:bg-gray-800 shadow-md"
      >
        <svg
          className="h-6 w-6"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M4 6h16M4 12h16M4 18h16"
          />
        </svg>
      </button>

      {menuVisible && (
        <Header onMenuClick={() => setMenuVisible(!menuVisible)} />
      )}

      <div className="flex">
        <Sidebar isOpen={menuVisible} onClose={() => setMenuVisible(false)} />
        <main
          className={`flex-1 transition-all duration-300 ${menuVisible ? "ml-64" : "ml-16"} ${menuVisible ? "mt-16" : "mt-16"}`}
        >
          <div className="p-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
