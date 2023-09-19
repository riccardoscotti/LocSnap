import './navbar.css'
import logo_path from '../locsnap_icon.png'

const Navbar = () => {
    return (
        <nav>
            <div id='logo'>
                <img id='logo-img' src={logo_path}></img>
                <h1 id='logo-txt'>LocSnap</h1>
            </div>
            <div className='nav-content'>
                <div className='nav-button'>Upload photo</div>
                <div className='nav-button'>Upload geo filter</div>
                <div className='nav-button'>Generate map</div>
            </div>
        </nav>
    )
}

export default Navbar;