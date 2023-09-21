import './navbar.css'
import logo_path from '../locsnap_icon.png'
import { useNavigate } from 'react-router-dom'
import {useRef} from 'react';

const Navbar = () => {

    const navigate = useNavigate()
    const uploadFilterRef = useRef(null);

    const uploadFilter = event => {
        const fileObj = event.target.files && event.target.files[0];
        if (!fileObj)
            return;
        
        event.target.value = null;
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
                accept=".geojson" 
                onChange={uploadFilter}
            />
            <div className='nav-content'>
                <div className='nav-button' onClick={() => navigate('/upload')}>Upload photo</div>
                <div className='nav-button' onClick={() => uploadFilterRef.current.click()}>Upload geo filter</div>
                <div className='nav-button' onClick={() => navigate('/generate')}>Generate map</div>
            </div>
        </nav>
    )
}

export default Navbar;