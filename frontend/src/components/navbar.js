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
import ListGroup from 'react-bootstrap/ListGroup';

const Navbar = () => {

    const [friendsDialogStatus, openFriendsDialog] = React.useState(false);
    const [friendTextStatus, openFriendsText] = React.useState(false);
    const navigate = useNavigate()
    const uploadFilterRef = useRef(null)
    var friendRef = createRef();

    function OpenInputText(props) {
    
        return (
            <Modal
            {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                  Insert friend's username
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                <input type='text' ref={friendRef} className='search-collection' />
              </Modal.Body>
              <Modal.Footer>
                <Button id="confirmButton" onClick={() => {
                    axios.post('/add_friend', {
                        loggedUser: localStorage.getItem('user'),
                        newFriend: friendRef.current.value
                    })
                    .then((response) => {
                        if(response.data.status === 200) {
                            alert(`You and ${friendRef.current.value} "are now friends!`)
                        } else if (response.data.status === 409) {
                            alert(`${friendRef.current.value} and you are already friend.`)
                        }
                    })
                    .catch((error) => {
                        if(error.response.status === 401) {
                            alert("Error during friend addition.")
                        }
                    });
                }}>Confirm</Button>
              </Modal.Footer>
            </Modal>
          ); 
    }
    
    function addFriend() {
        openFriendsDialog(false)
        openFriendsText(true)
    }
    
    function removeFriend() {
        // ...
    }
    
    function sharePhoto() {
        // ...
    }
    
    function publishPhoto() {
        // ...
    }
    
    function SocialDialog(props) {
        
        return localStorage.getItem("friends") && (
            <Modal
              {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                  Manage your social activities
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                <ListGroup>
                    {    
                        Object.entries(JSON.parse(localStorage.getItem("friends"))).map( (friend) => {
                            return (
                                <div className="friendDiv">
                                    <ListGroup.Item className="friendItem"> {friend[1].name} </ListGroup.Item> 
                                </div>
                            )
                        })
                    }
                </ListGroup>
                <hr class="new5" />
                <div className='social-option-div' onClick={addFriend}>
                    <p className='social-option'>Add friend</p>
                </div>
                <div className='social-option-div' onClick={removeFriend}>
                    <p className='social-option'>Remove friend</p>
                </div>
                <div className='social-option-div' onClick={sharePhoto}>
                    <p className='social-option'>Share photo with a friend</p>
                </div>
                <div className='social-option-div' onClick={publishPhoto}>
                    <p className='social-option'>Publish photo</p>
                </div>
              </Modal.Body>
              <Modal.Footer>
                <Button id="confirmButton">Confirm</Button>
              </Modal.Footer>
            </Modal>
        );
    }

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
                <div className='nav-button' onClick={() => {
                    openFriendsDialog(true)
                }}>
                    Social
                </div>
                {/* <div className='nav-button' onClick={() => navigate('/social')}>Social</div> */}
                <div className='nav-button' onClick={() => logout()} id='logout'>Logout</div>
            </div>

            <SocialDialog
                show={ friendsDialogStatus }
                onHide={() => { openFriendsDialog(false) }}
            /> 

            <OpenInputText
                show={ friendTextStatus }
                onHide={() => openFriendsText(false) }
            />

        </nav>
    )
}

export default Navbar;