import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useState, createContext } from "react";
import Layout from "./components/layout/Layout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ExpensesList from "./pages/ExpensesList";
import ExpenseForm from "./pages/ExpenseForm";
import Installments from "./pages/Installments";
import Categories from "./pages/Categories";

export const AuthContext = createContext(null);

function App() {
  const [user, setUser] = useState(null);

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="expenses" element={<ExpensesList />} />
            <Route path="expenses/new" element={<ExpenseForm />} />
            <Route path="expenses/:id/edit" element={<ExpenseForm />} />
            <Route path="installments" element={<Installments />} />
            <Route path="categories" element={<Categories />} />
          </Route>
        </Routes>
      </Router>
    </AuthContext.Provider>
  );
}

export default App;
