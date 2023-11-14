import '../css/social.css'
import React, { useEffect } from 'react'
import axios from 'axios';

axios.defaults.baseURL = 'http://localhost:8080'

const Social = () => {

    var [friends, setFriends] = React.useState(null)

    function getFriends () {
        axios.post('/get_friends', {
            logged_user: localStorage.getItem("user")
        })
        .then(response => {
            setFriends(response.data.friends)
        })
    }

    useEffect(() => {
        getFriends()
    }, [])

    return friends && (
        <div className="main-content-social">
            <h1 className='title'>Social</h1>
                <div className='friends-list'>
                {
                Object.entries(friends).map( (friend) => {
                    return (
                        <div>
                            <p id='friend'> {friend[1]} </p>
                        </div>
                    )
                })
                }
            </div>
        </div>

    )
}

export default Social;