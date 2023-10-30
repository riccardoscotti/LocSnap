import '../css/explore.css'
import React from 'react'
import axios from 'axios'
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";

axios.defaults.baseURL = 'http://localhost:8080'

const getFavorites = () => {
    axios.post('/recommend', {
        logged_user: localStorage.getItem("user")
    })
    .then(response => {
        if (response.data.status === 200) {
            localStorage.setItem("user_favorite_type", response.data.user_type);
            localStorage.setItem("similar_users", response.data.similar_users);
        } else if (response.data.status === 204) {
            console.log("No photos uploaded yet...");
        }
    })
}

const Explore = () => {
    return (
        <div className="main-content">
            <div className='favorites'>
                <h1 className='title'>Explore</h1>
                <p>You seem to like {localStorage.getItem('user_favorite_type')} pictures</p>

            </div>
        </div>

    )
}

export default Explore;