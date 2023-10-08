import { Outlet, Navigate } from 'react-router-dom';
import Navbar from '../components/navbar';

import '../css/layout.css';
const Layout = () => {
    if(localStorage.getItem("user") !== null) {
        return (
            <>
                <Navbar />
                <Outlet />
            </> 
        )
    } else {
        alert("You must be logged first!")
        return <Navigate to='/login' />
    }
}

export default Layout;