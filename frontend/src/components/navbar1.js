import './navbar1.css'
import { useNavigate } from "react-router-dom";
import logo_path from '../locsnap_icon.png'

const Navbar1 = () => {
    const navigate = useNavigate()

    document.addEventListener('DOMContentLoaded', function() {
        
        document.getElementById('nav-button-upload').addEventListener('click', function() {
          navigate("/upload")
        });

        document.getElementById('nav-button-geofilter').addEventListener('click', function() {
            navigate("/geofilter")
        });

        document.getElementById('nav-button-map').addEventListener('click', function() {
            navigate("/showmap")
        });
    });

    return (
        <nav id="main-nav">
            <img id='logo-img' src={logo_path}/>
            <h1 id='logo-txt'>LocSnap</h1>
            <div className='nav-content'>
                <div className="navContent" id='nav-button-upload'>Upload photo</div>
                <div className="navContent" id='nav-button-geofilter'>Upload GeoFilter</div>
                <div className="navContent" id='nav-button-map'>Show map</div>
            </div>
        </nav>
    )
}

export default Navbar1;