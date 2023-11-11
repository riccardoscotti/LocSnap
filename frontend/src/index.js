import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import './css/index.css';
import Dashboard from './pages/dashboard';
import Layout from './pages/layout'
// import HomeScreen from './pages/homescreen'
import Login from './pages/login';
import UploadPhoto from './pages/uploadPhoto';
import GenerateMap from './pages/generateMap';
import Explore from './pages/explore'
import Social from './pages/social'

// import { ProtectedRoute } from './components/protectedRoute'

export default function App() {
  return (
    <React.StrictMode>
      <BrowserRouter>
        <Routes>
          <Route path='/login' element={<Login />} />
        </Routes>
        <Routes>
          <Route path='/' element={<Layout />} >
            <Route path='/' element={<Navigate to='/dashboard' />} />
            <Route path='/dashboard' element={<Dashboard />} />
            <Route path='/upload' element={<UploadPhoto />} />
            <Route path='/generate' element={<GenerateMap />} />
            <Route path='/explore' element={<Explore />} />
            <Route path='/social' element={<Social />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </React.StrictMode>
  )
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);