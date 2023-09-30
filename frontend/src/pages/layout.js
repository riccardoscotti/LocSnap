import { Outlet, Navigate } from 'react-router-dom';
import Navbar from '../components/navbar';
import { useLocalStorage } from '../hooks/useLocalStorage';

import '../css/layout.css';
const Layout = () => {
    const [user, setUser] = useLocalStorage("user", null)
    if(user) {
        
        return (
            <>
                <Navbar />
                <Outlet />
            </> 
        )
    }

    alert("You must be logged first!")
    return <Navigate to='/login' />

}

export default Layout;