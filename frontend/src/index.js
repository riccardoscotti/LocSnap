import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from "react-router-dom";


import './index.css';
import Dashboard from './pages/dashboard';
import Layout from './pages/layout'
import HomeScreen from './pages/homescreen'
import Login from './pages/login';

import { ProtectedRoute } from './components/protectedRoute'

export default function App() {
  return (
    <React.StrictMode>
      <BrowserRouter>
        <Routes>
          <Route path='/' element={<Layout />} >
            <Route path='/dashboard' element={<Dashboard />} />
            <Route path='/login' element={<Login/>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </React.StrictMode>
  )
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);