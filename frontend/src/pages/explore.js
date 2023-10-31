import '../css/explore.css'
import React, { useCallback, createRef, useEffect } from 'react'
import axios from 'axios'
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";

axios.defaults.baseURL = 'http://localhost:8080'

const mapRef = createRef()
const bolognaCoords = [44.494887, 11.3426163]

const getFavorites = () => {
    axios.post('/recommend', {
        logged_user: localStorage.getItem("user")
    })
    .then(response => {
        if (response.data.status === 200) {
          console.log(response.data);
            localStorage.setItem("user_favorite_type", response.data.user_favorite_type);
            localStorage.setItem("similar_users", response.data.similar_users);
        } else if (response.data.status === 204) {
            console.log("No photos uploaded yet...");
        }
    })
}

function showMarkers() {
    // if (mapRef.current !== "undefined") {
    //   var userPhotosMarkers = L.layerGroup().addTo(mapRef.current);
    //   Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
    //     var marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: mIcon}).addTo(userPhotosMarkers);
    //     marker.bindPopup(img[1].name);
    //   })
    // }
  }

function MapHookCB() {
    const mapCB = useCallback(node => {
      if (node) {
        mapRef.current = node
        // showMarkers()
      }
    }, [])

    return [mapCB];
}

function DrawMap() {
    const [innerMapRef] = MapHookCB()
    
    return (
      <MapContainer id='map-container' ref={innerMapRef} center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
        <TileLayer id='tile-layer' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    )
}

const Explore = () => {
    useEffect(getFavorites, [])
    return localStorage.getItem('user_favorite_type') && (
        <div className="main-content">
            <div className='favorites'>
                <h1 className='title'>Explore</h1>
                <p>You seem to like {localStorage.getItem('user_favorite_type')} pictures</p>      
            </div>
            <DrawMap />
        </div>

    )
}

export default Explore;