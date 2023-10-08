import './navbar.css'
import logo_path from '../locsnap_icon.png'
import { useNavigate } from 'react-router-dom'
import {useRef} from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';

const Navbar = () => {

    const navigate = useNavigate()
    const uploadFilterRef = useRef(null);

    const uploadFilter = event => {

        const fileObj = event.target.files[0];
        const reader = new FileReader();

        reader.onload = function() {
            localStorage.setItem("geojson", reader.result)
        }
        reader.readAsText(fileObj)

        event.target.value = null;
    }

    const logout = () => {
        localStorage.removeItem("user")
        navigate('/login')
    }

    return (
        <nav>
            <div id='logo' onClick={() => navigate('/dashboard')}>
                <img id='logo-img' src={logo_path}></img>
                <h1 id='logo-txt'>LocSnap</h1>
            </div>
            <input
                style={{display: 'none'}}
                ref={uploadFilterRef}
                type="file"
                accept=".geojson, .json" 
                onChange={uploadFilter}
            />
            <div className='nav-content'>
                <div className='nav-button' onClick={() => navigate('/upload')}>Upload photo</div>
                <div className='nav-button' onClick={() => uploadFilterRef.current.click()}>Upload geo filter</div>
                <div className='nav-button' onClick={() => navigate('/generate')}>Generate map</div>
                <div className='nav-button' onClick={() => logout()} id='logout'>Logout</div>
            </div>
        </nav>
    )
}

export default Navbar;