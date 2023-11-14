import '../css/explore.css'
import React, { useCallback, createRef, useEffect, useState } from 'react'
import axios from 'axios'
import * as L from 'leaflet';
import { MapContainer, TileLayer } from "react-leaflet";

import markerIcon from '../marker-icon.png'

const mIcon = new L.Icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon,
  iconSize: [45, 48],
  shadowSize: [50, 64],
  iconAnchor: [22, 94],
  shadowAnchor: [4, 62],
  popupAnchor: [-3, -76],
})

axios.defaults.baseURL = 'http://localhost:8080'

const Explore = () => {

  const mapRef = createRef()
  const bolognaCoords = [44.494887, 11.3426163]
  let [placeMap, setPlaceMap] = useState({})
  let [markerLoaded, setMarkerLoaded] = React.useState(false)
  let [foundPlaces, setFoundPlaces] = React.useState(false)
  
  const getFavorites = async () => {
      await axios.post('/recommend', {
          logged_user: localStorage.getItem("user")
      })
      .then(response => {
        if (response.data.status === 200) {
          localStorage.setItem("user_favorite_type", response.data.user_favorite_type);
          localStorage.setItem("similar_users", response.data.similar_users);
          localStorage.setItem("recommended_places", JSON.stringify(response.data.recommendedPlaces))
          Object.entries(response.data.recommendedPlaces).map(place => {
            geoCode(place[1])
          })
          setFoundPlaces(true)
        }
      })

      setMarkerLoaded(true)
  }

  useEffect(() => {
    getFavorites()
  }, [])
  
  function geoCode(place) {
    axios.get(`https://geocode.maps.co/search?q={${place}}`, {
    })
    .then(response => {
      setPlaceMap(placeMap => ({
        ...placeMap,
        [place]: [response.data[0].lat, response.data[0].lon]
      }))
    })
  }
  
  function MapHookCB() {
      const mapCB = useCallback(node => {
        if (node) {
          mapRef.current = node
          showMarkers()
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

  function drawExplore() {

    if (markerLoaded) {
      if (foundPlaces) {
        return (
          <div className="main-content">
                <div className='favorites'>
                    <h1 className='title'>Explore</h1>
                    <p>You seem to like <span id="favorite-type-text">{localStorage.getItem('user_favorite_type')}</span> pictures</p>      
                    <h2>You may also like:</h2>
                    {
                      Object.entries(placeMap).map(entry => 
                        (<p 
                          key={entry[0]}
                          className='recommended-place'
                          onClick={(e) => {
                            mapRef.current.flyTo(new L.LatLng(entry[1][0], entry[1][1]));
                          }}
                          >
                            {entry[0]}
                          </p>)
                      )
                    }
                </div>
                <DrawMap />
            </div>
        )
      } else {
        return (
          <div>
            <p id="noplaces">No similar users found...</p>
          </div>
        )
      }
    } else {
      return (
        <div>
          <p id="loading">Loading...</p>
        </div>
      )
    }
  }
  
  function showMarkers() {
    let recommendedMarkers = L.layerGroup().addTo(mapRef.current);
    if (mapRef.current !== 'undefined') {
      Object.entries(placeMap).map(entry => {
        let marker = new L.marker([entry[1][0], entry[1][1]], {icon: mIcon})
          .addTo(recommendedMarkers);
          marker.bindPopup(entry[0]);
      })
    }
  }

    return drawExplore()
}

export default Explore;