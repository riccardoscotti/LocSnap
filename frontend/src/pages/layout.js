import { Outlet } from 'react-router-dom';
import Navbar from '../components/navbar';

import '../css/layout.css';
const Layout = () => {
    return (
        <>
            <Navbar />
            <Outlet />
        </>
    )
}

export default Layout;