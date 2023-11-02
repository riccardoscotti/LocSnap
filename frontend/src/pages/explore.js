import '../css/explore.css'
import React, { useCallback, createRef, useEffect } from 'react'
import axios from 'axios'
import * as L from 'leaflet';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";

axios.defaults.baseURL = 'http://localhost:8080'

const Explore = () => {

  const mapRef = createRef()
  const bolognaCoords = [44.494887, 11.3426163]
  let placeMap = {}
  let [markerLoaded, setMarkerLoaded] = React.useState(false)
  
  const getFavorites = () => {
      axios.post('/recommend', {
          logged_user: localStorage.getItem("user")
      })
      .then(response => {
        if (response.data.status === 200) {
          //console.log(response.data.recommendedPlaces);
          localStorage.setItem("user_favorite_type", response.data.user_favorite_type);
          localStorage.setItem("similar_users", response.data.similar_users);
          localStorage.setItem("recommended_places", JSON.stringify(response.data.recommendedPlaces))
  
          Object.entries(response.data.recommendedPlaces).map(place => {
            geoCode(place[1])
          })
          setMarkerLoaded(true)
  
        } else if (response.data.status === 204) {
            console.log("No photos uploaded yet...");
        }
      })
  }
  
  function geoCode(place) {
    axios.get(`https://geocode.maps.co/search?q={${place}}`, {
    })
    .then(response => {
      placeMap[place] = [response.data[0].lat, response.data[0].lon]
      console.log("geocode: " + placeMap)
    })
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
  
  function ShowMarkers() {
    let recommendedMarkers = L.layerGroup()
  
    // Object.entries(placeMap).map( (place) => {
    //   console.log(place);
    // })
  }

    return localStorage.getItem('user_favorite_type') && markerLoaded && (
        <div className="main-content">
            <div className='favorites'>
                <h1 className='title'>Explore</h1>
                <p>You seem to like <span id="favorite-type-text">{localStorage.getItem('user_favorite_type')}</span> pictures</p>      
                <h2>You may also like:</h2>
                {
                  console.log(placeMap)
                  // Object.entries(placeMap).map( (place) => {
                  //   return (<p className="recommended-place" key={place}>Place: {place}</p>);
                  // })
                }
            </div>
            <DrawMap />
            <ShowMarkers />
        </div>

    )
}

export default Explore;