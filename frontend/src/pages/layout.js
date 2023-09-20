import { Outlet } from 'react-router-dom';
import Navbar1 from '../components/navbar1';
import Navbar2 from '../components/navbar2';

import './layout.css';
const Layout = () => {
    return (
        <>
            <Navbar1 />
            <Navbar2 />
            <Outlet />
        </>
    )
}

export default Layout;