import '../css/explore.css'
import React from 'react'
import axios from 'axios'
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";

axios.defaults.baseURL = 'http://localhost:8080'

const getFavorites = () => {
    axios.post('/favorites', {
        logged_user: localStorage.getItem("user")
    })
    .then(res => {
        return res
    })
}

const Explore = () => {
    return (
        <div className="main-content">
            <div className='favorites'>
                <h1 className='title'>Explore</h1>
                <p>You seem to like {} pictures</p>
            </div>
        </div>

    )
}

export default Explore;