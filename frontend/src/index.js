import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from "react-router-dom";

// per l'autenticazione
import { createContext, useContext, useMemo } from 'react';

import './index.css';
import Dashboard from './pages/dashboard';
import Layout from './pages/layout'
import Login from './pages/login';

export default function App() {
  return (
    <React.StrictMode>
      <BrowserRouter>
        <Routes>
          <Route path='/' element={<Layout />} />
          <Route path='/login' element={<Login />} />
        </Routes>
      </BrowserRouter>
    </React.StrictMode>
  )
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);