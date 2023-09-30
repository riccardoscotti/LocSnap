import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useState, useEffect, useLayoutEffect } from 'react';
import axios from "axios";
import { useLocalStorage } from '../hooks/useLocalStorage';

const base_url = "http://localhost:8080"

const Dashboard = () => {

  const [user, setUser] = useLocalStorage("user", null)
  const [collections, setCollections] = useState(null)

  useEffect(() => {
    retrieveCollections(setCollections)
  }, []);

  const retrieveCollections = () => {
    axios.post(`${base_url}/retrievecollections`, {logged_user: user})
    .then((response) => {
      if(response.status === 200) {
        setCollections(response.data.retrievedCollections)
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

  return collections && (
    <div className='main-content'>
      <div className='collections'>
      
        <h1 className='title'>My collections</h1>
        <input type='text' className='search-collection'/>
        
        {
          collections.map( (id, idx) => 
            {
              return (<CollectionCard className='collection' key={id} title={id} place={`Collection index: ${idx}`} prevs={collectionsImages} />)
            }
          )
        }
      </div> 
      <MapContainer id='map-container' center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    </div>
  );
}
export default Dashboard;