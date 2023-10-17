import './navbar.css'
import logo_path from '../locsnap_icon.png'
import Button from 'react-bootstrap/Button';
import { useNavigate } from 'react-router-dom'
import React, { useRef, useState, createRef, useEffect } from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';
import FormControlLabel from '@mui/material/FormControlLabel';
import Modal from 'react-bootstrap/Modal';
import Checkbox from '@mui/material/Checkbox';
import axios from 'axios'
import Social from '../utils/social_funcs'

const Navbar = () => {

    const [friendsDialogStatus, openFriendsDialog] = React.useState(false);
    const [friendTextStatus, openFriendsText] = React.useState(false);
    const navigate = useNavigate()
    const uploadFilterRef = useRef(null)

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
                <div className='nav-button' onClick={() => navigate('/generate')}>Generate map</div>
                <div className='nav-button' onClick={() => {}}>Manage your photos</div>
                <div className='nav-button' onClick={() => navigate('/explore')}>Explore</div>
                <div className='nav-button' onClick={() => {openFriendsDialog(true)}}>
                    Social
                </div>
                {/* <div className='nav-button' onClick={() => navigate('/social')}>Social</div> */}
                <div className='nav-button' onClick={() => logout()} id='logout'>Logout</div>
            </div>

        <Social />

        </nav>
    )
}

export default Navbar;