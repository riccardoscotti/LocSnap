import './navbar.css'
import logo_path from '../locsnap_icon.png'
import Button from 'react-bootstrap/Button';
import { useNavigate } from 'react-router-dom'
import React, { useRef, useState, createRef, useEffect } from 'react';
import Modal from 'react-bootstrap/Modal';
import axios from 'axios'
import ListGroup from 'react-bootstrap/ListGroup';

const Navbar = () => {

    const [socialDialogStatus, openSocialDialog] = React.useState(false);
    const [friendTextStatus, openFriendsText] = React.useState(false);
    const [friendTextRemoveStatus, openFriendsTextRemove] = React.useState(false);
    const [chooseCollectionDialogStatus, openChooseCollectionDialog] = React.useState(false);
    const [ManagePhotosDialogStatus, openManageDialog] = React.useState(false);
    const [choosePhotoDialogStatus, openChoosePhotoDialog] = React.useState(false);
    const [chooseFriendDialogStatus, openFriendDialog] = React.useState(false);
    const navigate = useNavigate()
    const uploadFilterRef = useRef(null)
    var friendRef = createRef(); // Add
    var friendRef2 = createRef(); // Remove
    var friendRef3 = createRef(); // Tag

    function ChooseCollectionDialog(props) {
        return localStorage.getItem("collections") && (
            <Modal
            {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                Select the collection
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                {
                    Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
                        return (
                            <div key={collection[1].name} className="dialogDiv" id="photoSelected" onClick={
                                () => {
                                    localStorage.setItem('selectedCollection', collection[1].name)
                                    imagesOf(collection[1].name)
                                    openChooseCollectionDialog(false)
                                }
                            }>
                                <p className="dialogItem"> {collection[1].name} </p> 
                            </div>
                        )
                    })
                }
              </Modal.Body>
            </Modal>
          );
    }

    // Friends list
    function ChooseFriendDialog(props) {
        return localStorage.getItem("friends") && (
            <Modal
            {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                    Select a friend
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                {
                    Object.entries(JSON.parse(localStorage.getItem("friends"))).map( (friend) => {
                        return (
                            <div key={friend[1]} className="dialogDiv" id="photoSelected" onClick={
                                () => {
                                    localStorage.setItem('selectedFriend', friend[1])
                                    openFriendDialog(false)
                                    tagFriend()
                                }
                            }>
                                <p className="dialogItem"> {friend[1]} </p> 
                            </div>
                        )
                    })
                }
              </Modal.Body>
              <Modal.Footer>
                <Button id="confirmButton" onClick={() => {
                    
                }}>Confirm</Button>
              </Modal.Footer>
            </Modal>
          );
    }

    function publishPhoto () {
        axios.post('/publish', {
            logged_user: localStorage.getItem('user'),
            image_name: localStorage.getItem('selectedPhoto'),
            collection_name: localStorage.getItem('selectedCollection')
        })
        .then((response) => {
            if (response.data.status === 200) {
                alert(`${localStorage.getItem("selectedPhoto")} published successfully.`)
            }
        })
        .catch(error => {
            console.log(error);
            alert('Could not tag friend.')
        })
    }

    async function deletePhoto() {
        await axios.post('/deletephoto', {
            logged_user: localStorage.getItem('user'),
            image_name: localStorage.getItem('selectedPhoto'),
            collection_name: localStorage.getItem('selectedCollection')
        })
        .then((response) => {
            if (response.data.status === 200) {
                alert(`${localStorage.getItem("selectedPhoto")} of collection ${localStorage.getItem("selectedCollection")} deleted successfully.`)

            }
        })
        .catch(error => {
            console.log(error);
            alert('Error during image deletion.')
        })

        window.location.reload();
    }

    function tagFriend() {
        axios.post('/tag_friend', {
            logged_user: localStorage.getItem('user'),
            image_name: localStorage.getItem('selectedPhoto'),
            friend: localStorage.getItem('selectedFriend')
        })
        .then((response) => {
            if (response.data.status === 200) {
                alert(`${localStorage.getItem("selectedFriend")} tagged successfully.`)
            }
        })
        .catch(error => {
            console.log(error);
            alert('Could not tag friend.')
        })
    }

    function imagesOf(collectionName) {
        let img_names = []
        axios.post('/imagesof', {
            logged_user: localStorage.getItem('user'),
            collection_name: collectionName
        })
        .then((response) => {
            switch(response.data.status) {
                case 200:
                    Object.entries(response.data.images).map(img => {
                        img_names.push(img[1].name)
                    })
                    localStorage.setItem('imagesOfSelectedColl', JSON.stringify(Object(img_names)))
                    openChoosePhotoDialog(true)
                    break;
                case 204:
                    alert('The collection is empty');
                    break;
                default:
                    alert('Connection error');
                    break;

            }
        })
        .catch(error => {
            console.log(error);
            alert('Could not retrieve pictures')
        })

        return img_names
    }

    function ChoosePhotoDialog(props) {
        return (
            <Modal
            {...props}
            size="md"
            aria-labelledby="contained-modal-title-vcenter"
            centered >
                <Modal.Header>
                    <Modal.Title id='contained-modal-title-vcenter'>
                        Select the pictures you want to share
                    </Modal.Title>
                </Modal.Header>
                    <Modal.Body>
                        {
                            choosePhotoDialogStatus && Object.entries(JSON.parse(localStorage.getItem('imagesOfSelectedColl'))).map(img => (
                                <div key={img[0]} className="dialogDiv" id="photoSelected" onClick={
                                    () => {
                                        localStorage.setItem('selectedPhoto', img[1])
                                        openChoosePhotoDialog(false)

                                        switch (localStorage.getItem("intent")) {
                                            case "share":
                                                openFriendDialog(true)
                                                break;
                                        
                                            case "publish":
                                                publishPhoto()
                                                break;
    
                                            case "delete":
                                                deletePhoto()
                                                break;
    
                                            default:
                                                break;
                                        }

                                        
                                    }
                                }>
                                    <p className="dialogItem"> {img[1]} </p> 
                                </div>
                            ))
                        }
                    </Modal.Body>
                

            </Modal>
        )
    }

    function OpenInputText(props) {
        return (
            <Modal
            {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                Insert the username of the friend you want to add
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
                            alert(`You and ${friendRef.current.value} are now friends!`)
                            window.location.reload()
                        } else if (response.data.status === 204) {
                            alert(`User ${friendRef.current.value} does not exist.`)
                        } else {
                            alert(`You and ${friendRef.current.value} are already friends.`)
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
        openSocialDialog(false)
        openFriendsText(true)
    }
    
    function removeFriend() {
        openSocialDialog(false)
        openFriendsTextRemove(true)
    }

    function FriendTextRemove(props) {
        return (
            <Modal
            {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                  Insert the username of the friend you want to delete
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                <input type='text' ref={friendRef2} className='search-collection' />
              </Modal.Body>
              <Modal.Footer>
                <Button id="confirmButton" onClick={() => {
                    axios.post('/remove_friend', {
                        logged_user: localStorage.getItem('user'),
                        friend: friendRef2.current.value
                    })
                    .then((response) => {
                        if(response.data.status === 200) {
                            alert(`You and ${friendRef2.current.value} are no longer friends!`)
                            window.location.reload()
                        } else if (response.data.status === 409) {
                            alert(`You and ${friendRef2.current.value} are not friend.`)
                        }
                    })
                    .catch((error) => {
                        if(error.response.status === 401) {
                            alert("Error during friend removal.")
                        }
                    });
                }}>Confirm</Button>
              </Modal.Footer>
            </Modal>
        ); 
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
                                <div key={friend[1]} className="dialogDiv">
                                    <ListGroup.Item className="dialogItem"> {friend[1]} </ListGroup.Item> 
                                </div>
                            )
                        })
                    }
                </ListGroup>
                <hr class="new5" />
                <div className='dialog-option-div' onClick={addFriend}>
                    <p className='dialog-option'>Add friend</p>
                </div>
                <div className='dialog-option-div' onClick={removeFriend}>
                    <p className='dialog-option'>Remove friend</p>
                </div>
                <div className='dialog-option-div' onClick={() => {
                    openSocialDialog(false)
                    openChooseCollectionDialog(true)
                    localStorage.setItem("intent", "share")
                }}>
                    <p className='dialog-option'>Share photo with a friend</p>
                </div>
              </Modal.Body>
            </Modal>
        );
    }

    function ManagePhotosDialog(props) {
        return (
            <Modal
              {...props}
              size="md"
              aria-labelledby="contained-modal-title-vcenter"
              centered >
              <Modal.Header>
                <Modal.Title id="contained-modal-title-vcenter">
                  Manage your photos
                </Modal.Title>
              </Modal.Header>
              <Modal.Body id='social-body'>
                <div className="dialog-option-div" onClick={() => {
                    openManageDialog(false)
                    openChooseCollectionDialog(true)
                    localStorage.setItem("intent", "publish")
                }}>
                    <p className='dialog-option'>Publish photo</p> 
                </div>
                <div className="dialog-option-div" onClick={() => {
                    openManageDialog(false)
                    openChooseCollectionDialog(true)
                    localStorage.setItem("intent", "delete")
                }}>
                    <p className='dialog-option'>Delete photo</p>
                </div>
              </Modal.Body>
            </Modal>
        )
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
        localStorage.clear()
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
                <div className='nav-button' onClick={() => {
                    openManageDialog(true)
                }}>Manage your photos</div>
                <div className='nav-button' onClick={() => {
                    openSocialDialog(true)
                }}> Social </div>
                <div className='nav-button' onClick={() => navigate('/explore')}>Explore</div>
                <div className='nav-button' onClick={() => logout()} id='logout'>Logout</div>
            </div>

            <ManagePhotosDialog
                show={ ManagePhotosDialogStatus }
                onHide={() => { openManageDialog(false) }}
            /> 

            <SocialDialog
                show={ socialDialogStatus }
                onHide={() => { openSocialDialog(false) }}
            /> 

            <OpenInputText
                show={ friendTextStatus }
                onHide={() => openFriendsText(false) }
            />

            <FriendTextRemove
                show={ friendTextRemoveStatus }
                onHide={ () => openFriendsTextRemove(false) }
            />

            <ChooseCollectionDialog
                show={ chooseCollectionDialogStatus }
                onHide={ () => openChooseCollectionDialog(false) }
            />

            <ChooseFriendDialog 
                show={ chooseFriendDialogStatus }
                onHide={ () => openFriendDialog(false) } />

            <ChoosePhotoDialog
                show={ choosePhotoDialogStatus }
                onHide={ () => {
                    openChoosePhotoDialog(false)
                    localStorage.removeItem("imagesOfSelectedColl")
                    localStorage.removeItem("selectedCollection")
                }}
            />


        </nav>
    )
}

export default Navbar;