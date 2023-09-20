import './navbar1.css'
import logo_path from '../locsnap_icon.png'

const Navbar1 = () => {
    return (
        <nav id="main-nav">
            <img id='logo-img' src={logo_path}/>
            <h1 id='logo-txt'>LocSnap</h1>
            <div className='nav-content'>
                <div className="navContent" id='nav-button-upload'>Upload photo</div>
                <div className="navContent" id='nav-button-geofilter'>Upload geo filter</div>
                <div className="navContent" id='nav-button-map'>Generate map</div>
            </div>
        </nav>
    )
}

export default Navbar1;