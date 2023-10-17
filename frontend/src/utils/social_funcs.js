import React, { useRef, useState, createRef } from 'react';
import Modal from 'react-bootstrap/Modal';
import axios from 'axios'
import Button from 'react-bootstrap/Button';

const Social = () => {

  var [socialDialogStatus, setDialogStatus] = useState(false);
  var [friendTextStatus, setFriendTextStatus] = useState(false);


    function OpenInputText(props) {
        var friendRef = createRef();
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
        // SocialDialog(false)
        Social.OpenInputText(true)
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
        
        return (
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

    return (
      <div>
        <SocialDialog
          show={ socialDialogStatus }
          onHide={() => { setDialogStatus(false) }}
        /> 

        <OpenInputText
          show={ friendTextStatus }
          onHide={() => setFriendTextStatus(false) }
        />
      </div>
    )
}

export default Social;