import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useEffect } from 'react';
import axios from "axios";
import * as L from 'leaflet';
import markerIcon from '../marker-icon.png'

const base_url = "http://localhost:8080"

const Dashboard = () => {

  const mIcon = new L.Icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon,
    iconSize: [45, 48],
    shadowSize: [50, 64],
    iconAnchor: [22, 94],
    shadowAnchor: [4, 62],
    popupAnchor: [-3, -76],
  })

  useEffect(() => {
    retrieveCollections()
  }, []);

  const retrieveCollections = () => {
    axios.post(`${base_url}/retrievecollections`, {logged_user: localStorage.getItem("user")})
    .then((response) => {
      if(response.status === 200) {
        localStorage.setItem("collections", JSON.stringify(response.data.retrievedCollections))
      }
    })
    .catch((error) => {
        if(error.response.status === 401) {
            alert("Error during collection retrieval!")
        }
    });
  }
  
  const bolognaCoords = [44.494887, 11.3426163]
  const collectionsImages = [
    'https://tourismmedia.italia.it/is/image/mitur/20210305163928-shutterstock-172796825?wid=1080&hei=660&fit=constrain,1&fmt=webp',
    'https://www.offerte-vacanza.com/informazioni/wp-content/uploads/2021/09/lago-di-braies-800x445.jpg', 
    'https://www.paesidelgusto.it/media/2021/12/madonna-di-campiglio.jpg&sharpen&save-as=webp&crop-to-fit&w=1200&h=800&q=76'
  ];

  return localStorage.getItem("collections") && (
    <div className='main-content'>
      <div className='collections'>
        <h1 className='title'>My collections</h1>
        <input type='text' className='search-collection'/>
        {
          Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
            return (<CollectionCard className='collection' key={collection[0]} 
            title={collection[0]} place={`Coords: ${collection[1]}`} prevs={collectionsImages} />)
          })
        }
      </div> 
      <MapContainer id='map-container' center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    </div>
  );
}
export default Dashboard;