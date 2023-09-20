import './navbar.css'
import logo_path from '../locsnap_icon.png'
import { useNavigate } from 'react-router-dom'

const Navbar = () => {

    const navigate = useNavigate()

    return (
        <nav>
            <div id='logo' onClick={() => navigate('/dashboard')}>
                <img id='logo-img' src={logo_path}></img>
                <h1 id='logo-txt'>LocSnap</h1>
            </div>
            <div className='nav-content'>
                <div className='nav-button' onClick={() => navigate('/upload')}>Upload photo</div>
                <div className='nav-button' onClick={() => navigate('/geofilter')}>Upload geo filter</div>
                <div className='nav-button' onClick={() => navigate('/generate')}>Generate map</div>
            </div>
        </nav>
    )
}

export default Navbar;