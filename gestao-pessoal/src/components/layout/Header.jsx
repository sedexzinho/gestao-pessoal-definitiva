import { useContext } from "react";
import { AuthContext } from "../../App";

export default function Header({ onMenuClick }) {
  const { user, logout } = useContext(AuthContext);

  return (
    <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 fixed top-0 left-0 right-0 z-40">
      <div className="px-4 py-3 flex items-center justify-between">
        <div className="flex items-center">
          <h1 className="ml-16 text-xl font-semibold text-gray-900 dark:text-white">
            Gestão Pessoal
          </h1>
        </div>

        <div className="flex items-center space-x-4">
          <div className="flex items-center">
            <span className="text-sm text-gray-700 dark:text-gray-300 mr-2">
              {user?.name || user?.email}
            </span>
            <div className="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center text-white text-sm font-medium">
              {user?.name?.[0] || user?.email?.[0]?.toUpperCase()}
            </div>
          </div>
          <button
            onClick={logout}
            className="p-2 rounded-lg text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700"
            title="Sair"
          >
            <svg
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
          </button>
        </div>
      </div>
    </header>
  );
}
